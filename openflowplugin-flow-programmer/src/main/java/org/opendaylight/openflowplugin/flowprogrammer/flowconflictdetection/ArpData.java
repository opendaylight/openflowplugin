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

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;


/**
 * This class converts ArpMatch to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class ArpData {
    Integer arpOp;
    Long srcMac;
    Long dstMac;
    Long ipv4Src;
    Long ipv4Dst;

    public ArpData(Integer arpOp, Long srcMac, Long dstMac, Long ipv4Src, Long ipv4Dst) {
        this.arpOp = arpOp;
        this.srcMac = srcMac;
        this.dstMac = dstMac;
        this.ipv4Src = ipv4Src;
        this.ipv4Dst = ipv4Dst;
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

    public static ArpData toArpData(ArpMatch match) {
        Integer arpOp = Integer.valueOf(-1);
        Long srcMac = Long.valueOf(-1);
        Long dstMac = Long.valueOf(-1);
        Long ipv4Src = Long.valueOf(-1);
        Long ipv4Dst = Long.valueOf(-1);

        if (match != null) {
            arpOp = match.getArpOp();
            ArpSourceHardwareAddress srcHA = match.getArpSourceHardwareAddress();
            if (srcHA != null) {
                MacAddress src = srcHA.getAddress();
                if (src != null) {
                    srcMac = macAddressToLong(src);
                }
                MacAddress srcMask = srcHA.getMask();
                if (srcMask != null) {
                    srcMac = srcMac & macAddressToLong(srcMask);
                }
            }
            ArpTargetHardwareAddress dstHA = match.getArpTargetHardwareAddress();
            if (dstHA != null) {
                MacAddress dst = dstHA.getAddress();
                if (dst != null) {
                    dstMac = macAddressToLong(dst);
                }
                MacAddress dstMask = dstHA.getMask();
                if (dstMask != null) {
                    dstMac = dstMac & macAddressToLong(dstMask);
                }
            }
            Ipv4Prefix srcIp = match.getArpSourceTransportAddress();
            if (srcIp != null) {
                ipv4Src = ipv4PrefixToLong(srcIp);
            }
            Ipv4Prefix dstIp = match.getArpTargetTransportAddress();
            if (dstIp != null) {
                ipv4Dst = ipv4PrefixToLong(dstIp);
            }
        }

        return new ArpData(arpOp, srcMac, dstMac, ipv4Src, ipv4Dst);
    }

    public boolean isSame(ArpMatch match) {
        ArpData arpData = toArpData(match);
        if (!arpData.arpOp.equals(this.arpOp)
            && !arpData.arpOp.equals(Integer.valueOf(-1))
            && !this.arpOp.equals(Integer.valueOf(-1))) {
            return false;
        }
        if (!arpData.srcMac.equals(this.srcMac)
            && !arpData.srcMac.equals(Long.valueOf(-1))
            && !this.srcMac.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!arpData.dstMac.equals(this.dstMac)
            && !arpData.dstMac.equals(Long.valueOf(-1))
            && !this.dstMac.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!arpData.ipv4Src.equals(this.ipv4Src)
            && !arpData.ipv4Src.equals(Long.valueOf(-1))
            && !this.ipv4Src.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!arpData.ipv4Dst.equals(this.ipv4Dst)
            && !arpData.ipv4Dst.equals(Long.valueOf(-1))
            && !this.ipv4Dst.equals(Long.valueOf(-1))) {
            return false;
        }
        return true;
    }
}
