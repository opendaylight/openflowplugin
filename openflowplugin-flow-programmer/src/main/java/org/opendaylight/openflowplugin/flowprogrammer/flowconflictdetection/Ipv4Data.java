/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import java.lang.Long;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;

/**
 * This class converts Ipv4Match to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class Ipv4Data {
    Long ipv4Src;
    Long ipv4Dst;

    public Ipv4Data(Long ipv4Src, Long ipv4Dst) {
        this.ipv4Src = ipv4Src;
        this.ipv4Dst = ipv4Dst;
    }

    public static Long ipv4PrefixToLong(Ipv4Prefix ip) {
        Long mask = Long.valueOf(-1);
        String[] parts = ip.getValue().split("/");
        if (parts.length == 2) {
            mask = Long.decode("0xFFFFFFFF00000000") >> Long.decode(parts[1]);
            mask &= Long.decode("0x00000000FFFFFFFF");
        }
        String[] bytes = parts[0].split(".");
        Long address =
            (Long.decode(bytes[0]) << 24) |
            (Long.decode(bytes[1]) << 16) |
            (Long.decode(bytes[2]) << 8) |
            (Long.decode(bytes[3]));
        if (mask != Long.valueOf(-1)) {
            address &= mask;
        }
        return address;
    }

    public static Ipv4Data toIpv4Data(Ipv4Match match) {
        Long ipv4Src = Long.valueOf(-1);
        Long ipv4Dst = Long.valueOf(-1);
        Ipv4Prefix ip = match.getIpv4Source();
        if (ip != null) {
            ipv4Src = ipv4PrefixToLong(ip);
        }
        ip = match.getIpv4Destination();
        if (ip != null) {
            ipv4Dst = ipv4PrefixToLong(ip);
        }
        return new Ipv4Data(ipv4Src, ipv4Dst);
    }

    public boolean isSame(Ipv4Match match) {
        Ipv4Data ipv4Data = toIpv4Data(match);
        if (!ipv4Data.ipv4Src.equals(this.ipv4Src)
            && !ipv4Data.ipv4Src.equals(Long.valueOf(-1))
            && !this.ipv4Src.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!ipv4Data.ipv4Dst.equals(this.ipv4Dst)
            && !ipv4Data.ipv4Dst.equals(Long.valueOf(-1))
            && !this.ipv4Dst.equals(Long.valueOf(-1))) {
            return false;
        }
        return true;
    }
}
