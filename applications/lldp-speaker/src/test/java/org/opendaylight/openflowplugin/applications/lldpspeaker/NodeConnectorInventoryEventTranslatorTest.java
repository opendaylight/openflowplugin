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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.DELETE;
import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.SUBTREE_MODIFIED;
import static org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType.WRITE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


/**
 * Tests for {@link NodeConnectorInventoryEventTranslator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorInventoryEventTranslatorTest {
    private static final InstanceIdentifier<NodeConnector> id = TestUtils.createNodeConnectorId("openflow:1", "openflow:1:1");
    private static final InstanceIdentifier<FlowCapableNodeConnector> iiToConnector = id.augmentation(FlowCapableNodeConnector.class);
    private static final FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector().build();

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
     * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorCreation() {
        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, iiToConnector, fcnc);
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(eventsObserver).nodeConnectorAdded(id, fcnc);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in link down state.
     */
    @Test
    public void testNodeConnectorCreationLinkDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(true, false).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, id, fcnc);
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verifyZeroInteractions(eventsObserver);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in admin down state.
     */
    @Test
    public void testNodeConnectorCreationAdminDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(false, true).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(WRITE, id, fcnc);
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verifyZeroInteractions(eventsObserver);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector item that have link down state removed in
     * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorUpdateToLinkDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(true, false).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(SUBTREE_MODIFIED, iiToConnector, fcnc);
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(eventsObserver).nodeConnectorRemoved(id);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector item with administrative down state removed in
     * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorUpdateToAdminDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(false, true).build();
        DataTreeModification dataTreeModification = setupDataTreeChange(SUBTREE_MODIFIED, iiToConnector, fcnc);
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(eventsObserver).nodeConnectorRemoved(id);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorAdded} is called
     * for each FlowCapableNodeConnector item with administrative up and link up state added in
     * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification}.
     */
    @Test
    public void testNodeConnectorUpdateToUp() {
        DataTreeModification dataTreeModification = setupDataTreeChange(SUBTREE_MODIFIED, iiToConnector, fcnc);
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(eventsObserver).nodeConnectorAdded(id, fcnc);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector path that
     * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeModification} return.
     */
    @Test
    public void testNodeConnectorRemoval() {
        DataTreeModification dataTreeModification = setupDataTreeChange(DELETE, iiToConnector, null);
        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataTreeChanged(Collections.singleton(dataTreeModification));
        verify(eventsObserver).nodeConnectorRemoved(id);
    }

    /**
     * Test that checks if {@link NodeConnectorEventsObserver#nodeConnectorAdded} and
     * @{NodeConnectorEventsObserver#nodeConnectorRemoved} are called for each
     * observer when multiple observers are registered for notifications.
     */
    @Test
    public  void testMultipleObserversNotified() throws Exception {
        // Create prerequisites
        InstanceIdentifier<NodeConnector> id2 = TestUtils.createNodeConnectorId("openflow:1", "openflow:1:2");
        InstanceIdentifier<FlowCapableNodeConnector> iiToConnector2 = id2.augmentation(FlowCapableNodeConnector.class);
        List<DataTreeModification> modifications = new ArrayList();
        modifications.add(setupDataTreeChange(WRITE, iiToConnector, fcnc));
        modifications.add(setupDataTreeChange(DELETE, iiToConnector2, null));
        // Invoke onDataTreeChanged and check that both observers notified
        translator.onDataTreeChanged(modifications);
        verify(eventsObserver).nodeConnectorAdded(id, fcnc);
        verify(eventsObserver).nodeConnectorRemoved(id2);
        verify(eventsObserver2).nodeConnectorAdded(id, fcnc);
        verify(eventsObserver2).nodeConnectorRemoved(id2);
    }

    @Test
    public void tearDown() throws Exception {
        translator.close();
    }

    private <T extends DataObject> DataTreeModification setupDataTreeChange(final ModificationType type,
                                                                            final InstanceIdentifier<T> ii,
                                                                            final FlowCapableNodeConnector connector) {
        final DataTreeModification dataTreeModification = mock(DataTreeModification.class);
        when(dataTreeModification.getRootNode()).thenReturn(mock(DataObjectModification.class));
        DataTreeIdentifier<T> identifier = new DataTreeIdentifier(LogicalDatastoreType.OPERATIONAL, ii);
        when(dataTreeModification.getRootNode().getModificationType()).thenReturn(type);
        when(dataTreeModification.getRootPath()).thenReturn(identifier);
        when(dataTreeModification.getRootNode().getDataAfter()).thenReturn(connector);
        return dataTreeModification;

    }
}
