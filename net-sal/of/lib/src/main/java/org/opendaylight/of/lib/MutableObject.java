/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Implemented by OpenFlow classes that provide mutators for setting state.
 *
 * @author Simon Hunt
 */
public interface MutableObject {

    /** Returns true if this mutable object is still writable. That is,
     * the {@code #toImmutable()} method (defined on a sub-interface)
     * has not yet been invoked.
     *
     * @return true if this mutable object is still writable
     */
    boolean writable();

}
