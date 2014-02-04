/**
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public class FlowCreatorUtil {
    
    public static void setWildcardedFlowMatch(short version,MultipartRequestFlowBuilder flowBuilder){
        if(version == OFConstants.OFP_VERSION_1_0){
            flowBuilder.setMatchV10(createWildcardedMatchV10());
        }
        if(version == OFConstants.OFP_VERSION_1_3){
            flowBuilder.setMatch(createWildcardedMatch());
        }
    }
    
    public static void setWildcardedFlowMatch(short version,MultipartRequestAggregateBuilder flowBuilder){
        if(version == OFConstants.OFP_VERSION_1_0){
            flowBuilder.setMatchV10(createWildcardedMatchV10());
        }
        if(version == OFConstants.OFP_VERSION_1_3){
            flowBuilder.setMatch(createWildcardedMatch());
        }
    }

    /**
     * Method creates openflow 1.0 format match, that can match all the flow entries.
     * @return
     */
    public static MatchV10 createWildcardedMatchV10() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        builder.setNwSrcMask((short) 0);
        builder.setNwDstMask((short) 0);
        builder.setInPort(0);
        builder.setDlSrc(new MacAddress("00:00:00:00:00:00"));
        builder.setDlDst(new MacAddress("00:00:00:00:00:00"));
        builder.setDlVlan(0);
        builder.setDlVlanPcp((short) 0);
        builder.setDlType(0);
        builder.setNwTos((short) 0);
        builder.setNwProto((short) 0);
        builder.setNwSrc(new Ipv4Address("0.0.0.0"));
        builder.setNwDst(new Ipv4Address("0.0.0.0"));
        builder.setTpSrc(0);
        builder.setTpDst(0);
        
        return builder.build();
    }
    
    public static Match createWildcardedMatch(){
        return new MatchBuilder().setType(OxmMatchType.class).build();

    }
}
