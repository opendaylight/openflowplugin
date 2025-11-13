/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.BundleMessagesCommiter;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesCommiter;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesProperty;
import org.opendaylight.openflowplugin.applications.frm.NodeConfigurator;
import org.opendaylight.openflowplugin.applications.frm.nodeconfigurator.NodeConfiguratorImpl;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.serviceutils.srm.RecoverableListener;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager and middle point for whole module. It contains ActiveNodeHolder and provide all RPC services.
 */
@Singleton
public final class ForwardingRulesManagerImpl implements ForwardingRulesManager, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerImpl.class);

    private static final int FRM_RECONCILIATION_PRIORITY = Integer.getInteger("frm.reconciliation.priority", 1);
    private static final String SERVICE_NAME = "FRM";

    private final FlowNodeConnectorInventoryTranslatorImpl flowNodeConnectorInventoryTranslatorImpl;
    private final DevicesGroupRegistry devicesGroupRegistry = new DevicesGroupRegistry();
    private final NodeConfigurator nodeConfigurator = new NodeConfiguratorImpl();
    private final OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler;
    private final BundleMessagesCommiter<Group> bundleGroupListener;
    private final BundleMessagesCommiter<Flow> bundleFlowListener;
    private final ServiceRecoveryRegistry serviceRecoveryRegistry;
    private final AtomicLong txNum = new AtomicLong();
    private final DataBroker dataService;

    private final AutoCloseable configurationServiceRegistration;
    private ForwardingRulesCommiter<Flow> flowListener;
    private ForwardingRulesCommiter<Group> groupListener;
    private ForwardingRulesCommiter<Meter> meterListener;
    private ForwardingRulesCommiter<TableFeatures> tableListener;
    private FlowNodeReconciliationImpl flowNodeReconciliation;
    private NotificationRegistration reconciliationNotificationRegistration;
    private DeviceMastershipManager deviceMastershipManager;
    private boolean disableReconciliation;
    private boolean staleMarkingEnabled;
    private int reconciliationRetryCount;
    private boolean isBundleBasedReconciliationEnabled;

    private final @NonNull AddFlow addFlow;
    private final @NonNull RemoveFlow removeFlow;
    private final @NonNull UpdateFlow updateFlow;
    private final @NonNull AddGroup addGroup;
    private final @NonNull RemoveGroup removeGroup;
    private final @NonNull UpdateGroup updateGroup;
    private final @NonNull AddMeter addMeter;
    private final @NonNull RemoveMeter removeMeter;
    private final @NonNull UpdateMeter updateMeter;
    private final @NonNull UpdateTable updateTable;
    private final @NonNull ControlBundle controlBundle;
    private final @NonNull AddBundleMessages addBundleMessages;
    private final @NonNull GetActiveBundle getActiveBundle;

    @Inject
    public ForwardingRulesManagerImpl(final DataBroker dataBroker,
                                      final RpcService rpcRegistry,
                                      final RpcProviderService rpcProviderService,
                                      final ForwardingRulesManagerConfig config,
                                      final MastershipChangeServiceManager mastershipChangeServiceManager,
                                      final ConfigurationService configurationService,
                                      final ReconciliationManager reconciliationManager,
                                      final OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler,
                                      final ServiceRecoveryRegistry serviceRecoveryRegistry,
                                      final FlowGroupCacheManager flowGroupCacheManager) {
        disableReconciliation = config.getDisableReconciliation();
        staleMarkingEnabled = config.getStaleMarkingEnabled();
        reconciliationRetryCount = config.getReconciliationRetryCount().toJava();
        isBundleBasedReconciliationEnabled = config.getBundleBasedReconciliationEnabled();
        configurationServiceRegistration = configurationService.registerListener(this);
        dataService = requireNonNull(dataBroker);
        this.openflowServiceRecoveryHandler = requireNonNull(openflowServiceRecoveryHandler);
        this.serviceRecoveryRegistry = requireNonNull(serviceRecoveryRegistry);

        addFlow = rpcRegistry.getRpc(AddFlow.class);
        removeFlow = rpcRegistry.getRpc(RemoveFlow.class);
        updateFlow = rpcRegistry.getRpc(UpdateFlow.class);
        addGroup = rpcRegistry.getRpc(AddGroup.class);
        removeGroup = rpcRegistry.getRpc(RemoveGroup.class);
        updateGroup = rpcRegistry.getRpc(UpdateGroup.class);
        addMeter = rpcRegistry.getRpc(AddMeter.class);
        removeMeter = rpcRegistry.getRpc(RemoveMeter.class);
        updateMeter = rpcRegistry.getRpc(UpdateMeter.class);
        controlBundle = rpcRegistry.getRpc(ControlBundle.class);
        addBundleMessages = rpcRegistry.getRpc(AddBundleMessages.class);
        updateTable = rpcRegistry.getRpc(UpdateTable.class);
        getActiveBundle = rpcRegistry.getRpc(GetActiveBundle.class);

        flowNodeReconciliation = new FlowNodeReconciliationImpl(this, dataService, SERVICE_NAME,
                FRM_RECONCILIATION_PRIORITY, ResultState.DONOTHING, flowGroupCacheManager);
        if (isReconciliationDisabled()) {
            LOG.debug("Reconciliation is disabled by user");
        } else {
            reconciliationNotificationRegistration = reconciliationManager.registerService(flowNodeReconciliation);
            LOG.debug("Reconciliation is enabled by user and successfully registered to the reconciliation framework");
        }
        deviceMastershipManager = new DeviceMastershipManager(flowNodeReconciliation, dataService,
            mastershipChangeServiceManager, rpcProviderService);
        flowNodeConnectorInventoryTranslatorImpl = new FlowNodeConnectorInventoryTranslatorImpl(dataService);

        bundleFlowListener = new BundleFlowForwarder(this);
        bundleGroupListener = new BundleGroupForwarder(this);
        flowListener = new FlowForwarder(this, dataService);
        groupListener = new GroupForwarder(this, dataService);
        meterListener = new MeterForwarder(this, dataService);
        tableListener = new TableForwarder(this, dataService);
        LOG.info("ForwardingRulesManager has started successfully.");
    }

    @Override
    @PreDestroy
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
        if (flowNodeReconciliation != null) {
            flowNodeReconciliation.close();
            flowNodeReconciliation = null;
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
    public ReadTransaction getReadTransaction() {
        return dataService.newReadOnlyTransaction();
    }

    @Override
    public String getNewTransactionId() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public boolean isNodeActive(final DataObjectIdentifier<FlowCapableNode> ident) {
        return deviceMastershipManager.isNodeActive(ident.getFirstKeyOf(Node.class).getId());
    }

    @Override
    public boolean checkNodeInOperationalDataStore(final DataObjectIdentifier<FlowCapableNode> ident) {
        boolean result = false;
        final var nodeIid = ident.trimTo(Node.class);
        try (var transaction = dataService.newReadOnlyTransaction()) {
            var future = transaction.exists(LogicalDatastoreType.OPERATIONAL, nodeIid);
            if (future.get()) {
                result = true;
            } else {
                LOG.debug("{}: Failed to read {}", Thread.currentThread().getStackTrace()[1], nodeIid);
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Failed to read {} ", nodeIid, e);
        }

        return result;
    }

    @Override
    public AddFlow addFlow() {
        return addFlow;
    }

    @Override
    public RemoveFlow removeFlow() {
        return removeFlow;
    }

    @Override
    public @NonNull UpdateFlow updateFlow() {
        return updateFlow;
    }

    @Override
    public AddGroup addGroup() {
        return addGroup;
    }

    @Override
    public RemoveGroup removeGroup() {
        return removeGroup;
    }

    @Override
    public UpdateGroup updateGroup() {
        return updateGroup;
    }

    @Override
    public AddMeter addMeter() {
        return addMeter;
    }

    @Override
    public RemoveMeter removeMeter() {
        return removeMeter;
    }

    @Override
    public UpdateMeter updateMeter() {
        return updateMeter;
    }

    @Override
    public UpdateTable updateTable() {
        return updateTable;
    }

    @Override
    public ControlBundle controlBundle() {
        return controlBundle;
    }

    @Override
    public AddBundleMessages addBundleMessages() {
        return addBundleMessages;
    }

    @Override
    public GetActiveBundle getActiveBundle() {
        return getActiveBundle;
    }

    @Override
    public DevicesGroupRegistry getDevicesGroupRegistry() {
        return devicesGroupRegistry;
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
    public BundleMessagesCommiter<Flow> getBundleFlowListener() {
        return bundleFlowListener;
    }

    @Override
    public BundleMessagesCommiter<Group> getBundleGroupListener() {
        return bundleGroupListener;
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
    public void addRecoverableListener(final RecoverableListener recoverableListener) {
        serviceRecoveryRegistry.addRecoverableListener(openflowServiceRecoveryHandler.buildServiceRegistryKey(),
                recoverableListener);
    }

    @Override
    public FlowNodeConnectorInventoryTranslatorImpl getFlowNodeConnectorInventoryTranslatorImpl() {
        return flowNodeConnectorInventoryTranslatorImpl;
    }

    @Override
    public NodeConfigurator getNodeConfigurator() {
        return nodeConfigurator;
    }

    @Override
    public FlowNodeReconciliation getFlowNodeReconciliation() {
        return flowNodeReconciliation;
    }

    @Override
    public boolean isBundleBasedReconciliationEnabled() {
        return isBundleBasedReconciliationEnabled;
    }

    @Override
    public boolean isNodeOwner(final DataObjectIdentifier<FlowCapableNode> ident) {
        return ident != null && deviceMastershipManager.isDeviceMastered(ident.getFirstKeyOf(Node.class).getId());
    }

    @VisibleForTesting
    public void setDeviceMastershipManager(final DeviceMastershipManager deviceMastershipManager) {
        this.deviceMastershipManager = deviceMastershipManager;
    }

    @Override
    public void onPropertyChanged(final String propertyName, final String propertyValue) {
        final ForwardingRulesProperty forwardingRulesProperty = ForwardingRulesProperty.forValue(propertyName);
        if (forwardingRulesProperty != null) {
            switch (forwardingRulesProperty) {
                case DISABLE_RECONCILIATION:
                    disableReconciliation = Boolean.parseBoolean(propertyValue);
                    break;
                case STALE_MARKING_ENABLED:
                    staleMarkingEnabled = Boolean.parseBoolean(propertyValue);
                    break;
                case RECONCILIATION_RETRY_COUNT:
                    reconciliationRetryCount = Integer.parseInt(propertyValue);
                    break;
                case BUNDLE_BASED_RECONCILIATION_ENABLED:
                    isBundleBasedReconciliationEnabled = Boolean.parseBoolean(propertyValue);
                    break;
                default:
                    LOG.warn("No forwarding rule property found.");
                    break;
            }
        }
    }
}
