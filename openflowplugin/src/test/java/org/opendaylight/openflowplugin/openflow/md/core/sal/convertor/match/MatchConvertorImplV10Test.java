/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;

/**
 * @author michal.polkorab
 *
 */
public class MatchConvertorImplV10Test {

    /**
     * Initializes OpenflowPortsUtil
     */
    @Before
    public void startUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchV10ToSALMatch(MatchV10, BigInteger, OpenflowVersion)}
     */
    @Test
    public void test() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(false, false, false, false,
                false, false, false, false, false, false));
        builder.setNwSrcMask((short) 24);
        builder.setNwDstMask((short) 16);
        builder.setInPort(6653);
        builder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        builder.setDlDst(new MacAddress("02:02:02:02:02:02"));
        builder.setDlVlan(128);
        builder.setDlVlanPcp((short) 2);
        builder.setDlType(15);
        builder.setNwTos((short) 16);
        builder.setNwProto((short) 6);
        builder.setNwSrc(new Ipv4Address("1.1.1.2"));
        builder.setNwDst(new Ipv4Address("32.16.8.1"));
        builder.setTpSrc(2048);
        builder.setTpDst(4096);
        MatchV10 match = builder.build();

        Match salMatch = MatchConvertorImpl.fromOFMatchV10ToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);

        Assert.assertEquals("Wrong in port", "openflow:42:6653", salMatch.getInPort().getValue());
        Assert.assertEquals("Wrong dl src", new MacAddress("01:01:01:01:01:01"), salMatch.getEthernetMatch()
                .getEthernetSource().getAddress());
        Assert.assertEquals("Wrong dl dst", new MacAddress("02:02:02:02:02:02"), salMatch.getEthernetMatch()
                .getEthernetDestination().getAddress());
        Assert.assertEquals("Wrong dl type", 15, salMatch.getEthernetMatch().getEthernetType().getType().getValue().intValue());
        Assert.assertEquals("Wrong dl vlan", 128, salMatch.getVlanMatch().getVlanId().getVlanId().getValue().intValue());
        Assert.assertEquals("Wrong dl vlan pcp", 2, salMatch.getVlanMatch().getVlanPcp().getValue().intValue());
        Ipv4Match ipv4Match = (Ipv4Match) salMatch.getLayer3Match();
        Assert.assertEquals("Wrong nw src address", "1.1.1.2/24", ipv4Match.getIpv4Source().getValue());
        Assert.assertEquals("Wrong nw dst address", "32.16.8.1/16", ipv4Match.getIpv4Destination().getValue());
        Assert.assertEquals("Wrong ip protocol", 6, salMatch.getIpMatch().getIpProtocol().intValue());
        Assert.assertEquals("Wrong ip proto", null, salMatch.getIpMatch().getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, salMatch.getIpMatch().getIpEcn());
        Assert.assertEquals("Wrong ip dscp", 4, salMatch.getIpMatch().getIpDscp().getValue().intValue());
        TcpMatch tcpMatch = (TcpMatch) salMatch.getLayer4Match();
        Assert.assertEquals("Wrong tp dst", 4096, tcpMatch.getTcpDestinationPort().getValue().intValue());
        Assert.assertEquals("Wrong tp src", 2048, tcpMatch.getTcpSourcePort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchV10ToSALMatch(MatchV10, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testWildcardedMatch() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        builder.setNwSrcMask((short) 24);
        builder.setNwDstMask((short) 16);
        builder.setInPort(6653);
        builder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        builder.setDlDst(new MacAddress("02:02:02:02:02:02"));
        builder.setDlVlan(128);
        builder.setDlVlanPcp((short) 2);
        builder.setDlType(15);
        builder.setNwTos((short) 14);
        builder.setNwProto((short) 6);
        builder.setNwSrc(new Ipv4Address("1.1.1.2"));
        builder.setNwDst(new Ipv4Address("32.16.8.1"));
        builder.setTpSrc(2048);
        builder.setTpDst(4096);
        MatchV10 match = builder.build();

        Match salMatch = MatchConvertorImpl.fromOFMatchV10ToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);

        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl match", null, salMatch.getEthernetMatch());
        Assert.assertEquals("Wrong dl vlan match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong layer 3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong layer 4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ip match", null, salMatch.getIpMatch());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchV10ToSALMatch(MatchV10, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testWildcardedMatchWithNoValuesSet() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        MatchV10 match = builder.build();

        Match salMatch = MatchConvertorImpl.fromOFMatchV10ToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);

        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl match", null, salMatch.getEthernetMatch());
        Assert.assertEquals("Wrong dl vlan match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong layer 3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong layer 4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ip match", null, salMatch.getIpMatch());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchV10ToSALMatch(MatchV10, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testMatchWithValuesUnset() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(false, false, false, false,
                false, false, false, false, false, false));
        builder.setNwProto((short) 17);
        builder.setTpSrc(2048);
        builder.setTpDst(4096);
        MatchV10 match = builder.build();

        Match salMatch = MatchConvertorImpl.fromOFMatchV10ToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);

        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl match", null, salMatch.getEthernetMatch());
        Assert.assertEquals("Wrong dl vlan match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong layer 3 match", null, salMatch.getLayer3Match());
        UdpMatch udpMatch = (UdpMatch) salMatch.getLayer4Match();
        Assert.assertEquals("Wrong udp dst", 4096, udpMatch.getUdpDestinationPort().getValue().intValue());
        Assert.assertEquals("Wrong udp src", 2048, udpMatch.getUdpSourcePort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchV10ToSALMatch(MatchV10, BigInteger, OpenflowVersion)}
     */
    @Test(expected=NullPointerException.class)
    public void testEmptyMatch() {
        MatchV10Builder builder = new MatchV10Builder();
        MatchV10 match = builder.build();

        MatchConvertorImpl.fromOFMatchV10ToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
    }
}