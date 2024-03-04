/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.util;

import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

/**
 * Basic {@link DataTreeModification} related tools.
 */
public final class ModificationUtil {
    private ModificationUtil() {
        // Hidden on purpose
    }

    public static String nodeIdValue(final DataTreeModification<Node> modification) {
        final NodeId nodeId = nodeId(modification);
        return nodeId == null ? null : nodeId.getValue();
    }

    public static NodeId nodeId(final DataTreeModification<Node> modification) {
        final DataObjectModification<Node> rootNode = modification.getRootNode();
        final Node dataAfter = rootNode.dataAfter();

        if (dataAfter != null) {
            return dataAfter.getId();
        }

        final Node dataBefore = rootNode.dataBefore();
        if (dataBefore != null) {
            return dataBefore.getId();
        }

        return null;
    }

    public static FlowCapableNode flowCapableNodeAfter(final DataTreeModification<Node> modification) {
        final Node dataAfter = modification.getRootNode().dataAfter();
        if (dataAfter == null) {
            return null;
        }
        return dataAfter.augmentation(FlowCapableNode.class);
    }
}
