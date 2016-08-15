/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.lldpspeaker;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * NodeConnectorEventsObserver can be added to NodeConnectorInventoryEventTranslator to receive events
 * when node connector added or removed.
 */
public interface NodeConnectorEventsObserver {
    /**
     * This method is called when new node connector is added to inventory or when existing
     * node connector changed it's status to UP. This method can be called multiple times for
     * the same creation event.
     *
     * @param nodeConnectorInstanceId Object that uniquely identify added node connector
     * @param flowConnector object containing almost all of details about node connector
     */
    void nodeConnectorAdded(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId,
                                   FlowCapableNodeConnector flowConnector);

    /**
     * This method is called when some node connector is removed from inventory or when existing
     * node connector changed it's status to DOWN. This method can be called multiple times for
     * the same removal event.
     * @param nodeConnectorInstanceId Object that uniquely identify added node connector
     */
    void nodeConnectorRemoved(InstanceIdentifier<NodeConnector> nodeConnectorInstanceId);
}
