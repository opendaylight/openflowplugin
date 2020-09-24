/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.opendaylight.yangtools.yang.common.Uint16;

public class MatchNormalizationUtilTest {
    @Test
    public void normalizeInPortMatch() {
        final long port = 10;

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeInPortMatch(new MatchBuilder()
                .setInPort(new NodeConnectorId("openflow:1:" + port)), EncodeConstants.OF13_VERSION_ID);

        assertEquals(String.valueOf(port), matchBuilder.getInPort().getValue());
    }

    @Test
    public void normalizeInPhyPortMatch() {
        final long port = 10;

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeInPhyPortMatch(new MatchBuilder()
                .setInPhyPort(new NodeConnectorId("openflow:1:" + port)), EncodeConstants.OF13_VERSION_ID);

        assertEquals(String.valueOf(port), matchBuilder.getInPhyPort().getValue());
    }

    @Test
    public void normalizeArpMatch() {
        final int arpOp = 10;
        final String source = "192.168.1.2/24";
        final String destination = "192.168.2.2/24";
        final MacAddress sourceHw = new MacAddress("01:23:45:AB:CD:EF");
        final MacAddress dstHw = new MacAddress("01:23:45:AB:C0:EF");
        final MacAddress mask = new MacAddress("FF:FF:FF:FF:FF:FF");

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeArpMatch(new MatchBuilder()
                .setLayer3Match(new ArpMatchBuilder()
                        .setArpOp(Uint16.TEN)
                        .setArpSourceTransportAddress(new Ipv4Prefix(source))
                        .setArpTargetTransportAddress(new Ipv4Prefix(destination))
                        .setArpTargetHardwareAddress(new ArpTargetHardwareAddressBuilder()
                                .setAddress(dstHw)
                                .setMask(mask)
                                .build())
                        .setArpSourceHardwareAddress(new ArpSourceHardwareAddressBuilder()
                                .setAddress(sourceHw)
                                .setMask(mask)
                                .build())
                        .build()));

        assertEquals(arpOp, ((ArpMatch) matchBuilder.getLayer3Match()).getArpOp().intValue());
    }

    @Test
    public void normalizeTunnelIpv4Match() {
        final String source = "192.168.1.2/24";
        final String destination = "192.168.2.2/24";

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeTunnelIpv4Match(new MatchBuilder()
                .setLayer3Match(new TunnelIpv4MatchBuilder()
                        .setTunnelIpv4Source(new Ipv4Prefix(source))
                        .setTunnelIpv4Destination(new Ipv4Prefix(destination))
                        .build()));

        assertEquals("192.168.1.0/24",
                ((TunnelIpv4Match) matchBuilder.getLayer3Match()).getTunnelIpv4Source().getValue());
        assertEquals("192.168.2.0/24",
                ((TunnelIpv4Match) matchBuilder.getLayer3Match()).getTunnelIpv4Destination().getValue());
    }

    @Test
    public void normalizeIpv4Match() {
        final String source = "192.168.1.2/24";
        final String destination = "192.168.2.2/24";

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeIpv4Match(new MatchBuilder()
                .setLayer3Match(new Ipv4MatchBuilder()
                        .setIpv4Source(new Ipv4Prefix(source))
                        .setIpv4Destination(new Ipv4Prefix(destination))
                        .build()));

        assertEquals("192.168.1.0/24", ((Ipv4Match) matchBuilder.getLayer3Match()).getIpv4Source().getValue());
        assertEquals("192.168.2.0/24",
                ((Ipv4Match) matchBuilder.getLayer3Match()).getIpv4Destination().getValue());
    }

    @Test
    public void normalizeIpv4MatchArbitraryBitMask() {
        final Ipv4Address leftAddress = new Ipv4Address("192.168.72.1");
        final DottedQuad leftMask = new DottedQuad("255.255.0.0");
        final Ipv4Prefix right = new Ipv4Prefix("192.168.0.0/16");

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeIpv4MatchArbitraryBitMask(new MatchBuilder()
                .setLayer3Match(new Ipv4MatchArbitraryBitMaskBuilder()
                        .setIpv4SourceAddressNoMask(leftAddress)
                        .setIpv4SourceArbitraryBitmask(leftMask)
                        .setIpv4DestinationAddressNoMask(leftAddress)
                        .setIpv4DestinationArbitraryBitmask(leftMask)
                        .build()));

        assertEquals(right, ((Ipv4Match) matchBuilder.getLayer3Match()).getIpv4Source());
        assertEquals(right, ((Ipv4Match) matchBuilder.getLayer3Match()).getIpv4Destination());
    }

    @Test
    public void normalizeIpv6Match() {
        final Ipv6Prefix leftPrefix = new Ipv6Prefix("1E3D:5678:9ABC::/24");
        final Ipv6Prefix rightPrefix = new Ipv6Prefix("1e3d:5600:0:0:0:0:0:0/24");
        final MacAddress leftMac = new MacAddress("01:23:45:AB:CD:EF");
        final MacAddress rightMac = new MacAddress("01:23:45:ab:cd:ef");
        final Ipv6Address leftAddress = new Ipv6Address("1E3D:5678:9ABC::");
        final Ipv6Address rightAddress = new Ipv6Address("1e3d:5678:9abc:0:0:0:0:0");

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeIpv6Match(new MatchBuilder()
                .setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6NdSll(leftMac)
                        .setIpv6NdTll(leftMac)
                        .setIpv6NdTarget(leftAddress)
                        .setIpv6Source(leftPrefix)
                        .setIpv6Destination(leftPrefix)
                        .build()));

        assertEquals(rightMac, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6NdSll());
        assertEquals(rightMac, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6NdTll());
        assertEquals(rightPrefix, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6Source());
        assertEquals(rightPrefix, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6Destination());
        assertEquals(rightAddress, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6NdTarget());
    }

    @Test
    public void normalizeIpv6MatchArbitraryBitMask() {
        final Ipv6Address leftAddress = new Ipv6Address("1E3D:5678:9ABC::");
        final Ipv6ArbitraryMask leftMask = new Ipv6ArbitraryMask("FFFF:FF00::");
        final Ipv6Prefix right = new Ipv6Prefix("1e3d:5600:0:0:0:0:0:0/24");

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeIpv6MatchArbitraryBitMask(new MatchBuilder()
                .setLayer3Match(new Ipv6MatchArbitraryBitMaskBuilder()
                        .setIpv6SourceAddressNoMask(leftAddress)
                        .setIpv6SourceArbitraryBitmask(leftMask)
                        .setIpv6DestinationAddressNoMask(leftAddress)
                        .setIpv6DestinationArbitraryBitmask(leftMask)
                        .build()));

        assertEquals(right, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6Source());
        assertEquals(right, ((Ipv6Match) matchBuilder.getLayer3Match()).getIpv6Destination());
    }

    @Test
    public void normalizeEthernetMatch() {
        final MacAddress left = new MacAddress("01:23:45:AB:CD:EF");
        final MacAddress leftMask = new MacAddress("FF:FF:FF:FF:FF:FF");
        final MacAddress right = new MacAddress("01:23:45:ab:cd:ef");

        final MatchBuilder matchBuilder = MatchNormalizationUtil.normalizeEthernetMatch(new MatchBuilder()
                .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetSource(new EthernetSourceBuilder()
                                .setAddress(left)
                                .setMask(leftMask)
                                .build())
                        .setEthernetDestination(new EthernetDestinationBuilder()
                                .setAddress(left)
                                .setMask(leftMask)
                                .build())
                        .build()));

        assertEquals(right, matchBuilder.getEthernetMatch().getEthernetSource().getAddress());
        assertEquals(right, matchBuilder.getEthernetMatch().getEthernetDestination().getAddress());
        assertNull(matchBuilder.getEthernetMatch().getEthernetSource().getMask());
        assertNull(matchBuilder.getEthernetMatch().getEthernetDestination().getMask());
    }
}