/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.manager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.flow.capable.port.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author joe
 *
 */
public abstract class DataChangeListenerBase {

    @Mock
    private DataBroker mockDataBroker;

    @Mock
    protected BindingTransactionChain mockTxChain;

    @Mock
    protected AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> mockedDataChangeListener;

    private OperationProcessor processor;

    protected InstanceIdentifier<Topology> topologyIID;

    protected TerminationPointChangeListenerImpl terminationPointListener;
    protected NodeChangeListenerImpl nodeChangeListener;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        doReturn(mockTxChain).when(mockDataBroker)
                .createTransactionChain(any(TransactionChainListener.class));

        processor = new OperationProcessor(mockDataBroker);

        topologyIID = InstanceIdentifier.create(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1")));
        terminationPointListener = new TerminationPointChangeListenerImpl(mockDataBroker, processor);
        nodeChangeListener = new NodeChangeListenerImpl(mockDataBroker, processor);

        executor.execute(processor);
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    protected void mockDataChangeListener(Map<InstanceIdentifier<?>,DataObject> createdData, Map<InstanceIdentifier<?>, DataObject> updatedData, Set<?> removedPaths) {
        doReturn(createdData == null ? Collections.emptyMap() : createdData).when(mockedDataChangeListener).getCreatedData();
        doReturn(updatedData == null ? Collections.emptyMap() : updatedData).when(mockedDataChangeListener).getUpdatedData();
        doReturn(removedPaths == null ? Collections.emptySet() : removedPaths).when(mockedDataChangeListener).getRemovedPaths();
    }

    protected FlowCapableNodeConnector provideFlowCapableNodeConnector(final boolean isLinkDown, final boolean isPortDown) {
        FlowCapableNodeConnectorBuilder builder = new FlowCapableNodeConnectorBuilder();
        builder.setState(new StateBuilder().setLinkDown(isLinkDown).build());
        builder.setConfiguration(new PortConfig(true, true, true, isPortDown));
        return builder.build();
    }
}
