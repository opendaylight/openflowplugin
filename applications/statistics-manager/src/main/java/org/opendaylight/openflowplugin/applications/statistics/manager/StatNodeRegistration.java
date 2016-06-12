/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * statistics-manager
 * org.opendaylight.openflowplugin.applications.statistics.manager.impl
 *
 * StatNodeRegistration
 * Class represents {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode}
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener} in Operational/DataStore for ADD / REMOVE
 * actions which are represented connect / disconnect OF actions. Connect functionality are expecting
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Sep 5, 2014
 */
public interface StatNodeRegistration extends OpendaylightInventoryListener, AutoCloseable {

    /**
     * Method contains {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode} registration to {@link StatisticsManager}
     * for permanently collecting statistics by {@link StatPermCollector} and
     * as a prevention to use a validation check to the Operational/DS for identify
     * connected {@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode}.
     *
     * @param keyIdent
     * @param data
     * @param nodeIdent
     */
    void connectFlowCapableNode(InstanceIdentifier<SwitchFeatures> keyIdent,
            SwitchFeatures data, InstanceIdentifier<Node> nodeIdent);

    /**
     * Method cut {@link Node} registration for {@link StatPermCollector}
     *
     * @param keyIdent
     */
    void disconnectFlowCapableNode(InstanceIdentifier<Node> keyIdent);

    /**
     * Method returns if *this* instance of the stats-manager is owner of the node
     * @param node Given Node
     * @return true if owner, else false
     */
    boolean isFlowCapableNodeOwner(NodeId node);
}
