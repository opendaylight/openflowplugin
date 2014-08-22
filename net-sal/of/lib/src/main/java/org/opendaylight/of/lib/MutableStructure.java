/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib;

/**
 * Implemented by OpenFlow structure classes that provide mutators for
 * setting the state of the structure.
 *
 * @author Simon Hunt
 */
public interface MutableStructure extends MutableObject, Structure {

    /**
     * Returns an immutable instance of this structure. In essence, converts
     * this structure to be read-only.
     * <p>
     * It is expected that the reference to this mutable structure will be
     * dropped. Note that <em>all</em> method calls invoked on a
     * {@code MutableStructure} after {@code toImmutable()} has been invoked
     * will result in an {@link InvalidMutableException} being thrown.
     *
     * @return an immutable instance of this structure.
     * @throws InvalidMutableException if this structure is no longer writable
     */
    OpenflowStructure toImmutable();

}
