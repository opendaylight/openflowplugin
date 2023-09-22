/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class AbstractFRMTest extends AbstractDataBrokerTest {
    private ForwardingRulesManagerImpl forwardingRulesManager;

    @Mock
    private RpcConsumerRegistry rpcConsumerRegistry;
    @Mock
    private RpcProviderService rpcProviderService;
    @Mock
    private ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    private DeviceMastershipManager deviceMastershipManager;
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

    protected ForwardingRulesManagerImpl getForwardingRulesManager() {
        return forwardingRulesManager;
    }

    protected void addFlowCapableNode(final NodeKey nodeKey) {
        Nodes nodes = new NodesBuilder()
            .setNode(BindingMap.of(new NodeBuilder()
                .withKey(nodeKey)
                .addAugmentation(new FlowCapableNodeBuilder().setDescription("test node").build())
                .build()))
            .build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class), nodes);
        assertCommit(writeTx.commit());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(Nodes.class), nodes);
        assertCommit(writeTx.commit());
    }

    private static ForwardingRulesManagerConfig getConfig() {
        return new ForwardingRulesManagerConfigBuilder()
                .setDisableReconciliation(false)
                .setStaleMarkingEnabled(false)
                .setReconciliationRetryCount(Uint16.ZERO)
                .setBundleBasedReconciliationEnabled(false)
                .build();
    }

    private static ConfigurationService getConfigurationService() {
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

    private ListenerRegistrationHelper getRegistrationHelper() {
        return new ListenerRegistrationHelper(getDataBroker());
    }
}
