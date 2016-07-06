/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 21.4.2015.
 */
public final class MatchUtil {

    private static final MacAddress ZERO_MAC_ADDRESS = new MacAddress("00:00:00:00:00:00");
    private static final Ipv4Address ZERO_IPV4_ADDRESS = new Ipv4Address("0.0.0.0");
    private MatchUtil(){
        throw new IllegalStateException("This class should not be instantiated.");
    }


    public static MatchV10Builder createEmptyV10Match() {
        Short zeroShort = Short.valueOf("0");
        Integer zeroInteger = Integer.valueOf(0);
        MatchV10Builder matchV10Builder = new MatchV10Builder();
        matchV10Builder.setDlDst(ZERO_MAC_ADDRESS);
        matchV10Builder.setDlSrc(ZERO_MAC_ADDRESS);
        matchV10Builder.setDlType(zeroInteger);
        matchV10Builder.setDlVlan(zeroInteger);
        matchV10Builder.setDlVlanPcp(zeroShort);
        matchV10Builder.setInPort(zeroInteger);
        matchV10Builder.setNwDst(ZERO_IPV4_ADDRESS);
        matchV10Builder.setNwDstMask(zeroShort);
        matchV10Builder.setNwProto(zeroShort);
        matchV10Builder.setNwSrc(ZERO_IPV4_ADDRESS);
        matchV10Builder.setNwSrcMask(zeroShort);
        matchV10Builder.setNwTos(zeroShort);
        matchV10Builder.setTpDst(zeroInteger);
        matchV10Builder.setTpSrc(zeroInteger);
        FlowWildcardsV10 flowWildcardsV10 = new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true);
        matchV10Builder.setWildcards(flowWildcardsV10);
        return matchV10Builder;
    }



}
