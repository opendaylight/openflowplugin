/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.common;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.HelloElemVersionBitmap;
import org.opendaylight.of.lib.msg.HelloElement;
import org.opendaylight.of.lib.msg.HelloElementType;
import org.opendaylight.of.lib.msg.OfmHello;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides common algorithms that both controller and switch need to utilize.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class HandshakeLogic {

    /** Determines the protocol version that is common to both HELLO messages;
     * returns null if no such version exists.
     * <p>
     * See Section 6.3.1 "Connection Setup" of the 1.3.1 OpenFlow Spec.
     *
     * @param h1 hello message one
     * @param h2 hello message two
     * @return the negotiated version
     */
    public static ProtocolVersion negotiateVersion(OfmHello h1, OfmHello h2) {
        ProtocolVersion pv1 = h1.getVersion();
        ProtocolVersion pv2 = h2.getVersion();

        // Simple case is if the HELLO header versions match - that's the one
        if (pv1 == pv2)
            return pv1;

        ProtocolVersion result = null;

        Set<ProtocolVersion> sup1 = getSupportVers(h1);
        Set<ProtocolVersion> sup2 = getSupportVers(h2);

        if (sup1 != null || sup2 != null) {

            if (sup1 != null && sup2 != null) {
                Set<ProtocolVersion> working = new HashSet<ProtocolVersion>(sup1);
                working.retainAll(sup2);
                result = ProtocolVersion.max(working);
            } else {
                Set<ProtocolVersion> versionSet = sup1 != null ? sup1 : sup2;
                ProtocolVersion base = sup1 == null ? pv1 : pv2;
                if (versionSet.contains(base))
                    result = base;
            }
        }

        return result;
    }

    // Returns a set of supported versions if set in the Element list
    private static Set<ProtocolVersion> getSupportVers(OfmHello h) {
        Set<ProtocolVersion> set = null;

        List<HelloElement> elm1 = h.getElements();
        if (elm1 != null && elm1.size() > 0) {
            for (HelloElement e : elm1) {
                if (e.getElementType() == HelloElementType.VERSION_BITMAP) {
                    set = ((HelloElemVersionBitmap) e).getSupportedVersions();
                    break;
                }
            }
        }
        return set;
    }
}
