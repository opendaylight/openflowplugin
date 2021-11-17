/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.mockito.Mock;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.impl.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.openflowplugin.applications.frm.impl.ListenerRegistrationHelper;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.SalBundleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class FRMTest extends AbstractDataBrokerTest {
    private ForwardingRulesManagerImpl forwardingRulesManager;

    @Mock
    RpcConsumerRegistry rpcConsumerRegistry;
    @Mock
    RpcProviderService rpcProviderService;
    @Mock
    ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    DeviceMastershipManager deviceMastershipManager;
    @Mock
    private ReconciliationManager reconciliationManager;
    @Mock
    private OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler;
    @Mock
    private ServiceRecoveryRegistry serviceRecoveryRegistry;
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;
    @Mock
    private FlowGroupCacheManager flowGroupCacheManager;

    protected void setUpForwardingRulesManager() {
        when(rpcConsumerRegistry.getRpcService(SalFlowService.class))
                .thenReturn(new SalFlowServiceMock());
        when(rpcConsumerRegistry.getRpcService(SalGroupService.class))
                .thenReturn(new SalGroupServiceMock());
        when(rpcConsumerRegistry.getRpcService(SalMeterService.class))
                .thenReturn(new SalMeterServiceMock());
        when(rpcConsumerRegistry.getRpcService(SalTableService.class))
                .thenReturn(new SalTableServiceMock());
        when(rpcConsumerRegistry.getRpcService(SalBundleService.class))
                .thenReturn(new SalBundleServiceMock());
        when(rpcConsumerRegistry.getRpcService(ArbitratorReconcileService.class))
                .thenReturn(new ArbitratorReconcileServiceMock());

        forwardingRulesManager = new ForwardingRulesManagerImpl(getDataBroker(), rpcConsumerRegistry,
                rpcProviderService, getConfig(), mastershipChangeServiceManager, clusterSingletonService,
                getConfigurationService(), reconciliationManager, openflowServiceRecoveryHandler,
                serviceRecoveryRegistry, flowGroupCacheManager, getRegistrationHelper());
        forwardingRulesManager.start();
    }

    protected void setDeviceMastership(final NodeId nodeId) {
        // TODO consider tests rewrite (added because of complicated access)
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        when(deviceMastershipManager.isDeviceMastered(nodeId)).thenReturn(true);
    }

    protected final ForwardingRulesManagerImpl getForwardingRulesManager() {
        return forwardingRulesManager;
    }

    public void addFlowCapableNode(final NodeKey nodeKey) {
        Nodes nodes = new NodesBuilder().build();

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.withKey(nodeKey);
        nodeBuilder.addAugmentation(new FlowCapableNodeBuilder().build());

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class), nodes);

        InstanceIdentifier<Node> flowNodeIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey);
        writeTx.put(LogicalDatastoreType.OPERATIONAL, flowNodeIdentifier, nodeBuilder.build());
        writeTx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Nodes.class), nodes);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, flowNodeIdentifier, nodeBuilder.build());
        assertCommit(writeTx.commit());
    }

    public void removeNode(final NodeKey nodeKey) throws ExecutionException, InterruptedException {
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.OPERATIONAL,
                InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey));
        writeTx.commit().get();
    }

    public void addTable(final TableKey tableKey, final NodeKey nodeKey) {
        addFlowCapableNode(nodeKey);
        final Table table = new TableBuilder().withKey(tableKey).build();
        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        InstanceIdentifier<Table> tableII = InstanceIdentifier.create(Nodes.class).child(Node.class, nodeKey)
                .augmentation(FlowCapableNode.class).child(Table.class, tableKey);
        writeTx.put(LogicalDatastoreType.CONFIGURATION, tableII, table);
        assertCommit(writeTx.commit());
    }

    public ForwardingRulesManagerConfig getConfig() {
        return new ForwardingRulesManagerConfigBuilder()
                .setDisableReconciliation(false)
                .setStaleMarkingEnabled(false)
                .setReconciliationRetryCount(Uint16.ZERO)
                .setBundleBasedReconciliationEnabled(false)
                .build();
    }

    public ConfigurationService getConfigurationService() {
        final ConfigurationService configurationService = mock(ConfigurationService.class);
        final ForwardingRulesManagerConfig config = getConfig();

        when(configurationService.registerListener(any())).thenReturn(() -> {
        });

        lenient().when(configurationService.getProperty(eq("disable-reconciliation"), any()))
                .thenReturn(config.getDisableReconciliation());

        lenient().when(configurationService.getProperty(eq("stale-marking-enabled"), any()))
                .thenReturn(config.getStaleMarkingEnabled());

        lenient().when(configurationService.getProperty(eq("reconciliation-retry-count"),
                any())).thenReturn(config.getReconciliationRetryCount());

        lenient().when(configurationService.getProperty(eq("bundle-based-reconciliation-enabled"),
                any())).thenReturn(config.getBundleBasedReconciliationEnabled());

        return configurationService;
    }

    protected Callable<Integer> listSize(final List<?> list) {
        // The condition supplier part
        return list::size;
    }

    public ListenerRegistrationHelper getRegistrationHelper() {
        return new ListenerRegistrationHelper(getDataBroker());
    }
}
