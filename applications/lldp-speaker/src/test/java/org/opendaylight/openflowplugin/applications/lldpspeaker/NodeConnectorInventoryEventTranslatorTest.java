/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.lldpspeaker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType.DELETE;
import static org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType.SUBTREE_MODIFIED;
import static org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType.WRITE;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Tests for {@link NodeConnectorInventoryEventTranslator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorInventoryEventTranslatorTest {
    private static final InstanceIdentifier<NodeConnector> ID = TestUtils
            .createNodeConnectorId("openflow:1", "openflow:1:1");
    private static final InstanceIdentifier<FlowCapableNodeConnector> NODE_CONNECTOR_INSTANCE_IDENTIFIER = ID
            .augmentation(FlowCapableNodeConnector.class);
    private static final FlowCapableNodeConnector FLOW_CAPABLE_NODE_CONNECTOR = TestUtils
            .createFlowCapableNodeConnector().build();

    @Mock
    private NodeConnectorEventsObserver eventsObserver;
    @Mock
    private NodeConnectorEventsObserver eventsObserver2;

    private NodeConnectorInventoryEventTranslator translator;

    @Before
    public void setUp() {
        translator = new NodeConnectorInventoryEventTranslator(mock(DataBroker.class), eventsObserver, eventsObserver2);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorAdded} is called
     * for each FlowCapableNodeConnector item added in
     * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorCreation() {
        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                                                                        FLOW_CAPABLE_NODE_CONNECTOR);
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verify(eventsObserver).nodeConnectorAdded(ID, FLOW_CAPABLE_NODE_CONNECTOR);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in link down state.
     */
    @Test
    public void testNodeConnectorCreationLinkDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(true, false).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, ID, fcnc);
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verifyNoInteractions(eventsObserver);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in admin down state.
     */
    @Test
    public void testNodeConnectorCreationAdminDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(false, true).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, ID, fcnc);
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verifyNoInteractions(eventsObserver);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector item that have link down state removed in
     * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorUpdateToLinkDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(true, false).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(SUBTREE_MODIFIED,
                                                                        NODE_CONNECTOR_INSTANCE_IDENTIFIER, fcnc);
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verify(eventsObserver).nodeConnectorRemoved(ID);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector item with administrative down state removed in
     * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorUpdateToAdminDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(false, true).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(SUBTREE_MODIFIED,
                                                                        NODE_CONNECTOR_INSTANCE_IDENTIFIER, fcnc);
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verify(eventsObserver).nodeConnectorRemoved(ID);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorAdded} is called
     * for each FlowCapableNodeConnector item with administrative up and link up state added in
     * {@link org.opendaylight.md}.
     */
    @Test
    public void testNodeConnectorUpdateToUp() {
        DataTreeModification dataTreeModification = setupDataTreeChange(SUBTREE_MODIFIED,
                                                                        NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                                                                        FLOW_CAPABLE_NODE_CONNECTOR);
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verify(eventsObserver).nodeConnectorAdded(ID, FLOW_CAPABLE_NODE_CONNECTOR);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector path that
     * {@link org.opendaylight.mdsal.binding.api.DataTreeModification} return.
     */
    @Test
    public void testNodeConnectorRemoval() {
        DataTreeModification dataTreeModification = setupDataTreeChange(DELETE, NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                                                                        null);
        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataTreeChanged(List.of(dataTreeModification));
        verify(eventsObserver).nodeConnectorRemoved(ID);
    }

    /**
     * Test that if {@link NodeConnectorEventsObserver#nodeConnectorAdded} and.
     * @{NodeConnectorEventsObserver#nodeConnectorRemoved} are called for each observer when multiple
     * observers are registered for notifications.
     */
    @Test
    public void testMultipleObserversNotified() {
        // Create prerequisites
        InstanceIdentifier<NodeConnector> id2 = TestUtils.createNodeConnectorId("openflow:1", "openflow:1:2");
        InstanceIdentifier<FlowCapableNodeConnector> iiToConnector2 = id2.augmentation(FlowCapableNodeConnector.class);
        // Invoke onDataTreeChanged and check that both observers notified
        translator.onDataTreeChanged(List.of(
            setupDataTreeChange(WRITE, NODE_CONNECTOR_INSTANCE_IDENTIFIER, FLOW_CAPABLE_NODE_CONNECTOR),
            setupDataTreeChange(DELETE, iiToConnector2, null)));
        verify(eventsObserver).nodeConnectorAdded(ID, FLOW_CAPABLE_NODE_CONNECTOR);
        verify(eventsObserver).nodeConnectorRemoved(id2);
        verify(eventsObserver2).nodeConnectorAdded(ID, FLOW_CAPABLE_NODE_CONNECTOR);
        verify(eventsObserver2).nodeConnectorRemoved(id2);
    }

    @Test
    public void tearDown() {
        translator.close();
    }

    private static <T extends DataObject> DataTreeModification setupDataTreeChange(final ModificationType type,
            final InstanceIdentifier<T> ii, final FlowCapableNodeConnector connector) {
        final DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        when(dataTreeModification.getRootNode()).thenReturn(mock(DataObjectModification.class));
        DataTreeIdentifier<T> identifier = DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, ii);
        when(dataTreeModification.getRootNode().modificationType()).thenReturn(type);
        when(dataTreeModification.getRootPath()).thenReturn(identifier);
        when(dataTreeModification.getRootNode().dataAfter()).thenReturn(connector);
        return dataTreeModification;
    }
}
