/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesProperty;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * forwardingrules-manager org.opendaylight.openflowplugin.applications.frm.impl
 *
 * <p>
 * Manager and middle point for whole module. It contains ActiveNodeHolder and
 * provide all RPC services.
 *
 */
public class ForwardingRulesManagerImpl implements ForwardingRulesManager {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerImpl.class);

    static final int STARTUP_LOOP_TICK = 500;
    static final int STARTUP_LOOP_MAX_RETRIES = 8;
    private static final int FRM_RECONCILIATION_PRIORITY = Integer.getInteger("frm.reconciliation.priority", 0);
    private static final String SERVICE_NAME = "FRM";

    private final AtomicLong txNum = new AtomicLong();
    private final DataBroker dataBroker;
    private final SalFlowService salFlowService;
    private final SalGroupService salGroupService;
    private final SalMeterService salMeterService;
    private final SalTableService salTableService;
    private final SalBundleService salBundleService;
    private final AutoCloseable configurationServiceRegistration;
    private final MastershipChangeServiceManager mastershipChangeServiceManager;
    private ForwardingRulesCommiter<Flow> flowListener;
    private ForwardingRulesCommiter<Group> groupListener;
    private ForwardingRulesCommiter<Meter> meterListener;
    private ForwardingRulesCommiter<TableFeatures> tableListener;
    private FlowNodeReconciliation nodeListener;
    private NotificationRegistration reconciliationNotificationRegistration;
    private FlowNodeConnectorInventoryTranslatorImpl flowNodeConnectorInventoryTranslatorImpl;
    private DeviceMastershipManager deviceMastershipManager;
    private final ReconciliationManager reconciliationManager;

    private boolean disableReconciliation;
    private boolean staleMarkingEnabled;
    private int reconciliationRetryCount;
    private boolean isBundleBasedReconciliationEnabled;

    public ForwardingRulesManagerImpl(final DataBroker dataBroker,
                                      final RpcConsumerRegistry rpcRegistry,
                                      final ForwardingRulesManagerConfig config,
                                      final MastershipChangeServiceManager mastershipChangeServiceManager,
                                      final ConfigurationService configurationService,
                                      final ReconciliationManager reconciliationManager) {
        this.dataBroker = dataBroker;
        this.reconciliationManager = reconciliationManager;
        this.mastershipChangeServiceManager = mastershipChangeServiceManager;
        disableReconciliation = config.isDisableReconciliation();
        staleMarkingEnabled = config.isStaleMarkingEnabled();
        reconciliationRetryCount = config.getReconciliationRetryCount();
        isBundleBasedReconciliationEnabled = config.isBundleBasedReconciliationEnabled();
        configurationServiceRegistration = configurationService.registerListener(this);
        salFlowService = rpcRegistry.getRpcService(SalFlowService.class);
        salGroupService = rpcRegistry.getRpcService(SalGroupService.class);
        salMeterService = rpcRegistry.getRpcService(SalMeterService.class);
        salTableService = rpcRegistry.getRpcService(SalTableService.class);
        salBundleService = rpcRegistry.getRpcService(SalBundleService.class);
    }

    @Override
    public void start() {
        if (!isReconciliationDisabled()) {
            nodeListener = new FlowNodeReconciliationImpl(this);
            reconciliationNotificationRegistration = reconciliationManager.registerService(nodeListener);
            LOG.debug("Reconciliation is enabled by user and successfully registered to the reconciliation framework");
        }

        deviceMastershipManager = new DeviceMastershipManagerImpl(mastershipChangeServiceManager);
        flowNodeConnectorInventoryTranslatorImpl = new FlowNodeConnectorInventoryTranslatorImpl(this, dataBroker);
        flowListener = new FlowForwarder(this, dataBroker);
        groupListener = new GroupForwarder(this, dataBroker);
        meterListener = new MeterForwarder(this, dataBroker);
        tableListener = new TableForwarder(this, dataBroker);
        LOG.info("ForwardingRulesManager has started successfully.");
    }

    @Override
    public void close() throws Exception {
        configurationServiceRegistration.close();

        if (flowListener != null) {
            flowListener.close();
            flowListener = null;
        }

        if (groupListener != null) {
            groupListener.close();
            groupListener = null;
        }

        if (meterListener != null) {
            meterListener.close();
            meterListener = null;
        }

        if (tableListener != null) {
            tableListener.close();
            tableListener = null;
        }

        if (nodeListener != null) {
            nodeListener.close();
            nodeListener = null;
        }

        if (deviceMastershipManager != null) {
            deviceMastershipManager.close();
        }

        if (reconciliationNotificationRegistration != null) {
            reconciliationNotificationRegistration.close();
            reconciliationNotificationRegistration = null;
        }
    }

    @Override
    public ReadOnlyTransaction getReadTransaction() {
        return dataBroker.newReadOnlyTransaction();
    }

    @Override
    public WriteTransaction getWriteTransaction() {
        return dataBroker.newWriteOnlyTransaction();
    }

    @Override
    public String getNewTransactionId() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public boolean checkNodeInOperationalDataStore(InstanceIdentifier<FlowCapableNode> ident) {
        try (ReadOnlyTransaction transaction = getReadTransaction()) {
            return transaction
                    .read(LogicalDatastoreType.OPERATIONAL, ident.firstIdentifierOf(Node.class))
                    .checkedGet()
                    .isPresent();
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", ident, e);
        }

        return false;
    }

    @Override
    public SalFlowService getSalFlowService() {
        return salFlowService;
    }

    @Override
    public SalGroupService getSalGroupService() {
        return salGroupService;
    }

    @Override
    public SalMeterService getSalMeterService() {
        return salMeterService;
    }

    @Override
    public SalTableService getSalTableService() {
        return salTableService;
    }

    @Override
    public SalBundleService getSalBundleService() {
        return salBundleService;
    }

    @Override
    public ForwardingRulesCommiter<Flow> getFlowCommiter() {
        return flowListener;
    }

    @Override
    public ForwardingRulesCommiter<Group> getGroupCommiter() {
        return groupListener;
    }

    @Override
    public ForwardingRulesCommiter<Meter> getMeterCommiter() {
        return meterListener;
    }

    @Override
    public ForwardingRulesCommiter<TableFeatures> getTableFeaturesCommiter() {
        return tableListener;
    }

    @Override
    public boolean isReconciliationDisabled() {
        return disableReconciliation;
    }

    @Override
    public boolean isStaleMarkingEnabled() {
        return staleMarkingEnabled;
    }

    @Override
    public int getReconciliationRetryCount() {
        return reconciliationRetryCount;
    }

    @Override
    public FlowNodeConnectorInventoryTranslatorImpl getFlowNodeConnectorInventoryTranslatorImpl() {
        return flowNodeConnectorInventoryTranslatorImpl;
    }

    @Override
    public boolean isBundleBasedReconciliationEnabled() {
        return isBundleBasedReconciliationEnabled;
    }

    @Override
    public boolean isNodeOwner(InstanceIdentifier<FlowCapableNode> ident) {
        return Objects.nonNull(ident) && deviceMastershipManager.isDeviceMastered(ident.firstKeyOf(Node.class).getId());
    }

    @VisibleForTesting
    void setDeviceMastershipManager(final DeviceMastershipManager deviceMastershipManager) {
        this.deviceMastershipManager = deviceMastershipManager;
    }

    @Override
    public void onPropertyChanged(@Nonnull final String propertyName, @Nonnull final String propertyValue) {
        Optional.ofNullable(ForwardingRulesProperty.forValue(propertyName)).ifPresent(forwardingRulesProperty -> {
            switch (forwardingRulesProperty) {
                case DISABLE_RECONCILIATION:
                    disableReconciliation = Boolean.valueOf(propertyValue);
                    break;
                case STALE_MARKING_ENABLED:
                    staleMarkingEnabled = Boolean.valueOf(propertyValue);
                    break;
                case RECONCILIATION_RETRY_COUNT:
                    reconciliationRetryCount = Integer.valueOf(propertyValue);
                    break;
                case BUNDLE_BASED_RECONCILIATION_ENABLED:
                    isBundleBasedReconciliationEnabled = Boolean.valueOf(propertyValue);
                    break;
                default:
                    LOG.warn("Not forwarding rule property found.");
                    break;
            }
        });
    }
}