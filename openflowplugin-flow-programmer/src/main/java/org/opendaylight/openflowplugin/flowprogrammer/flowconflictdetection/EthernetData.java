/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import java.lang.Long;

/**
 * This class converts EthernetMatch to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class EthernetData {
    private Long srcMac;
    private Long dstMac;
    private Long etherType;

    public EthernetData(Long src, Long dst, Long type) {
        this.srcMac = src;
        this.dstMac = dst;
        this.etherType = type;
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

    public static EthernetData toEthernetData(EthernetMatch etherMatch) {
        Long src = Long.valueOf(-1); // if srcMac is not set
        Long dst = Long.valueOf(-1); // if dstMac is not set
        Long type = Long.valueOf(-1); // // if etherType is not set
        if (etherMatch != null) {
            EthernetSource srcEthernet = etherMatch.getEthernetSource();
            if (srcEthernet != null) {
                MacAddress srcMac = srcEthernet.getAddress();
                if (srcMac != null) {
                    src = macAddressToLong(srcMac);
                }
                MacAddress srcMask = srcEthernet.getMask();
                if (srcMask != null) {
                    src = src & macAddressToLong(srcMask);
                }
            }
            EthernetDestination dstEthernet = etherMatch.getEthernetDestination();
            if (dstEthernet != null) {
                MacAddress dstMac = dstEthernet.getAddress();
                if (dstMac != null) {
                    dst = macAddressToLong(dstMac);
                }
                MacAddress dstMask = dstEthernet.getMask();
                if (dstMask != null) {
                    dst = dst & macAddressToLong(dstMask);
                }
            }
            EthernetType etherType = etherMatch.getEthernetType();
            if (etherType != null) {
                type = etherType.getType().getValue();
            }
        }
        return new EthernetData(src, dst, type);
    }

    public boolean isSame(EthernetMatch match) {
        EthernetData etherData = toEthernetData(match);
        if (!etherData.etherType.equals(this.etherType)
            && !etherData.etherType.equals(Long.valueOf(-1))
            && !this.etherType.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!etherData.srcMac.equals(this.srcMac)
            && !etherData.srcMac.equals(Long.valueOf(-1))
            && !this.srcMac.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!etherData.dstMac.equals(this.dstMac)
            && !etherData.dstMac.equals(Long.valueOf(-1))
            && !this.dstMac.equals(Long.valueOf(-1))) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        return "etherType: " + String.valueOf(this.etherType) + ", srcMac: " + this.srcMac.toString() + ", dstMac: " + this.dstMac.toString();
    }
}
