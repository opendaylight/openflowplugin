/**
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.FrmReconciliationService;
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
public class ForwardingRulesManagerImpl implements ForwardingRulesManager {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardingRulesManagerImpl.class);

    static final int STARTUP_LOOP_TICK = 500;
    static final int STARTUP_LOOP_MAX_RETRIES = 8;
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
    private final MastershipChangeServiceManager mastershipChangeServiceManager;
    private final RpcProviderRegistry rpcRegistry;
    private ForwardingRulesCommiter<Flow> flowListener;
    private ForwardingRulesCommiter<Group> groupListener;
    private ForwardingRulesCommiter<Meter> meterListener;
    private ForwardingRulesCommiter<TableFeatures> tableListener;
    private FlowNodeReconciliation nodeListener;
    private NotificationRegistration reconciliationNotificationRegistration;
    private FlowNodeConnectorInventoryTranslatorImpl flowNodeConnectorInventoryTranslatorImpl;
    private DeviceMastershipManager deviceMastershipManager;
    private final ReconciliationManager reconciliationManager;
    private DevicesGroupRegistry devicesGroupRegistry;
    private NodeConfigurator nodeConfigurator;
    private ArbitratorReconcileService arbitratorReconciliationManager;
    private boolean disableReconciliation;
    private boolean staleMarkingEnabled;
    private int reconciliationRetryCount;
    private boolean isBundleBasedReconciliationEnabled;
    private final OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler;
    private final ServiceRecoveryRegistry serviceRecoveryRegistry;

    public ForwardingRulesManagerImpl(final DataBroker dataBroker, final RpcProviderRegistry rpcRegistry,
                                      final ForwardingRulesManagerConfig config,
                                      final MastershipChangeServiceManager mastershipChangeServiceManager,
                                      final ClusterSingletonServiceProvider clusterSingletonService,
                                      final ConfigurationService configurationService,
                                      final ReconciliationManager reconciliationManager,
                                      final OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler,
                                      final ServiceRecoveryRegistry serviceRecoveryRegistry) {
        disableReconciliation = config.isDisableReconciliation();
        staleMarkingEnabled = config.isStaleMarkingEnabled();
        reconciliationRetryCount = config.getReconciliationRetryCount();
        isBundleBasedReconciliationEnabled = config.isBundleBasedReconciliationEnabled();
        this.configurationServiceRegistration = configurationService.registerListener(this);
        this.dataService = Preconditions.checkNotNull(dataBroker, "DataBroker can not be null!");
        this.clusterSingletonServiceProvider = Preconditions.checkNotNull(clusterSingletonService,
                "ClusterSingletonService provider can not be null");
        this.reconciliationManager = reconciliationManager;
        this.rpcRegistry = rpcRegistry;
        this.mastershipChangeServiceManager = mastershipChangeServiceManager;

        Preconditions.checkArgument(rpcRegistry != null, "RpcProviderRegistry can not be null !");

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
        this.openflowServiceRecoveryHandler = Preconditions.checkNotNull(openflowServiceRecoveryHandler,
                "Openflow service recovery handler cannot be null");
        this.serviceRecoveryRegistry = Preconditions.checkNotNull(serviceRecoveryRegistry,
                "Service recovery registry cannot be null");
        this.arbitratorReconciliationManager = Preconditions
                .checkNotNull(rpcRegistry.getRpcService(ArbitratorReconcileService.class),
                        "ArbitratorReconciliationManager can not be null!");
    }

    @Override
    public void start() {
        nodeConfigurator = new NodeConfiguratorImpl();
        this.devicesGroupRegistry = new DevicesGroupRegistry();

        this.nodeListener = new FlowNodeReconciliationImpl(this, dataService, SERVICE_NAME, FRM_RECONCILIATION_PRIORITY,
                ResultState.DONOTHING);
        if (this.isReconciliationDisabled()) {
            LOG.debug("Reconciliation is disabled by user");
        } else {
            this.reconciliationNotificationRegistration = reconciliationManager.registerService(this.nodeListener);
            LOG.debug("Reconciliation is enabled by user and successfully registered to the reconciliation framework");
        }
        this.deviceMastershipManager = new DeviceMastershipManager(clusterSingletonServiceProvider, this.nodeListener,
                dataService, mastershipChangeServiceManager);
        this.deviceMastershipManager.setRoutedRpcReg(rpcRegistry.addRoutedRpcImplementation(
                FrmReconciliationService.class, new FrmReconciliationServiceImpl(this)));
        flowNodeConnectorInventoryTranslatorImpl = new FlowNodeConnectorInventoryTranslatorImpl(dataService);

        this.flowListener = new FlowForwarder(this, dataService);
        this.groupListener = new GroupForwarder(this, dataService);
        this.meterListener = new MeterForwarder(this, dataService);
        this.tableListener = new TableForwarder(this, dataService);
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
    public String getNewTransactionId() {
        return "DOM-" + txNum.getAndIncrement();
    }

    @Override
    public boolean isNodeActive(InstanceIdentifier<FlowCapableNode> ident) {
        return deviceMastershipManager.isNodeActive(ident.firstKeyOf(Node.class).getId());
    }

    @Override
    public boolean checkNodeInOperationalDataStore(InstanceIdentifier<FlowCapableNode> ident) {
        boolean result = false;
        InstanceIdentifier<Node> nodeIid = ident.firstIdentifierOf(Node.class);
        try (ReadOnlyTransaction transaction = dataService.newReadOnlyTransaction()) {
            ListenableFuture<com.google.common.base.Optional<Node>> future = transaction
                .read(LogicalDatastoreType.OPERATIONAL, nodeIid);
            com.google.common.base.Optional<Node> optionalDataObject = future.get();
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
        return this.devicesGroupRegistry;
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
    public void addRecoverableListener(RecoverableListener recoverableListener) {
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
    public boolean isNodeOwner(InstanceIdentifier<FlowCapableNode> ident) {
        return Objects.nonNull(ident) && deviceMastershipManager.isDeviceMastered(ident.firstKeyOf(Node.class).getId());
    }

    @VisibleForTesting
    public void setDeviceMastershipManager(final DeviceMastershipManager deviceMastershipManager) {
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
                    reconciliationRetryCount = Integer.parseInt(propertyValue);
                    break;
                case BUNDLE_BASED_RECONCILIATION_ENABLED:
                    isBundleBasedReconciliationEnabled = Boolean.valueOf(propertyValue);
                    break;
                default:
                    LOG.warn("No forwarding rule property found.");
                    break;
            }
        });
    }
}
