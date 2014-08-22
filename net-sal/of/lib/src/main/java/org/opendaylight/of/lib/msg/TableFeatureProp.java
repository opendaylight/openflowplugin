/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.util.StringUtils;

/**
 * The abstract superclass of instances representing table feature properties.
 *
 * @author Simon Hunt
 */
public abstract class TableFeatureProp {

    /** The length of a table feature property header, in bytes. */
    static final int HEADER_LEN = 4;

    final Header header;

    /** Constructs a table feature property.
     *
     * @param header the property header
     */
    TableFeatureProp(Header header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return "{" + header.type + "}";
    }

    /** Returns the property type.
     *
     * @return the property type
     */
    public TableFeaturePropType getType() {
        return header.type;
    }

    /** Returns the length in bytes of the property when encoded.
     * <p>
     * This is really of interest only to the message encoder.
     *
     * @return the length in bytes
     */
    public abstract int getTotalLength();

    /** Returns a multi-line representation of this table feature property.
     *
     * @param indent the additional indent (number of spaces)
     * @return the multi-line debug string representation
     */
    String toDebugString(int indent) {
        final String ind = StringUtils.spaces(indent);
        return  ind + "Property Type : " + header.type;
    }

    /* Implementation note:
    *   we don't expose the length field, since that is an implementation
    *   detail that the consumer should not care about.
    */

    /** Table Feature Property Header. */
    static class Header {
        /** Type of property. */
        TableFeaturePropType type;
        /** Length of property (when encoded as byte array). */
        int length;
    }
}
