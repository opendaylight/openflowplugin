/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

@RunWith(MockitoJUnitRunner.class)
public abstract class DataTreeChangeListenerBase {

    private OperationProcessor processor;
    protected WithKey<Topology, TopologyKey> topologyIID;
    protected TerminationPointChangeListenerImpl terminationPointListener;
    protected NodeChangeListenerImpl nodeChangeListener;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);
    @Mock
    private DataBroker mockDataBroker;
    @Mock
    protected TransactionChain mockTxChain;

    @Before
    public void setUp() {
        doReturn(mockTxChain).when(mockDataBroker).createTransactionChain();

        processor = new OperationProcessor(mockDataBroker);

        topologyIID = DataObjectIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")))
                .build();
        terminationPointListener = new TerminationPointChangeListenerImpl(mockDataBroker, processor);
        nodeChangeListener = new NodeChangeListenerImpl(mockDataBroker, processor);

        executor.execute(processor);
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    protected FlowCapableNodeConnector provideFlowCapableNodeConnector(final boolean isLinkDown,
            final boolean isPortDown) {
        return new FlowCapableNodeConnectorBuilder()
            .setState(new StateBuilder().setLinkDown(isLinkDown).build())
            .setConfiguration(new PortConfig(true, true, true, isPortDown))
            .build();
    }

    protected <T extends DataObject> DataTreeModification<T> setupDataTreeChange(final ModificationType type,
            final DataObjectIdentifier<T> ii, final boolean getDataAfter) {
        final var dataTreeModification = mock(DataTreeModification.class);
        when(dataTreeModification.getRootNode()).thenReturn(mock(DataObjectModification.class));
        when(dataTreeModification.getRootNode().modificationType()).thenReturn(type);
        when(dataTreeModification.path()).thenReturn(ii);
        if (getDataAfter) {
            when(dataTreeModification.getRootNode().dataAfter()).thenReturn(mock(FlowCapableNodeConnector.class));
        }
        return dataTreeModification;
    }
}
