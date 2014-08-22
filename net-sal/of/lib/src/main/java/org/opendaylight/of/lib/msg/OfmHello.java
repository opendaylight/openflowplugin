/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import java.util.Collections;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Represents an OpenFlow HELLO message; Since 1.0.
 *
 * @author Simon Hunt
 */
public class OfmHello extends OpenflowMessage {

    List<HelloElement> elements;

    /**
     * Constructs an OpenFlow HELLO message.
     *
     * @param header the message header
     */
    OfmHello(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        if (elements == null || elements.size() == 0)
            return super.toString();

        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",elems=").append(elemList()).append("}");
        return sb.toString();
    }

    /** Returns a comma separated list of hello element type names.
     *
     * @return element type names
     */
    private String elemList() {
        StringBuilder sb = new StringBuilder();
        for (HelloElement he: elements)
            sb.append(he.getElementType()).append(",");
        int len = sb.length();
        sb.replace(len-1, len, "");
        return sb.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        if (elements != null)
            for (HelloElement he: elements)
                sb.append(EOLI).append(he);
        return sb.toString();
    }

    /** Returns the hello elements associated with this HELLO message.
     *
     * @return the hello elements
     */
    public List<HelloElement> getElements() {
        return elements == null ? null : Collections.unmodifiableList(elements);
    }
}
