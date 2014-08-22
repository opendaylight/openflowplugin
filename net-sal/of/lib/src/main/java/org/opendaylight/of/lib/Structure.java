/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib;

/**
 * Provides access to the attributes common to all {@link OpenflowStructure}s.
 *
 * @author Simon Hunt
 */
public interface Structure {

    /** Returns the protocol version of this OpenFlow structure.
     *
     * @return the structure protocol version
     */
    ProtocolVersion getVersion();

    /** Returns a multi-line string representation of this structure.
     *
     * @return a multi-line string representation
     */
    String toDebugString();

}
