/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * A network protocol's data (mutable).
 *
 * @author Frank Wood
 */
public interface ProtocolData {

    /**
     * Called to verify all protocol constrains are satisfied.
     *
     * @throws ProtocolException if a constraint is violated
     */
    void verify();
    
}
