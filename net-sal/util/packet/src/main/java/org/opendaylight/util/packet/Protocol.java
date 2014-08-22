/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * A network protocol (immutable).
 *
 * @author Frank Wood
 */
public interface Protocol {
    
    /**
     * Returns the protocol ID.
     * 
     * @return protocol ID
     */
    ProtocolId id();

    
    /**
     * Returns the protocol debug string.
     * 
     * @return the protocol debug string
     */
    String toDebugString();

}
