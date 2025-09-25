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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectDeleted;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataObjectModified;
import org.opendaylight.mdsal.binding.api.DataObjectWritten;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

/**
 * Tests for {@link NodeConnectorInventoryEventTranslator}.
 */
@ExtendWith(MockitoExtension.class)
class NodeConnectorInventoryEventTranslatorTest {
    private static final WithKey<NodeConnector, NodeConnectorKey> ID =
        TestUtils.createNodeConnectorId("openflow:1", "openflow:1:1");
    private static final DataObjectIdentifier<FlowCapableNodeConnector> NODE_CONNECTOR_INSTANCE_IDENTIFIER =
        ID.toBuilder().augmentation(FlowCapableNodeConnector.class).build();
    private static final FlowCapableNodeConnector FLOW_CAPABLE_NODE_CONNECTOR =
        TestUtils.createFlowCapableNodeConnector().build();

    @Mock
    private NodeConnectorEventsObserver eventsObserver;
    @Mock
    private NodeConnectorEventsObserver eventsObserver2;
    @Mock
    private DataBroker dataBroker;

    private NodeConnectorInventoryEventTranslator translator;

    @BeforeEach
    void beforeEach() {
        translator = new NodeConnectorInventoryEventTranslator(dataBroker, eventsObserver, eventsObserver2);
    }

    @AfterEach
    void afterEach() {
        translator.close();
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorAdded} is called for each
     * {@link FlowCapableNodeConnector} item added in
     * {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
     */
    @Test
    void testNodeConnectorCreation() {
        translator.onDataTreeChanged(List.of(
            newWrite(NODE_CONNECTOR_INSTANCE_IDENTIFIER, FLOW_CAPABLE_NODE_CONNECTOR)));
        verify(eventsObserver).onNodeConnectorUp(ID, FLOW_CAPABLE_NODE_CONNECTOR);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in link down state.
     */
    @Test
    void testNodeConnectorCreationLinkDown() {
        translator.onDataTreeChanged(List.of(
            newWrite(NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                TestUtils.createFlowCapableNodeConnector(true, false).build())));
        verifyNoInteractions(eventsObserver);
        translator.onDataTreeChanged(List.of(newDeletion(NODE_CONNECTOR_INSTANCE_IDENTIFIER)));
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in admin down state.
     */
    @Test
    void testNodeConnectorCreationAdminDown() {
        translator.onDataTreeChanged(List.of(
            newWrite(NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                TestUtils.createFlowCapableNodeConnector(false, true).build())));
        verifyNoInteractions(eventsObserver);
        translator.onDataTreeChanged(List.of(newDeletion(NODE_CONNECTOR_INSTANCE_IDENTIFIER)));
    }

    /**
     * Test that checks that admin-down/status-up is not reported.
     */
    @Test
    void testNodeConnectorUpdateToLinkDown() {
        // FIXME| Sooo ... this is not what a DataBroker woult reprt as the first event:
        translator.onDataTreeChanged(List.of(
            newWrite(NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                TestUtils.createFlowCapableNodeConnector(true, false).build())));
        verifyNoInteractions(eventsObserver);

        translator.onDataTreeChanged(List.of(
            newModification(NODE_CONNECTOR_INSTANCE_IDENTIFIER, FLOW_CAPABLE_NODE_CONNECTOR)));
        verify(eventsObserver).onNodeConnectorUp(ID, FLOW_CAPABLE_NODE_CONNECTOR);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called for each
     * {@link FlowCapableNodeConnector} item with administrative down state removed in
     * a {@link org.opendaylight.mdsal.binding.api.DataTreeModification}.
     */
    @Test
    void testNodeConnectorUpdateToAdminDown() {
        translator.onDataTreeChanged(List.of(
            newWrite(NODE_CONNECTOR_INSTANCE_IDENTIFIER, FLOW_CAPABLE_NODE_CONNECTOR)));
        verify(eventsObserver).onNodeConnectorUp(ID, FLOW_CAPABLE_NODE_CONNECTOR);

        translator.onDataTreeChanged(List.of(
            newModification(NODE_CONNECTOR_INSTANCE_IDENTIFIER,
                TestUtils.createFlowCapableNodeConnector(false, true).build())));
        verify(eventsObserver).onNodeConnectorDown(ID);
    }

    /**
     * Test that if {@link NodeConnectorEventsObserver#nodeConnectorAdded} and
     * {@link NodeConnectorEventsObserver#nodeConnectorRemoved} are called for each observer when multiple
     * observers are registered for notifications.
     */
    @Test
    void testMultipleObserversNotified() {
        // Invoke onDataTreeChanged and check that both observers notified
        translator.onDataTreeChanged(List.of(
            newWrite(NODE_CONNECTOR_INSTANCE_IDENTIFIER, FLOW_CAPABLE_NODE_CONNECTOR)));
        verify(eventsObserver).onNodeConnectorUp(ID, FLOW_CAPABLE_NODE_CONNECTOR);
        verify(eventsObserver2).onNodeConnectorUp(ID, FLOW_CAPABLE_NODE_CONNECTOR);

        translator.onDataTreeChanged(List.of(newDeletion(NODE_CONNECTOR_INSTANCE_IDENTIFIER)));
        verify(eventsObserver).onNodeConnectorDown(ID);
        verify(eventsObserver2).onNodeConnectorDown(ID);
    }

    private static DataTreeModification<FlowCapableNodeConnector> newDeletion(
            final DataObjectIdentifier<FlowCapableNodeConnector> ii) {
        final DataObjectDeleted<FlowCapableNodeConnector> root = mock();
        return setupDataTreeChange(root, DELETE, ii);
    }

    private static DataTreeModification<FlowCapableNodeConnector> newModification(
            final DataObjectIdentifier<FlowCapableNodeConnector> ii, final FlowCapableNodeConnector connector) {
        final DataObjectModified<FlowCapableNodeConnector> root = mock();
        return setupWithDataAfter(root, SUBTREE_MODIFIED, ii, connector);
    }

    private static DataTreeModification<FlowCapableNodeConnector> newWrite(
            final DataObjectIdentifier<FlowCapableNodeConnector> ii, final FlowCapableNodeConnector connector) {
        final DataObjectWritten<FlowCapableNodeConnector> root = mock();
        return setupWithDataAfter(root, WRITE, ii, connector);
    }

    private static DataTreeModification<FlowCapableNodeConnector> setupWithDataAfter(
            final DataObjectModification.WithDataAfter<FlowCapableNodeConnector> root, final ModificationType type,
            final DataObjectIdentifier<FlowCapableNodeConnector> ii, final FlowCapableNodeConnector connector) {
        final var ret = setupDataTreeChange(root, type, ii);
        when(root.dataAfter()).thenReturn(connector);
        return ret;
    }

    private static DataTreeModification<FlowCapableNodeConnector> setupDataTreeChange(
            final DataObjectModification<FlowCapableNodeConnector> root, final ModificationType type,
            final DataObjectIdentifier<FlowCapableNodeConnector> ii) {
        when(root.modificationType()).thenReturn(type);

        final DataTreeModification<FlowCapableNodeConnector> dataTreeModification = mock();
        when(dataTreeModification.getRootNode()).thenReturn(root);
        when(dataTreeModification.path()).thenReturn(ii);
        return dataTreeModification;
    }
}
