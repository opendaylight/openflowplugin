/*
 * Copyright (c) 2014 Pacnet and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.lldpspeaker;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;

/**
 * An observer of {@link NodeConnector}s changing their logical state.
 */
public interface NodeConnectorEventsObserver {
    /**
     * This method is called when new node connector is added to inventory or when existing node connector changed
     * its status to UP.
     *
     * @param nodeConnectorInstanceId Object that uniquely identify added node connector
     * @param flowConnector object containing almost all of details about node connector
     */
    void onNodeConnectorUp(@NonNull WithKey<NodeConnector, NodeConnectorKey> nodeConnectorInstanceId,
        @NonNull FlowCapableNodeConnector flowConnector);

    /**
     * This method is called when some node connector is removed from inventory or when existing node connector changed
     * its status to DOWN.
     *
     * @param nodeConnectorInstanceId Object that uniquely identify added node connector
     */
    void onNodeConnectorDown(@NonNull WithKey<NodeConnector, NodeConnectorKey> nodeConnectorInstanceId);
}
