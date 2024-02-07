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
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
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
@Singleton
public final class ForwardingRulesManagerImpl implements ForwardingRulesManager, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerImpl.class);

    private static final int FRM_RECONCILIATION_PRIORITY = Integer.getInteger("frm.reconciliation.priority", 1);
    private static final String SERVICE_NAME = "FRM";

    private final AtomicLong txNum = new AtomicLong();
    private final DataBroker dataService;
    private final SalFlowService salFlowService;
    private final SalGroupService salGroupService;
    private final SalMeterService salMeterService;
    private final SalTableService salTableService;
    private final ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private final SalBundleService salBundleService;
    private final AutoCloseable configurationServiceRegistration;
    private ForwardingRulesCommiter<Flow> flowListener;
    private ForwardingRulesCommiter<Group> groupListener;
    private ForwardingRulesCommiter<Meter> meterListener;
    private ForwardingRulesCommiter<TableFeatures> tableListener;
    private final BundleMessagesCommiter<Flow> bundleFlowListener;
    private final BundleMessagesCommiter<Group> bundleGroupListener;
    private FlowNodeReconciliation nodeListener;
    private NotificationRegistration reconciliationNotificationRegistration;
    private final FlowNodeConnectorInventoryTranslatorImpl flowNodeConnectorInventoryTranslatorImpl;
    private DeviceMastershipManager deviceMastershipManager;
    private final DevicesGroupRegistry devicesGroupRegistry = new DevicesGroupRegistry();
    private final NodeConfigurator nodeConfigurator = new NodeConfiguratorImpl();
    private final ArbitratorReconcileService arbitratorReconciliationManager;
    private boolean disableReconciliation;
    private boolean staleMarkingEnabled;
    private int reconciliationRetryCount;
    private boolean isBundleBasedReconciliationEnabled;
    private final OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler;
    private final ServiceRecoveryRegistry serviceRecoveryRegistry;

    @Inject
    public ForwardingRulesManagerImpl(final DataBroker dataBroker,
                                      final RpcConsumerRegistry rpcRegistry,
                                      final RpcProviderService rpcProviderService,
                                      final ForwardingRulesManagerConfig config,
                                      final MastershipChangeServiceManager mastershipChangeServiceManager,
                                      final ClusterSingletonServiceProvider clusterSingletonService,
                                      final ConfigurationService configurationService,
                                      final ReconciliationManager reconciliationManager,
                                      final OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler,
                                      final ServiceRecoveryRegistry serviceRecoveryRegistry,
                                      final FlowGroupCacheManager flowGroupCacheManager,
                                      final ListenerRegistrationHelper registrationHelper) {
        disableReconciliation = config.getDisableReconciliation();
        staleMarkingEnabled = config.getStaleMarkingEnabled();
        reconciliationRetryCount = config.getReconciliationRetryCount().toJava();
        isBundleBasedReconciliationEnabled = config.getBundleBasedReconciliationEnabled();
        configurationServiceRegistration = configurationService.registerListener(this);
        dataService = requireNonNull(dataBroker);
        clusterSingletonServiceProvider = requireNonNull(clusterSingletonService);

        salFlowService = requireNonNull(rpcRegistry.getRpcService(SalFlowService.class),
                "RPC SalFlowService not found.");
        salGroupService = requireNonNull(rpcRegistry.getRpcService(SalGroupService.class),
                "RPC SalGroupService not found.");
        salMeterService = requireNonNull(rpcRegistry.getRpcService(SalMeterService.class),
                "RPC SalMeterService not found.");
        salTableService = requireNonNull(rpcRegistry.getRpcService(SalTableService.class),
                "RPC SalTableService not found.");
        salBundleService = requireNonNull(rpcRegistry.getRpcService(SalBundleService.class),
                "RPC SalBundlService not found.");
        this.openflowServiceRecoveryHandler = requireNonNull(openflowServiceRecoveryHandler,
                "Openflow service recovery handler cannot be null");
        this.serviceRecoveryRegistry = requireNonNull(serviceRecoveryRegistry,
                "Service recovery registry cannot be null");
        arbitratorReconciliationManager =
                requireNonNull(rpcRegistry.getRpcService(ArbitratorReconcileService.class),
                        "ArbitratorReconciliationManager can not be null!");

        nodeListener = new FlowNodeReconciliationImpl(this, dataService, SERVICE_NAME, FRM_RECONCILIATION_PRIORITY,
                ResultState.DONOTHING, flowGroupCacheManager);
        if (isReconciliationDisabled()) {
            LOG.debug("Reconciliation is disabled by user");
        } else {
            reconciliationNotificationRegistration = reconciliationManager.registerService(nodeListener);
            LOG.debug("Reconciliation is enabled by user and successfully registered to the reconciliation framework");
        }
        deviceMastershipManager = new DeviceMastershipManager(clusterSingletonServiceProvider, nodeListener,
                dataService, mastershipChangeServiceManager, rpcProviderService,
                new FrmReconciliationServiceImpl(this));
        flowNodeConnectorInventoryTranslatorImpl = new FlowNodeConnectorInventoryTranslatorImpl(dataService);

        bundleFlowListener = new BundleFlowForwarder(this);
        bundleGroupListener = new BundleGroupForwarder(this);
        flowListener = new FlowForwarder(this, dataService, registrationHelper);
        groupListener = new GroupForwarder(this, dataService, registrationHelper);
        meterListener = new MeterForwarder(this, dataService, registrationHelper);
        tableListener = new TableForwarder(this, dataService, registrationHelper);
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
    public ReadTransaction getReadTransaction() {
        return dataService.newReadOnlyTransaction();
    }

    @Override
    public String getNewTransactionId() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public boolean isNodeActive(final InstanceIdentifier<FlowCapableNode> ident) {
        return deviceMastershipManager.isNodeActive(ident.firstKeyOf(Node.class).getId());
    }

    @Override
    public boolean checkNodeInOperationalDataStore(final InstanceIdentifier<FlowCapableNode> ident) {
        boolean result = false;
        InstanceIdentifier<Node> nodeIid = ident.firstIdentifierOf(Node.class);
        try (ReadTransaction transaction = dataService.newReadOnlyTransaction()) {
            ListenableFuture<Optional<Node>> future = transaction
                .read(LogicalDatastoreType.OPERATIONAL, nodeIid);
            Optional<Node> optionalDataObject = future.get();
            if (optionalDataObject.isPresent()) {
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
    public DevicesGroupRegistry getDevicesGroupRegistry() {
        return devicesGroupRegistry;
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
    public BundleMessagesCommiter<Flow> getBundleFlowListener() {
        return bundleFlowListener;
    }

    @Override
    public BundleMessagesCommiter<Group> getBundleGroupListener() {
        return bundleGroupListener;
    }

    @Override
    public ArbitratorReconcileService getArbitratorReconciliationManager() {
        return arbitratorReconciliationManager;
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

    public FlowNodeReconciliation getNodeListener() {
        return nodeListener;
    }

    @Override
    public boolean isBundleBasedReconciliationEnabled() {
        return isBundleBasedReconciliationEnabled;
    }

    @Override
    public boolean isNodeOwner(final InstanceIdentifier<FlowCapableNode> ident) {
        return ident != null && deviceMastershipManager.isDeviceMastered(ident.firstKeyOf(Node.class).getId());
    }

    @VisibleForTesting
    public void setDeviceMastershipManager(final DeviceMastershipManager deviceMastershipManager) {
        this.deviceMastershipManager = deviceMastershipManager;
    }

    @Override
    public void onPropertyChanged(@NonNull final String propertyName, @NonNull final String propertyValue) {
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
