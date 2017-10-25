/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Basic {@link InstanceIdentifier} related tools.
 */
public class PathUtil {

    private PathUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }
    public static NodeId digNodeId(final InstanceIdentifier<?> nodeIdent) {
        return nodeIdent.firstKeyOf(Node.class, NodeKey.class).getId();
    }

    public static InstanceIdentifier<Node> digNodePath(final InstanceIdentifier<FlowCapableNode> nodeIdent) {
        return nodeIdent.firstIdentifierOf(Node.class);
    }
}
