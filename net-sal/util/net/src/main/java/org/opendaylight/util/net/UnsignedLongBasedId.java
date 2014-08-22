/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;


/**
 * A base class for unsigned identifiers with a long payload.
 *
 * @author Simon Hunt
 */
public abstract class UnsignedLongBasedId extends UnsignedId {

    private static final long serialVersionUID = -2531647809442024248L;

    private static final String OX = "0x";
    /** The id.
     * @serial id
     */
    protected final long id;

    /** Constructs the id.
     *
     * @param id the id value
     */
    protected UnsignedLongBasedId(long id) {
        this.id = id;
    }

    /** Returns a string representation of this id. This
     * default implementation expresses the value as a hex
     * number, beginning with "0x...".
     *
     * @return the id, as a string
     */
    @Override
    public String toString() {
        return OX + Long.toHexString(id);
    }

    /** Returns this id as a long.
     *
     * @return this id as a long
     */
    public long toLong() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnsignedLongBasedId that = (UnsignedLongBasedId) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

}
