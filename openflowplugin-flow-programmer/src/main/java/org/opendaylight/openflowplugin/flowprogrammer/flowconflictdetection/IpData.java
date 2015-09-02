/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;

import java.lang.Short;

/**
 * This class converts IpMatch to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class IpData {
    private Short ipDscp;
    private Short ipEcn;
    private int ipVer; //Ipv4: 1; Ipv6: 2;
    private Short ipProto; //icmpv4:1, tcp: 6, icmpv6: 58, udp: 17, sctp: 132

    public IpData(Short ipDscp, Short ipEcn, int ipVer, Short ipProto) {
        this.ipDscp = ipDscp;
        this.ipEcn = ipEcn;
        this.ipVer = ipVer;
        this.ipProto = ipProto;
    }

    public static IpData toIpData(IpMatch ipMatch) {
        Short ipDscp = -1;
        Short ipEcn = -1;
        int ipVer = -1;
        Short ipProto = -1;
        if (ipMatch != null) {
            ipDscp = ipMatch.getIpDscp().getValue();
            ipEcn = ipMatch.getIpEcn();
            ipVer = ipMatch.getIpProto().getIntValue();
            ipProto = ipMatch.getIpProtocol();
        }
        return new IpData(ipDscp, ipEcn, ipVer, ipProto);
    }

    public boolean isSame(IpMatch ipMatch) {
        IpData ipData = toIpData(ipMatch);
        if (!ipData.ipProto.equals(this.ipProto)
            && !ipData.ipProto.equals(Short.valueOf((short)(-1)))
            && !this.ipProto.equals(Short.valueOf((short)(-1)))) {
            return false;
        }
        if ((ipData.ipVer != this.ipVer)
            && (ipData.ipVer != -1) && (this.ipVer != -1)) {
            return false;
        }
        if (!ipData.ipDscp.equals(this.ipDscp)
            && !ipData.ipDscp.equals(Short.valueOf((short)(-1)))
            && !this.ipDscp.equals(Short.valueOf((short)(-1)))) {
            return false;
        }
        if (!ipData.ipEcn.equals(this.ipEcn)
            && !ipData.ipEcn.equals(Short.valueOf((short)(-1)))
            && !this.ipEcn.equals(Short.valueOf((short)(-1)))) {
            return false;
        }
        return true;
    }
}
