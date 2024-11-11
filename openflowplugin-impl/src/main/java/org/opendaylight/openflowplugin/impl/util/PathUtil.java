/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Purpose: utility class providing path and {@link InstanceIdentifier} tools.
 */
public final class PathUtil {
    private PathUtil() {
        // Hidden on purpose
    }

    /**
     * Extracts node id from instance identifier.
     * @param input instance identifier
     * @return node-id from given instance identifier
     * @deprecated Use {@link DataObjectIdentifier.WithKey#key()} instead
     */
    @Deprecated
    public static NodeId extractNodeId(final InstanceIdentifier<Node> input) {
        return input.firstKeyOf(Node.class).getId();
    }

    /**
     * Extracts node id from node reference.
     * @param input reference to {@link Node}
     * @return node-id from given reference
     */
    public static NodeId extractNodeId(final NodeRef input) {
        return ((DataObjectIdentifier<?>) input.getValue()).toLegacy().firstKeyOf(Node.class).getId();
    }
}
