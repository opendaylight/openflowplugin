/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/30/14.
 */
public class MatchConvertorV10Test {

    private static final Uint32 ETH_TYPE_802_3 = Uint32.ZERO;
    private static final MacAddress ZERO_MAC = MacAddress.getDefaultInstance("00:00:00:00:00:00");
    private static final MacAddress FF_MAC = MacAddress.getDefaultInstance("ff:ff:ff:ff:ff:ff");
    private static final String NODE_CONNECTOR_ID = "1234";
    private static final String DSCP = "0";
    private static final Uint8 IP_PROTOCOL = Uint8.valueOf(6);
    private static final PortNumber DEFAULT_PORT = new PortNumber(Uint16.valueOf(9999));
    private static final Ipv4Prefix IPV4_PREFIX = Ipv4Prefix.getDefaultInstance("10.0.0.1/24");
    private static final VlanId DEFAULT_VLAN_ID = new VlanId(Uint16.valueOf(42));
    private static final Ipv4Address DEFAULT_IPV4_ADDRESS = new Ipv4Address("10.0.0.1");
    private static final Uint8 DEFAULT_MASK = Uint8.valueOf(24);
    private ConvertorManager converterManager;

    @Before
    public void setup() {
        converterManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    public void testConvert() {

        Optional<MatchV10> matchV10Optional = converterManager.convert(createL4UdpMatch().build(),
                new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        MatchV10 matchV10 = matchV10Optional.get();

        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(Uint16.ZERO, matchV10.getDlType());
        assertEquals(Uint16.MAX_VALUE, matchV10.getDlVlan());
        assertEquals(DEFAULT_PORT.getValue(), matchV10.getTpSrc());
        assertEquals(DEFAULT_PORT.getValue(), matchV10.getTpDst());
        assertEquals(Uint16.valueOf(NODE_CONNECTOR_ID), matchV10.getInPort());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(), matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask());
        assertEquals(Uint8.ZERO, matchV10.getNwTos());
        assertEquals(DEFAULT_PORT.getValue(), matchV10.getTpSrc());
        assertEquals(DEFAULT_PORT.getValue(), matchV10.getTpDst());

        matchV10Optional = converterManager.convert(createL4TcpMatch().build(),
                new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        matchV10 = matchV10Optional.get();
        assertEquals(DEFAULT_PORT.getValue(), matchV10.getTpSrc());
        assertEquals(DEFAULT_PORT.getValue(), matchV10.getTpDst());

        matchV10Optional = converterManager.convert(createVlanTcpMatch().build(),
                new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        matchV10 = matchV10Optional.get();
        assertEquals(DEFAULT_VLAN_ID.getValue(), matchV10.getDlVlan());
    }

    @Test
    public void testConvertIcmpv4() {
        MatchBuilder matchBuilder = createMatchBuilderWithDefaults();
        Match match = matchBuilder.build();
        Optional<MatchV10> matchV10Optional = converterManager.convert(match,
                new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        MatchV10 matchV10 = matchV10Optional.get();

        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(Uint16.ZERO, matchV10.getDlType());
        assertEquals(Uint16.MAX_VALUE, matchV10.getDlVlan());
        assertEquals(Uint16.valueOf(NODE_CONNECTOR_ID), matchV10.getInPort());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(), matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask());
        assertEquals(Uint8.ZERO, matchV10.getNwTos());
        assertEquals(Uint16.ZERO, matchV10.getTpSrc());
        assertEquals(Uint16.ZERO, matchV10.getTpDst());

        boolean wcTpSrc = true;
        boolean wcTpDst = true;
        FlowWildcardsV10 wc = new FlowWildcardsV10(
                false, false, false, true, true, false, false, false,
                wcTpDst, wcTpSrc);
        assertEquals(wc, matchV10.getWildcards());

        // Specify ICMP type only.
        Uint8 icmpType = Uint8.valueOf(55);
        Icmpv4MatchBuilder icmpv4MatchBuilder = new Icmpv4MatchBuilder().setIcmpv4Type(icmpType);
        wcTpSrc = false;
        wc = new FlowWildcardsV10(
            false, false, false, true, true, false, false, false,
            wcTpDst, wcTpSrc);
        match = matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build()).build();
        matchV10Optional = converterManager.convert(match,
                new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        matchV10 = matchV10Optional.get();
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(0, matchV10.getDlType().intValue());
        assertEquals(0xffff, matchV10.getDlVlan().intValue());
        assertEquals(Integer.parseInt(NODE_CONNECTOR_ID),
                     matchV10.getInPort().intValue());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(),
                     matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask());
        assertEquals(Uint8.ZERO, matchV10.getNwTos());
        assertEquals(55, matchV10.getTpSrc().toJava());
        assertEquals(Uint16.ZERO, matchV10.getTpDst());
        assertEquals(wc, matchV10.getWildcards());

        // Specify ICMP code only.
        Uint8 icmpCode = Uint8.valueOf(31);
        icmpv4MatchBuilder = new Icmpv4MatchBuilder().setIcmpv4Type((Uint8) null).setIcmpv4Code(icmpCode);
        wcTpSrc = true;
        wcTpDst = false;
        wc = new FlowWildcardsV10(
            false, false, false, true, true, false, false, false,
            wcTpDst, wcTpSrc);
        match = matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build()).build();
        matchV10Optional = converterManager.convert(match,
                new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        matchV10 = matchV10Optional.get();
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(Uint16.ZERO, matchV10.getDlType());
        assertEquals(Uint16.MAX_VALUE, matchV10.getDlVlan());
        assertEquals(Uint16.valueOf(NODE_CONNECTOR_ID), matchV10.getInPort());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(), matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask());
        assertEquals(Uint8.ZERO, matchV10.getNwTos());
        assertEquals(Uint16.ZERO, matchV10.getTpSrc());
        assertEquals(icmpCode.toUint16(), matchV10.getTpDst());
        assertEquals(wc, matchV10.getWildcards());

        // Specify both ICMP type and code.
        icmpType = Uint8.valueOf(11);
        icmpCode = Uint8.valueOf(22);
        icmpv4MatchBuilder = new Icmpv4MatchBuilder().setIcmpv4Type(icmpType).setIcmpv4Code(icmpCode);
        wcTpSrc = false;
        wcTpDst = false;
        wc = new FlowWildcardsV10(false, false, false, true, true, false, false, false, wcTpDst, wcTpSrc);
        match = matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build()).build();
        matchV10Optional = converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_0));
        matchV10 = matchV10Optional.get();
        assertEquals(ZERO_MAC, matchV10.getDlDst());
        assertEquals(FF_MAC, matchV10.getDlSrc());
        assertEquals(Uint16.ZERO, matchV10.getDlType());
        assertEquals(Uint16.MAX_VALUE, matchV10.getDlVlan());
        assertEquals(Uint16.valueOf(NODE_CONNECTOR_ID), matchV10.getInPort());
        assertEquals(DEFAULT_IPV4_ADDRESS.getValue(), matchV10.getNwDst().getValue());
        assertEquals(DEFAULT_MASK, matchV10.getNwDstMask());
        assertEquals(Uint8.ZERO, matchV10.getNwTos());
        assertEquals(icmpType.toUint16(), matchV10.getTpSrc());
        assertEquals(icmpCode.toUint16(), matchV10.getTpDst());
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
        final MatchBuilder matchBuilder = createL4TcpMatch();
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

        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setEthernetMatch(ethernetMatchBuilder.build());

        NodeConnectorId nodeConnectorId = NodeConnectorId.getDefaultInstance(NODE_CONNECTOR_ID);

        matchBuilder.setInPhyPort(nodeConnectorId);
        matchBuilder.setInPort(nodeConnectorId);

        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        ipMatchBuilder.setIpDscp(Dscp.getDefaultInstance(DSCP));
        ipMatchBuilder.setIpEcn(Uint8.ZERO);
        ipMatchBuilder.setIpProto(IpVersion.Ipv4);
        ipMatchBuilder.setIpProtocol(IP_PROTOCOL);
        matchBuilder.setIpMatch(ipMatchBuilder.build());

        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Destination(IPV4_PREFIX);
        ipv4MatchBuilder.setIpv4Source(IPV4_PREFIX);
        matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
        matchBuilder.setInPort(new NodeConnectorId(NODE_CONNECTOR_ID));
        return matchBuilder;
    }
}
