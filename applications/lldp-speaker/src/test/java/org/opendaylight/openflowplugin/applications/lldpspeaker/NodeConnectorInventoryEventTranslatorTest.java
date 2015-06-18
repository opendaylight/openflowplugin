/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


/**
 * Tests for @{NodeConnectorInventoryEventTranslator} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConnectorInventoryEventTranslatorTest {
    static InstanceIdentifier<NodeConnector> id = TestUtils.createNodeConnectorId("openflow:1", "openflow:1:1");
    static InstanceIdentifier<FlowCapableNodeConnector> iiToConnector = id.augmentation(FlowCapableNodeConnector.class);
    static FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector().build();

    @Mock DataBroker dataBroker;
    @Mock ListenerRegistration<DataChangeListener> dataChangeListenerRegistration;
    @Mock NodeConnectorEventsObserver eventsObserver;
    @Mock NodeConnectorEventsObserver eventsObserver2;

    MockDataChangedEvent dataChangedEvent = new MockDataChangedEvent();
    NodeConnectorInventoryEventTranslator translator;

    @Before
    public void setUp() {

        when(dataBroker.registerDataChangeListener(
                any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class),
                any(DataChangeListener.class),
                any(AsyncDataBroker.DataChangeScope.class)))
                .thenReturn(dataChangeListenerRegistration);
        translator = new NodeConnectorInventoryEventTranslator(dataBroker, eventsObserver, eventsObserver2);
    }

    /**
     * Test that checks if @{NodeConnectorEventsObserver#nodeConnectorAdded} is called
     * for each FlowCapableNodeConnector item that @{AsyncDataChangeEvent#getCreatedData} return.
     */
    @Test
    public void testNodeConnectorCreation() {
        // Setup dataChangedEvent to mock new port creation in inventory
        dataChangedEvent.created.put(iiToConnector, fcnc);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verify(eventsObserver).nodeConnectorAdded(id, fcnc);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in link down state.
     */
    @Test
    public void testNodeConnectorCreationLinkDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(true, false).build();

        // Setup dataChangedEvent to mock new port creation in inventory
        dataChangedEvent.created.put(id, fcnc);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(eventsObserver);
    }

    /**
     * Test that checks that nothing is called when port appeared in inventory in admin down state.
     */
    @Test
    public void testNodeConnectorCreationAdminDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(false, true).build();

        // Setup dataChangedEvent to mock new port creation in inventory
        dataChangedEvent.created.put(id, fcnc);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verifyZeroInteractions(eventsObserver);
    }

    /**
     * Test that checks if @{NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector item inside @{AsyncDataChangeEvent#getUpdatedData}
     * that have link down state.
     */
    @Test
    public void testNodeConnectorUpdateToLinkDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(true, false).build();

        // Setup dataChangedEvent to mock link down
        dataChangedEvent.updated.put(iiToConnector, fcnc);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verify(eventsObserver).nodeConnectorRemoved(id);
    }

    /**
     * Test that checks if @{NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector item inside @{AsyncDataChangeEvent#getUpdatedData}
     * that have administrative down state.
     */
    @Test
    public void testNodeConnectorUpdateToAdminDown() {
        FlowCapableNodeConnector fcnc = TestUtils.createFlowCapableNodeConnector(false, true).build();

        // Setup dataChangedEvent to mock link down and administrative port down
        dataChangedEvent.updated.put(iiToConnector, fcnc);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verify(eventsObserver).nodeConnectorRemoved(id);
    }

    /**
     * Test that checks if @{NodeConnectorEventsObserver#nodeConnectorAdded} is called
     * for each FlowCapableNodeConnector item inside @{AsyncDataChangeEvent#getUpdatedData}
     * that have administrative up and link up state.
     */
    @Test
    public void testNodeConnectorUpdateToUp() {
        // Setup dataChangedEvent to mock link up and administrative port up
        dataChangedEvent.updated.put(iiToConnector, fcnc);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verify(eventsObserver).nodeConnectorAdded(id, fcnc);
    }

    /**
     * Test that checks if @{NodeConnectorEventsObserver#nodeConnectorRemoved} is called
     * for each FlowCapableNodeConnector path that @{AsyncDataChangeEvent#getRemovedPaths} return.
     */
    @Test
    public void testNodeConnectorRemoval() {
        // Setup dataChangedEvent to mock node connector removal
        dataChangedEvent.removed.add(iiToConnector);

        // Invoke NodeConnectorInventoryEventTranslator and check result
        translator.onDataChanged(dataChangedEvent);
        verify(eventsObserver).nodeConnectorRemoved(id);
    }

    /**
     * Test that checks if @{NodeConnectorEventsObserver#nodeConnectorAdded} and
     * @{NodeConnectorEventsObserver#nodeConnectorRemoved} are called for each
     * observer when multiple observers are registered for notifications.
     */
    @Test
    public  void testMultipleObserversNotified() throws Exception {
        // Create prerequisites
        InstanceIdentifier<NodeConnector> id2 = TestUtils.createNodeConnectorId("openflow:1", "openflow:1:2");
        InstanceIdentifier<FlowCapableNodeConnector> iiToConnector2 = id2.augmentation(FlowCapableNodeConnector.class);

        // Setup dataChangedEvent to mock port creation and removal
        dataChangedEvent.created.put(iiToConnector, fcnc);
        dataChangedEvent.removed.add(iiToConnector2);

        // Invoke onDataChanged and check that both observers notified
        translator.onDataChanged(dataChangedEvent);
        verify(eventsObserver).nodeConnectorAdded(id, fcnc);
        verify(eventsObserver).nodeConnectorRemoved(id2);
        verify(eventsObserver2).nodeConnectorAdded(id, fcnc);
        verify(eventsObserver2).nodeConnectorRemoved(id2);
    }

    /**
     * Test that @{ListenerRegistration} is closed when ${NodeConnectorInventoryEventTranslator#close}
     * method is called.
     * @throws Exception
     */
    @Test
    public void testCleanup() throws Exception {
        // Trigger cleanup
        translator.close();

        // Verify that ListenerRegistration to DOM events
        verify(dataChangeListenerRegistration, times(2)).close();
    }

    static class MockDataChangedEvent implements AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> {
        Map<InstanceIdentifier<?>,DataObject> created = new HashMap<>();
        Map<InstanceIdentifier<?>,DataObject> updated = new HashMap<>();
        Set<InstanceIdentifier<?>> removed = new HashSet<>();

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getCreatedData() {
            return created;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getUpdatedData() {
            return updated;
        }

        @Override
        public Set<InstanceIdentifier<?>> getRemovedPaths() {
            return removed;
        }

        @Override
        public Map<InstanceIdentifier<?>, DataObject> getOriginalData() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }

        @Override
        public DataObject getOriginalSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }

        @Override
        public DataObject getUpdatedSubtree() {
            throw new UnsupportedOperationException("Not implemented by mock");
        }
    }
}
