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
 * Interface for bundle data injection
 */
public interface BundleMessageDataInjector {
    /**
     * Set xid
     * @param xid request id
     */
    void setXid(long xid);

    /**
     * Set node id
     * @param node node id
     */
    void setNode(NodeRef node);

}
