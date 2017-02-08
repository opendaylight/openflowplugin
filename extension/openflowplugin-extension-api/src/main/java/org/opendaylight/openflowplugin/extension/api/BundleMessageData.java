/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;

/**
 * Handle additional data for bundle meassage
 */
public interface BundleMessageData {
    /**
     *
     * @param xid
     */
    void setXid(long xid);

    /**
     *
     * @param node
     */
    void setNode(NodeRef node);

}
