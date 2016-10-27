/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/30/14.
 */
public class MatchConvertorV10ImplTest {

    private static final MatchConvertorV10Impl matchConvertorV10 = new MatchConvertorV10Impl();
    private static final BigInteger dataPathId = BigInteger.TEN;
    private static final long ETH_TYPE_802_3 = 0x0000;
    private static final MacAddress ZERO_MAC = MacAddress.getDefaultInstance("00:00:00:00:00:00");
    private static final MacAddress FF_MAC = MacAddress.getDefaultInstance("ff:ff:ff:ff:ff:ff");
    private static final String NODE_CONNECTOR_ID = "1234";
    private static final short ZERO = 0;
    private static final String DSCP = "0";
    private static final short IP_PROTOCOL = 6;
    private static final PortNumber DEFAULT_PORT = new PortNumber(9999);
    private static final Ipv4Prefix ipv4Prefix = Ipv4Prefix.getDefaultInstance("10.0.0.1/24");
    private static final VlanId DEFAULT_VLAN_ID = new VlanId(42);
    private static final Ipv4Address DEFAULT_IPV4_ADDRESS = new Ipv4Address("10.0.0.1");
    private static final short DEFAULT_MASK = 24;
    private ConvertorManager convertorManager;

    @Before
    public void setup() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    /**
     * Test method for {@link MatchConvertorV10Impl#convert(Match,BigInteger)}
     */
    public void testConvert() {
        MatchV10 matchV10 = matchConvertorV10.convert(createL4UdpMatch().build(), null);

        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(0, matchV10.getDlType().intValue());
        assertEquals(0xffff, matchV10.getDlVlan().intValue());
        assertEquals(DEFAULT_PORT.getValue().intValue(), matchV10.getTpSrc().intValue());
        assertEquals(DEFAULT_PORT.getValue().intValue(), matchV10.getTpDst().intValue());
        assertEquals(Integer.parseInt(NODE_CONNECTOR_ID), matchV10.getInPort().intValue());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(), matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask().shortValue());
        assertEquals(0, matchV10.getNwTos().shortValue());
        assertEquals(DEFAULT_PORT.getValue().intValue(), matchV10.getTpSrc().intValue());
        assertEquals(DEFAULT_PORT.getValue().intValue(), matchV10.getTpDst().intValue());

        matchV10 = matchConvertorV10.convert(createL4TcpMatch().build(), null);
        assertEquals(DEFAULT_PORT.getValue().intValue(), matchV10.getTpSrc().intValue());
        assertEquals(DEFAULT_PORT.getValue().intValue(), matchV10.getTpDst().intValue());

        matchV10 = matchConvertorV10.convert(createVlanTcpMatch().build(), null);
        assertEquals(DEFAULT_VLAN_ID.getValue().intValue(), matchV10.getDlVlan().intValue());

    }

    /**
     * ICMPv4 match test for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertReactorConvertor#convert(Object, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor)}.
     */
    @Test
    public void testConvertIcmpv4() {
        MatchBuilder matchBuilder = createMatchBuilderWithDefaults();
        Match match = matchBuilder.build();
        MatchV10 matchV10 = matchConvertorV10.convert(match, convertorManager);
        Integer zero = 0;
        boolean wcTpSrc = true;
        boolean wcTpDst = true;
        FlowWildcardsV10 wc = new FlowWildcardsV10(
            false, false, false, true, true, false, false, false,
            wcTpDst, wcTpSrc);
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(0, matchV10.getDlType().intValue());
        assertEquals(0xffff, matchV10.getDlVlan().intValue());
        assertEquals(Integer.parseInt(NODE_CONNECTOR_ID),
                     matchV10.getInPort().intValue());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(),
                     matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask().shortValue());
        assertEquals(0, matchV10.getNwTos().shortValue());
        assertEquals(zero, matchV10.getTpSrc());
        assertEquals(zero, matchV10.getTpDst());
        assertEquals(wc, matchV10.getWildcards());

        // Specify ICMP type only.
        Integer icmpType = 55;
        Icmpv4MatchBuilder icmpv4MatchBuilder = new Icmpv4MatchBuilder().
            setIcmpv4Type(icmpType.shortValue());
        wcTpSrc = false;
        wc = new FlowWildcardsV10(
            false, false, false, true, true, false, false, false,
            wcTpDst, wcTpSrc);
        match = matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build()).
            build();
        matchV10 = matchConvertorV10.convert(match, convertorManager);
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(0, matchV10.getDlType().intValue());
        assertEquals(0xffff, matchV10.getDlVlan().intValue());
        assertEquals(Integer.parseInt(NODE_CONNECTOR_ID),
                     matchV10.getInPort().intValue());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(),
                     matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask().shortValue());
        assertEquals(0, matchV10.getNwTos().shortValue());
        assertEquals(icmpType, matchV10.getTpSrc());
        assertEquals(zero, matchV10.getTpDst());
        assertEquals(wc, matchV10.getWildcards());

        // Specify ICMP code only.
        Integer icmpCode = 31;
        icmpv4MatchBuilder = new Icmpv4MatchBuilder().
            setIcmpv4Type(null).setIcmpv4Code(icmpCode.shortValue());
        wcTpSrc = true;
        wcTpDst = false;
        wc = new FlowWildcardsV10(
            false, false, false, true, true, false, false, false,
            wcTpDst, wcTpSrc);
        match = matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build()).
            build();
        matchV10 = matchConvertorV10.convert(match, convertorManager);
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(0, matchV10.getDlType().intValue());
        assertEquals(0xffff, matchV10.getDlVlan().intValue());
        assertEquals(Integer.parseInt(NODE_CONNECTOR_ID),
                     matchV10.getInPort().intValue());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(),
                     matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask().shortValue());
        assertEquals(0, matchV10.getNwTos().shortValue());
        assertEquals(zero, matchV10.getTpSrc());
        assertEquals(icmpCode, matchV10.getTpDst());
        assertEquals(wc, matchV10.getWildcards());

        // Specify both ICMP type and code.
        icmpType = 11;
        icmpCode = 22;
        icmpv4MatchBuilder = new Icmpv4MatchBuilder().
            setIcmpv4Type(icmpType.shortValue()).
            setIcmpv4Code(icmpCode.shortValue());
        wcTpSrc = false;
        wcTpDst = false;
        wc = new FlowWildcardsV10(
            false, false, false, true, true, false, false, false,
            wcTpDst, wcTpSrc);
        match = matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build()).
            build();
        matchV10 = matchConvertorV10.convert(match, convertorManager);
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(0, matchV10.getDlType().intValue());
        assertEquals(0xffff, matchV10.getDlVlan().intValue());
        assertEquals(Integer.parseInt(NODE_CONNECTOR_ID),
                     matchV10.getInPort().intValue());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(),
                     matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask().shortValue());
        assertEquals(0, matchV10.getNwTos().shortValue());
        assertEquals(icmpType, matchV10.getTpSrc());
        assertEquals(icmpCode, matchV10.getTpDst());
        assertEquals(wc, matchV10.getWildcards());
    }

    private static MatchBuilder createL4UdpMatch() {
        MatchBuilder matchBuilder = createMatchBuilderWithDefaults();

        UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();

        udpMatchBuilder.setUdpDestinationPort(DEFAULT_PORT);
        udpMatchBuilder.setUdpSourcePort(DEFAULT_PORT);
        matchBuilder.setLayer4Match(udpMatchBuilder.build());

        return matchBuilder;
    }

    private static MatchBuilder createVlanTcpMatch() {
        MatchBuilder matchBuilder = createL4TcpMatch();
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanId(DEFAULT_VLAN_ID);
        vlanIdBuilder.setVlanIdPresent(true);
        vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
        matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        return matchBuilder;
    }

    private static MatchBuilder createL4TcpMatch() {
        MatchBuilder matchBuilder = createMatchBuilderWithDefaults();

        TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
        tcpMatchBuilder.setTcpDestinationPort(DEFAULT_PORT);
        tcpMatchBuilder.setTcpSourcePort(DEFAULT_PORT);
        matchBuilder.setLayer4Match(tcpMatchBuilder.build());

        return matchBuilder;
    }

    private static MatchBuilder createMatchBuilderWithDefaults() {
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder();
        EthernetTypeBuilder ethernetTypeBuilder = new EthernetTypeBuilder();

        //IEEE802.3
        EtherType etherType = new EtherType(ETH_TYPE_802_3);
        ethernetTypeBuilder.setType(etherType);
        ethernetMatchBuilder.setEthernetType(ethernetTypeBuilder.build());

        EthernetDestinationBuilder ethernetDestinationBuilder = new EthernetDestinationBuilder();
        ethernetDestinationBuilder.setAddress(ZERO_MAC);
        ethernetDestinationBuilder.setMask(ZERO_MAC);
        ethernetMatchBuilder.setEthernetDestination(ethernetDestinationBuilder.build());

        EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
        ethernetSourceBuilder.setMask(FF_MAC);
        ethernetSourceBuilder.setAddress(FF_MAC);
        ethernetMatchBuilder.setEthernetSource(ethernetSourceBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());

        NodeConnectorId nodeConnectorId = NodeConnectorId.getDefaultInstance(NODE_CONNECTOR_ID);

        matchBuilder.setInPhyPort(nodeConnectorId);
        matchBuilder.setInPort(nodeConnectorId);

        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        ipMatchBuilder.setIpDscp(Dscp.getDefaultInstance(DSCP));
        ipMatchBuilder.setIpEcn(ZERO);
        ipMatchBuilder.setIpProto(IpVersion.Ipv4);
        ipMatchBuilder.setIpProtocol(IP_PROTOCOL);
        matchBuilder.setIpMatch(ipMatchBuilder.build());

        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Destination(ipv4Prefix);
        ipv4MatchBuilder.setIpv4Source(ipv4Prefix);
        matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
        matchBuilder.setInPort(new NodeConnectorId(NODE_CONNECTOR_ID));
        return matchBuilder;
    }
}
