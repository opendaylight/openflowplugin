/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.AddBundleMessages;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.ControlBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.GetActiveBundle;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.UpdateTable;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class AbstractFRMTest extends AbstractDataBrokerTest {
    private ForwardingRulesManagerImpl forwardingRulesManager;

    @Mock
    private RpcService rpcConsumerRegistry;
    @Mock
    private RpcProviderService rpcProviderService;
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

    final CapturingAddFlow addFlow = new CapturingAddFlow();
    final CapturingRemoveFlow removeFlow = new CapturingRemoveFlow();
    final CapturingUpdateFlow updateFlow = new CapturingUpdateFlow();
    final CapturingAddGroup addGroup = new CapturingAddGroup();
    final CapturingRemoveGroup removeGroup = new CapturingRemoveGroup();
    final CapturingUpdateGroup updateGroup = new CapturingUpdateGroup();
    final CapturingAddMeter addMeter = new CapturingAddMeter();
    final CapturingRemoveMeter removeMeter = new CapturingRemoveMeter();
    final CapturingUpdateMeter updateMeter = new CapturingUpdateMeter();
    final CapturingUpdateTable updateTable = new CapturingUpdateTable();
    final CapturingControlBundle controlBundle = new CapturingControlBundle();
    final CapturingAddBundleMessages addBundleMessages = new CapturingAddBundleMessages();
    final CapturingGetActiveBundle getActiveBundle = new CapturingGetActiveBundle();

    protected void setUpForwardingRulesManager() {
        when(rpcConsumerRegistry.getRpc(AddFlow.class)).thenReturn(addFlow);
        when(rpcConsumerRegistry.getRpc(RemoveFlow.class)).thenReturn(removeFlow);
        when(rpcConsumerRegistry.getRpc(UpdateFlow.class)).thenReturn(updateFlow);
        when(rpcConsumerRegistry.getRpc(AddGroup.class)).thenReturn(addGroup);
        when(rpcConsumerRegistry.getRpc(RemoveGroup.class)).thenReturn(removeGroup);
        when(rpcConsumerRegistry.getRpc(UpdateGroup.class)).thenReturn(updateGroup);
        when(rpcConsumerRegistry.getRpc(AddMeter.class)).thenReturn(addMeter);
        when(rpcConsumerRegistry.getRpc(RemoveMeter.class)).thenReturn(removeMeter);
        when(rpcConsumerRegistry.getRpc(UpdateMeter.class)).thenReturn(updateMeter);
        when(rpcConsumerRegistry.getRpc(UpdateTable.class)).thenReturn(updateTable);
        when(rpcConsumerRegistry.getRpc(ControlBundle.class)).thenReturn(controlBundle);
        when(rpcConsumerRegistry.getRpc(AddBundleMessages.class)).thenReturn(addBundleMessages);
        when(rpcConsumerRegistry.getRpc(GetActiveBundle.class)).thenReturn(getActiveBundle);

        final var dataBroker = getDataBroker();
        forwardingRulesManager = new ForwardingRulesManagerImpl(getDataBroker(), rpcConsumerRegistry,
                rpcProviderService, getConfig(), mastershipChangeServiceManager, getConfigurationService(),
                reconciliationManager, openflowServiceRecoveryHandler, serviceRecoveryRegistry, flowGroupCacheManager);
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
}
