/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;

/**
 * keeps mapping between port number (protocol based) and {@link NodeConnectorRef}
 */
public interface PortNumberCache {

    /**
     * @param portNumber
     * @return corresponding nodeConnectorRef if present
     */
    @Nullable
    NodeConnectorRef lookupNodeConnectorRef(Long portNumber);

    /**
     * @param portNumber       protocol port number
     * @param nodeConnectorRef corresponding value of {@link NodeConnectorRef}
     */
    void storeNodeConnectorRef(@Nonnull Long portNumber, @Nonnull NodeConnectorRef nodeConnectorRef);
}
