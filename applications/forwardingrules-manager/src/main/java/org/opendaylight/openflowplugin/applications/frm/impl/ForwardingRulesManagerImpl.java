/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
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
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesProperty;
import org.opendaylight.openflowplugin.applications.frm.impl.commiters.FlowForwarder;
import org.opendaylight.openflowplugin.applications.frm.impl.commiters.FlowNodeConnectorInventoryTranslatorImpl;
import org.opendaylight.openflowplugin.applications.frm.impl.commiters.GroupForwarder;
import org.opendaylight.openflowplugin.applications.frm.impl.commiters.MeterForwarder;
import org.opendaylight.openflowplugin.applications.frm.impl.commiters.TableForwarder;
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
 * forwardingrules-manager
 * org.opendaylight.openflowplugin.applications.frm.impl
 *
 * Manager and middle point for whole module.
 * It contains ActiveNodeHolder and provide all RPC services.
 *
 */
public class ForwardingRulesManagerImpl implements ForwardingRulesManager {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerImpl.class);

    private final AtomicLong txNum = new AtomicLong();
    private final DataBroker dataService;
    private final SalFlowService salFlowService;
    private final SalGroupService salGroupService;
    private final SalMeterService salMeterService;
    private final SalTableService salTableService;
    private final MastershipChangeServiceManager mastershipChangeServiceManager;
    private final SalBundleService salBundleService;
    private final AutoCloseable configurationServiceRegistration;
    private final ReconciliationManager reconciliationManager;
    private ForwardingRulesCommiter<Flow> flowListener;
    private ForwardingRulesCommiter<Group> groupListener;
    private ForwardingRulesCommiter<Meter> meterListener;
    private ForwardingRulesCommiter<TableFeatures> tableListener;
    private FlowNodeReconciliation nodeListener;
    private NotificationRegistration reconciliationNotificationRegistration;
    private FlowNodeConnectorInventoryTranslatorImpl flowNodeConnectorInventoryTranslatorImpl;
    private DeviceMastershipManagerImpl deviceMastershipManager;
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
        disableReconciliation = config.isDisableReconciliation();
        staleMarkingEnabled = config.isStaleMarkingEnabled();
        reconciliationRetryCount = config.getReconciliationRetryCount();
        isBundleBasedReconciliationEnabled = config.isBundleBasedReconciliationEnabled();
        configurationServiceRegistration = configurationService.registerListener(this);
        dataService = dataBroker;
        this.mastershipChangeServiceManager = mastershipChangeServiceManager;
        this.reconciliationManager = reconciliationManager;
        this.salFlowService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalFlowService.class),
                "RPC SalFlowService not found.");
        this.salGroupService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalGroupService.class),
                "RPC SalGroupService not found.");
        this.salMeterService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalMeterService.class),
                "RPC SalMeterService not found.");
        this.salTableService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalTableService.class),
                "RPC SalTableService not found.");
        this.salBundleService = Preconditions.checkNotNull(rpcRegistry.getRpcService(SalBundleService.class),
                "RPC SalBundlService not found.");
    }

    @Override
    public void start() {
        this.nodeListener = new FlowNodeReconciliationImpl(this);

        if (this.isReconciliationDisabled()) {
            LOG.debug("Reconciliation is disabled by user");
        } else {
            this.reconciliationNotificationRegistration = reconciliationManager.registerService(this.nodeListener);
            LOG.debug("Reconciliation is enabled by user and successfully registered to the tasks framework");
        }

        deviceMastershipManager = new DeviceMastershipManagerImpl(mastershipChangeServiceManager);
        flowNodeConnectorInventoryTranslatorImpl = new FlowNodeConnectorInventoryTranslatorImpl(dataService);
        flowListener = new FlowForwarder(this, dataService);
        groupListener = new GroupForwarder(this, dataService);
        meterListener = new MeterForwarder(this, dataService);
        tableListener = new TableForwarder(this, dataService);
        LOG.info("ForwardingRulesManager has started successfully.");
    }

    @Override
    public void close() throws Exception {
        configurationServiceRegistration.close();

        if (this.flowListener != null) {
            this.flowListener.close();
            this.flowListener = null;
        }
        if (this.groupListener != null) {
            this.groupListener.close();
            this.groupListener = null;
        }
        if (this.meterListener != null) {
            this.meterListener.close();
            this.meterListener = null;
        }
        if (this.tableListener != null) {
            this.tableListener.close();
            this.tableListener = null;
        }
        if (this.nodeListener != null) {
            this.nodeListener.close();
            this.nodeListener = null;
        }
        if (deviceMastershipManager != null) {
            deviceMastershipManager.close();
        }
        if (this.reconciliationNotificationRegistration != null) {
            this.reconciliationNotificationRegistration.close();
            this.reconciliationNotificationRegistration = null;
        }
    }

    @Override
    public ReadOnlyTransaction getReadTransaction() {
        return dataService.newReadOnlyTransaction();
    }

    @Override
    public WriteTransaction getWriteTransaction() {
        return dataService.newWriteOnlyTransaction();
    }

    @Override
    public String getNewTransactionId() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public boolean checkNodeInOperationalDataStore(InstanceIdentifier<FlowCapableNode> ident) {
        final ReadOnlyTransaction transaction = dataService.newReadOnlyTransaction();
        final CheckedFuture<com.google.common.base.Optional<Node>, ReadFailedException> future = transaction
                .read(LogicalDatastoreType.OPERATIONAL, ident.firstIdentifierOf(Node.class));

        try {
            com.google.common.base.Optional<Node> isNodePresent = future.checkedGet();
            transaction.close();
            return isNodePresent.isPresent();
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", ident, e);
            transaction.close();
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
            }
        });
    }

    @VisibleForTesting
    void setDeviceMastershipManager(final DeviceMastershipManagerImpl deviceMastershipManager) {
        this.deviceMastershipManager = deviceMastershipManager;
    }
}
