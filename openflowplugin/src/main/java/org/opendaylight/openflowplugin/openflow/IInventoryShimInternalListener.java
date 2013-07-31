
/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow;

import java.util.Set;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.UpdateType;

/**
 * The Interface provides inventory updates to inventory listeners within the
 * protocol plugin
 */
public interface IInventoryShimInternalListener {
    /**
     * Updates node and its properties
     *
     * @param node                      {@link org.opendaylight.controller.sal.core.Node} being updated
     * @param type              {@link org.opendaylight.controller.sal.core.UpdateType}
     * @param props             set of {@link org.opendaylight.controller.sal.core.Property} such as
     *                                          {@link org.opendaylight.controller.sal.core.Description} and/or
     *                                          {@link org.opendaylight.controller.sal.core.Tier} etc.
     */
    public void updateNode(Node node, UpdateType type, Set<Property> props);

    /**
     * Updates node connector and its properties
     *
     * @param nodeConnector {@link org.opendaylight.controller.sal.core.NodeConnector} being updated
     * @param type              {@link org.opendaylight.controller.sal.core.UpdateType}
     * @param props             set of {@link org.opendaylight.controller.sal.core.Property} such as
     *                                          {@link org.opendaylight.controller.sal.core.Description} and/or
     *                                          {@link org.opendaylight.controller.sal.core.State} etc.
     */
    public void updateNodeConnector(NodeConnector nodeConnector,
            UpdateType type, Set<Property> props);

}
