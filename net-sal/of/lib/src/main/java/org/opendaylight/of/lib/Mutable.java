/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

/**
 * Encapsulates the common state and behavior of mutable objects.
 *
 * @author Simon Hunt
 */
public final class Mutable {

    private boolean writable = true;

    /** Checks that this mutable is still writable. If it is not, throw
     * an exception, using the toString() of the specified enclosing object
     * as the exception detail message.
     *
     * @param mo the enclosing mutable object
     */
    public void checkWritable(MutableObject mo) {
        if (!writable)
            throw new InvalidMutableException(mo.toString());
    }

    /** Invalidates this mutable, marking it forever unwritable.
     *
     * @param mo the enclosing mutable object
     */
    public void invalidate(MutableObject mo) {
        checkWritable(mo);
        writable = false;
    }

    /** Produces a tagged string, showing the writable state. The assumption
     * is that the parameter is a toString() of an object, which begins with
     * the following format:
     * <pre>
     *     {tag:...}
     * </pre>
     * where {@code tag} is a short (typically 3 character) tag identifying
     * the base class. For example, {@code "ofm"} for openflow message, or
     * {@code "ofs"} for openflow structure.
     *
     * @param s the original string
     * @return the tagged string
     */
    public String tagString(String s) {
        StringBuilder sb = new StringBuilder(s);
        int ci = s.indexOf(':');
        if (ci > 0)
            sb.replace(ci, ci, writable ? "-W" : "-X");
        return sb.toString();
    }

    /** Returns true if this mutable is still writable.
     *
     * @return true if writable
     */
    public boolean writable() {
        return writable;
    }

    @Override
    public String toString() {
        return writable ? "{W}" : "{X}";
    }
}
