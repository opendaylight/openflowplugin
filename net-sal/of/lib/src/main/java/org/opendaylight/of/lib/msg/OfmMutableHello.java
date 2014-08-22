/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.ArrayList;

/**
 * Mutable subclass of {@link OfmHello}.
 *
 * @author Simon Hunt
 */
public class OfmMutableHello extends OfmHello implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /** Constructs a mutable OpenFlow HELLO message.
     *
     * @param header the header
     */
    OfmMutableHello(Header header) {
        super(header);
    }

    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Copy over to read-only instance
        OfmHello msg = new OfmHello(header);
        msg.elements = this.elements;
        return msg;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }


    // =====================================================================
    // ==== SETTERS

    /** Adds the specified hello element to this HELLO message.
     *
     * @param element the element to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if element is null
     */
    public OfmMutableHello addElement(HelloElement element) {
        // Implementation note: lazy initialization of the elements list
        // We don't want to init in the constructor, as we may never add
        //  an element, for older style HELLO messages.
        if (elements == null)
            elements = new ArrayList<HelloElement>();
        elements.add(element);
        header.length += element.getTotalLength();

        if (HelloElemVersionBitmap.class.isInstance(element)) {
            ProtocolVersion pv = header.version;
            ProtocolVersion ev = element.getVersion();
            if (pv != ev)
                throw new IllegalArgumentException(pv +
                        E_WRONG_VER_IN_HEADER + ev);
        }

        // TODO: Review - should we validate further?
        // (e.g. adding two VersionBitmap elements might be bad)
        return this;
    }

    private static final String E_WRONG_VER_IN_HEADER = ": max supported version: ";
}