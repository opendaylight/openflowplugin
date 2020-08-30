/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.mockito.Mockito;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.binding.dom.adapter.test.AbstractDataBrokerTest;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.applications.frm.impl.ListenerRegistrationHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.forwardingrules.manager.config.rev160511.ForwardingRulesManagerConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

public abstract class FRMTest extends AbstractDataBrokerTest {

    public void addFlowCapableNode(NodeKey nodeKey) {
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

    // TODO: remove with mdsal-3.0.7 or later
    @SuppressWarnings("unchecked")
    protected static final void assertCommit(FluentFuture<?> future) {
        assertCommit((ListenableFuture<Void>) future);
    }

    public void removeNode(NodeKey nodeKey) throws ExecutionException, InterruptedException {
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
        final ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        final ForwardingRulesManagerConfig config = getConfig();

        Mockito.when(configurationService.registerListener(Mockito.any())).thenReturn(() -> {
        });

        Mockito.lenient().when(configurationService.getProperty(Mockito.eq("disable-reconciliation"), Mockito.any()))
                .thenReturn(config.isDisableReconciliation());

        Mockito.lenient().when(configurationService.getProperty(Mockito.eq("stale-marking-enabled"), Mockito.any()))
                .thenReturn(config.isStaleMarkingEnabled());

        Mockito.lenient().when(configurationService.getProperty(Mockito.eq("reconciliation-retry-count"),
                Mockito.any())).thenReturn(config.getReconciliationRetryCount());

        Mockito.lenient().when(configurationService.getProperty(Mockito.eq("bundle-based-reconciliation-enabled"),
                Mockito.any())).thenReturn(config.isBundleBasedReconciliationEnabled());

        return configurationService;
    }

    protected Callable<Integer> listSize(List<?> list) {
        // The condition supplier part
        return list::size;
    }

    public ListenerRegistrationHelper getRegistrationHelper() {
        ListenerRegistrationHelper registrationHelper = new ListenerRegistrationHelper(getDataBroker());
        return registrationHelper;
    }
}
