/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public final class HashUtil {

    private HashUtil() {

        throw new IllegalStateException("This class should not be instantiated.");
    }

    public static long calculateMatchHash(final Match match) {
        long hash = 0;
        int base = 0;
        if (null != match) {
            if (null != match.getEthernetMatch()) {
                hash = 1 << 0;
            }
            base++;
            if (null != match.getIcmpv4Match()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getIcmpv6Match()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getInPhyPort()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getInPort()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getIpMatch()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getLayer3Match()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getLayer4Match()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getMetadata()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getProtocolMatchFields()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getTcpFlagMatch()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getVlanMatch()) {
                hash = 1 << base;
            }
            base++;
            if (null != match.getTunnel()) {
                hash = 1 << base;
            }

        }
        return hash;
    }


}
