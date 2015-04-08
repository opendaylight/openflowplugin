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
        if (null != match) {
            if (null != match.getEthernetMatch()) {
                hash += 1;
            }
            if (null != match.getIcmpv4Match()) {
                hash += 2;
            }
            if (null != match.getIcmpv6Match()) {
                hash += 4;
            }
            if (null != match.getInPhyPort()) {
                hash += 8;
            }
            if (null != match.getEthernetMatch()) {
                hash += 16;
            }
            if (null != match.getInPort()) {
                hash += 32;
            }
            if (null != match.getIpMatch()) {
                hash += 64;
            }
            if (null != match.getLayer3Match()) {
                hash += 128;
            }
            if (null != match.getLayer4Match()) {
                hash += 256;
            }
            if (null != match.getIcmpv6Match()) {
                hash += 512;
            }
            if (null != match.getIcmpv4Match()) {
                hash += 1024;
            }
            if (null != match.getMetadata()) {
                hash += 2048;
            }
            if (null != match.getProtocolMatchFields()) {
                hash += 4096;
            }
            if (null != match.getTcpFlagMatch()) {
                hash += 8192;
            }
            if (null != match.getVlanMatch()) {
                hash += 16384;
            }
            if (null != match.getTunnel()) {
                hash += 32768;
            }
        }
        return hash;
    }


}
