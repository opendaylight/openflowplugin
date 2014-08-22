/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ProtocolVersion;

import java.util.Collections;
import java.util.Set;

/**
 * Hello element {@code VERSION_BITMAP}.
 *
 * @author Simon Hunt
 */
public class HelloElemVersionBitmap extends HelloElement {
    /** Length in bytes. */
    private static final int LIB_VER_BITMAP = 8;

    Set<ProtocolVersion> supportedVersions;

    /**
     * Constructs a VERSION BITMAP hello element.
     *
     * @param pv     the protocol version
     * @param header the element header
     */
    HelloElemVersionBitmap(ProtocolVersion pv, Header header) {
        super(pv, header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",suppVers=").append(supportedVersions)
                .append("}");
        return sb.toString();
    }

    /** Returns the supported versions.
     *
     * @return the supported versions
     */
    public Set<ProtocolVersion> getSupportedVersions() {
        return Collections.unmodifiableSet(supportedVersions);
    }

    /** Calculates the total length of this element.
     *
     * @return the total length of this element
     */
    int calcTotalLength() {
        // Implementation note: currently we hardcode this 8, which is the
        // correct value while we can express all possible versions in a
        // single bitmap
        return LIB_VER_BITMAP;
    }
}
