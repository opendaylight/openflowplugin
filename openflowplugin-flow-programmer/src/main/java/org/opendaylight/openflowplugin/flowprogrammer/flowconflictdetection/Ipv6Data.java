/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import java.lang.Long;
import java.lang.Integer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6Label;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;


/**
 * This class converts Ipv6Match to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class Ipv6Data {
    Long ipv6Src;
    Long ipv6Dst;

    public Ipv6Data(Long ipv6Src, Long ipv6Dst) {
        this.ipv6Src = ipv6Src;
        this.ipv6Dst = ipv6Dst;
    }

    public static Long macAddressToLong(MacAddress mac) {
        String HEX = "0x";
        String[] bytes = mac.getValue().split(":");
        Long address =
            (Long.decode(HEX + bytes[0]) << 40) |
            (Long.decode(HEX + bytes[1]) << 32) |
            (Long.decode(HEX + bytes[2]) << 24) |
            (Long.decode(HEX + bytes[3]) << 16) |
            (Long.decode(HEX + bytes[4]) << 8 ) |
            (Long.decode(HEX + bytes[5]));
        return address;
    }

    public static Long ipv6PrefixToLong(Ipv6Prefix ip) {
        // TODO
        Long address = Long.valueOf(-1);
        return address;
    }

    public static Ipv6Data toIpv6Data(Ipv6Match match) {
        // TODO
        Long ipv6Src = Long.valueOf(-1);
        Long ipv6Dst = Long.valueOf(-1);
        return new Ipv6Data(ipv6Src, ipv6Dst);
    }

    public boolean isSame(Ipv6Match match) {
        Ipv6Data ipv6Data = toIpv6Data(match);
        if (!ipv6Data.ipv6Src.equals(this.ipv6Src)
            && !ipv6Data.ipv6Src.equals(Long.valueOf(-1))
            && !this.ipv6Src.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!ipv6Data.ipv6Dst.equals(this.ipv6Dst)
            && !ipv6Data.ipv6Dst.equals(Long.valueOf(-1))
            && !this.ipv6Dst.equals(Long.valueOf(-1))) {
            return false;
        }
        return true;
    }
}
