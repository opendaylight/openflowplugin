/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

/**
 * Represents a property of a {@link Queue}.
 *
 * @author Simon Hunt
 */
public abstract class QueueProperty {
    final Header header;

    /** Constructor invoked by QueueFactory.
     *
     * @param header the queue property header
     */
    QueueProperty(Header header) {
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
    public QueuePropType getType() {
        return header.type;
    }

    /* Implementation note:
     *   we don't expose the length field, since that is an implementation
     *   detail that the consumer should not care about.
     */

    /** Queue Property Header. */
    static class Header {
        /** Type of property. */
        QueuePropType type;
        /** Length of property (when encoded as byte array). */
        int length;
    }
}
