/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;


/**
 * A base class for unsigned identifiers with an integer payload.
 *
 * @author Simon Hunt
 */
public abstract class UnsignedIntBasedId extends UnsignedId {

    private static final long serialVersionUID = 4867071674669663910L;

    /** The id.
     * @serial id
     */
    protected final int id;

    /** Constructs the id.
     *
     * @param id the id value
     */
    protected UnsignedIntBasedId(int id) {
        this.id = id;
    }

    /** Returns a string representation of this id.
     *
     * @return the id, as a string
     */
    @Override
    public String toString() {
        return Integer.toString(id);
    }

    /** Returns this id as an int.
     *
     * @return this id as an int
     */
    public int toInt() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnsignedIntBasedId that = (UnsignedIntBasedId) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}
