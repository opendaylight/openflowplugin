/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.StandardMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PacketType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsLabelCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsTcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PacketTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;

/**
 * Unit tests for MatchDeserializer.
 *
 * @author michal.polkorab
 */
public class MatchDeserializerTest {

    private OFDeserializer<Match> matchDeserializer;
    private DeserializerRegistry registry;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        registry = new DeserializerRegistryImpl();
        registry.init();
        matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID,
                        EncodeConstants.EMPTY_VALUE, Match.class));
    }

    /**
     * Testing Ipv4 address deserialization.
     */
    @Test
    public void testIpv4Address() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("80 00 18 04 00 01 02 03");

        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                0x8000, 12);
        key.setExperimenterId(null);
        OFDeserializer<MatchEntry> entryDeserializer = registry.getDeserializer(key);
        MatchEntry entry = entryDeserializer.deserialize(buffer);
        Assert.assertEquals("Wrong Ipv4 address format", new Ipv4Address("0.1.2.3"),
                ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst().getIpv4Address());
    }

    /**
     * Testing Ipv6 address deserialization.
     */
    @Test
    public void testIpv6Address() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("80 00 34 10 00 00 00 01 00 02 00 03 00 04 00 05 00 06 0F 07");

        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                0x8000, 26);
        key.setExperimenterId(null);
        OFDeserializer<MatchEntry> entryDeserializer = registry.getDeserializer(key);
        MatchEntry entry = entryDeserializer.deserialize(buffer);
        Assert.assertEquals("Wrong Ipv6 address format", new Ipv6Address("0:1:2:3:4:5:6:f07"),
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address());
    }

    /**
     * Testing match deserialization.
     */
    @Test
    public void testMatch() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("00 01 01 B0 "
                + "80 00 00 04 00 00 00 01 "
                + "80 00 02 04 00 00 00 02 "
                + "80 00 05 10 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 04 "
                + "80 00 07 0C 00 00 00 00 00 05 00 00 00 00 00 06 "
                + "80 00 09 0C 00 00 00 00 00 07 00 00 00 00 00 08 "
                + "80 00 0A 02 00 09 "
                + "80 00 0D 04 00 0A 00 0B "
                + "80 00 0E 01 0C "
                + "80 00 10 01 0D "
                + "80 00 12 01 0E "
                + "80 00 14 01 0F "
                + "80 00 17 08 0A 00 00 01 00 00 FF 00 "
                + "80 00 19 08 0A 00 00 02 00 00 00 FF "
                + "80 00 1A 02 00 03 "
                + "80 00 1C 02 00 04 "
                + "80 00 1E 02 00 05 "
                + "80 00 20 02 00 06 "
                + "80 00 22 02 00 07 "
                + "80 00 24 02 00 08 "
                + "80 00 26 01 05 "
                + "80 00 28 01 07 "
                + "80 00 2A 02 00 10 "
                + "80 00 2D 08 0A 00 00 09 00 00 FF 00 "
                + "80 00 2F 08 0A 00 00 0A 00 00 00 FF "
                + "80 00 31 0C 00 00 00 00 00 01 00 00 00 00 00 03 "
                + "80 00 33 0C 00 00 00 00 00 02 00 00 00 00 00 04 "
                + "80 00 35 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 15 "
                +             "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16 "
                + "80 00 37 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 17 "
                +             "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 18 "
                + "80 00 38 04 00 00 00 02 "
                + "80 00 3A 01 15 "
                + "80 00 3C 01 17 "
                + "80 00 3E 10 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 20 " //ipv6ndtarget
                + "80 00 40 06 00 05 00 00 00 01 "
                + "80 00 42 06 00 05 00 00 00 02 "
                + "80 00 44 04 00 00 02 03 "
                + "80 00 46 01 03 "
                + "80 00 48 01 01 "
                + "80 00 4B 06 00 00 02 00 00 01 "
                + "80 00 4D 10 00 00 00 00 00 00 00 07 00 00 00 00 00 00 00 FF "
                + "80 00 4F 04 00 00 03 04 "
                + "80 00 58 04 00 01 89 4f");

        Match match = matchDeserializer.deserialize(buffer);
        Assert.assertEquals("Wrong match type", OxmMatchType.class, match.getType());
        Assert.assertEquals("Wrong match entries size", 41, match.getMatchEntry().size());
        List<MatchEntry> entries = match.getMatchEntry();
        MatchEntry entry0 = entries.get(0);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry0.getOxmClass());
        Assert.assertEquals("Wrong entry field", InPort.class, entry0.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry0.getHasMask());
        Assert.assertEquals("Wrong entry value", 1,
                ((InPortCase) entry0.getMatchEntryValue()).getInPort().getPortNumber().getValue().intValue());
        MatchEntry entry1 = entries.get(1);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry1.getOxmClass());
        Assert.assertEquals("Wrong entry field", InPhyPort.class, entry1.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry1.getHasMask());
        Assert.assertEquals("Wrong entry value", 2,
                ((InPhyPortCase) entry1.getMatchEntryValue()).getInPhyPort().getPortNumber().getValue().intValue());
        MatchEntry entry2 = entries.get(2);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry2.getOxmClass());
        Assert.assertEquals("Wrong entry field", Metadata.class, entry2.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry2.getHasMask());
        Assert.assertArrayEquals("Wrong entry value", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 03"),
                ((MetadataCase) entry2.getMatchEntryValue()).getMetadata().getMetadata());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 04"),
                ((MetadataCase) entry2.getMatchEntryValue()).getMetadata().getMask());
        MatchEntry entry3 = entries.get(3);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry3.getOxmClass());
        Assert.assertEquals("Wrong entry field", EthDst.class, entry3.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry3.getHasMask());
        Assert.assertEquals("Wrong entry value", new MacAddress("00:00:00:00:00:05"),
                ((EthDstCase) entry3.getMatchEntryValue()).getEthDst().getMacAddress());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 00 00 06"),
                ((EthDstCase) entry3.getMatchEntryValue()).getEthDst().getMask());
        MatchEntry entry4 = entries.get(4);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry4.getOxmClass());
        Assert.assertEquals("Wrong entry field", EthSrc.class, entry4.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry4.getHasMask());
        Assert.assertEquals("Wrong entry value", new MacAddress("00:00:00:00:00:07"),
                ((EthSrcCase) entry4.getMatchEntryValue()).getEthSrc().getMacAddress());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 00 00 08"),
                ((EthSrcCase) entry4.getMatchEntryValue()).getEthSrc().getMask());
        MatchEntry entry5 = entries.get(5);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry5.getOxmClass());
        Assert.assertEquals("Wrong entry field", EthType.class, entry5.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry5.getHasMask());
        Assert.assertEquals("Wrong entry value", 9,
                ((EthTypeCase) entry5.getMatchEntryValue()).getEthType().getEthType().getValue().intValue());
        MatchEntry entry6 = entries.get(6);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry6.getOxmClass());
        Assert.assertEquals("Wrong entry field", VlanVid.class, entry6.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry6.getHasMask());
        Assert.assertEquals("Wrong entry value", 10,
                ((VlanVidCase) entry6.getMatchEntryValue()).getVlanVid().getVlanVid().intValue());
        Assert.assertEquals("Wrong entry value", false,
                ((VlanVidCase) entry6.getMatchEntryValue()).getVlanVid().getCfiBit());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 0B"),
                ((VlanVidCase) entry6.getMatchEntryValue()).getVlanVid().getMask());
        MatchEntry entry7 = entries.get(7);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry7.getOxmClass());
        Assert.assertEquals("Wrong entry field", VlanPcp.class, entry7.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry7.getHasMask());
        Assert.assertEquals("Wrong entry value", 12,
                ((VlanPcpCase) entry7.getMatchEntryValue()).getVlanPcp().getVlanPcp().intValue());
        MatchEntry entry8 = entries.get(8);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry8.getOxmClass());
        Assert.assertEquals("Wrong entry field", IpDscp.class, entry8.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry8.getHasMask());
        Assert.assertEquals("Wrong entry value", 13,
                ((IpDscpCase) entry8.getMatchEntryValue()).getIpDscp().getDscp().getValue().intValue());
        MatchEntry entry9 = entries.get(9);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry9.getOxmClass());
        Assert.assertEquals("Wrong entry field", IpEcn.class, entry9.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry9.getHasMask());
        Assert.assertEquals("Wrong entry value", 14,
                ((IpEcnCase) entry9.getMatchEntryValue()).getIpEcn().getEcn().intValue());
        MatchEntry entry10 = entries.get(10);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry10.getOxmClass());
        Assert.assertEquals("Wrong entry field", IpProto.class, entry10.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry10.getHasMask());
        Assert.assertEquals("Wrong entry value", 15,
                ((IpProtoCase) entry10.getMatchEntryValue()).getIpProto().getProtocolNumber().intValue());
        MatchEntry entry11 = entries.get(11);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry11.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv4Src.class, entry11.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry11.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv4Address("10.0.0.1"),
                ((Ipv4SrcCase) entry11.getMatchEntryValue()).getIpv4Src().getIpv4Address());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 FF 00"),
                ((Ipv4SrcCase) entry11.getMatchEntryValue()).getIpv4Src().getMask());
        MatchEntry entry12 = entries.get(12);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry12.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv4Dst.class, entry12.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry12.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv4Address("10.0.0.2"),
                ((Ipv4DstCase) entry12.getMatchEntryValue()).getIpv4Dst().getIpv4Address());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 FF"),
                ((Ipv4DstCase) entry12.getMatchEntryValue()).getIpv4Dst().getMask());
        MatchEntry entry13 = entries.get(13);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry13.getOxmClass());
        Assert.assertEquals("Wrong entry field", TcpSrc.class, entry13.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry13.getHasMask());
        Assert.assertEquals("Wrong entry value", 3,
                ((TcpSrcCase) entry13.getMatchEntryValue()).getTcpSrc().getPort().getValue().intValue());
        MatchEntry entry14 = entries.get(14);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry14.getOxmClass());
        Assert.assertEquals("Wrong entry field", TcpDst.class, entry14.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry14.getHasMask());
        Assert.assertEquals("Wrong entry value", 4,
                ((TcpDstCase) entry14.getMatchEntryValue()).getTcpDst().getPort().getValue().intValue());
        MatchEntry entry15 = entries.get(15);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry15.getOxmClass());
        Assert.assertEquals("Wrong entry field", UdpSrc.class, entry15.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry15.getHasMask());
        Assert.assertEquals("Wrong entry value", 5,
                ((UdpSrcCase) entry15.getMatchEntryValue()).getUdpSrc().getPort().getValue().intValue());
        MatchEntry entry16 = entries.get(16);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry16.getOxmClass());
        Assert.assertEquals("Wrong entry field", UdpDst.class, entry16.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry16.getHasMask());
        Assert.assertEquals("Wrong entry value", 6,
                ((UdpDstCase) entry16.getMatchEntryValue()).getUdpDst().getPort().getValue().intValue());
        MatchEntry entry17 = entries.get(17);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry17.getOxmClass());
        Assert.assertEquals("Wrong entry field", SctpSrc.class, entry17.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry17.getHasMask());
        Assert.assertEquals("Wrong entry value", 7,
                ((SctpSrcCase) entry17.getMatchEntryValue()).getSctpSrc().getPort().getValue().intValue());
        MatchEntry entry18 = entries.get(18);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry18.getOxmClass());
        Assert.assertEquals("Wrong entry field", SctpDst.class, entry18.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry18.getHasMask());
        Assert.assertEquals("Wrong entry value", 8,
                ((SctpDstCase) entry18.getMatchEntryValue()).getSctpDst().getPort().getValue().intValue());
        MatchEntry entry19 = entries.get(19);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry19.getOxmClass());
        Assert.assertEquals("Wrong entry field", Icmpv4Type.class, entry19.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry19.getHasMask());
        Assert.assertEquals("Wrong entry value", 5,
                ((Icmpv4TypeCase) entry19.getMatchEntryValue()).getIcmpv4Type().getIcmpv4Type().intValue());
        MatchEntry entry20 = entries.get(20);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry20.getOxmClass());
        Assert.assertEquals("Wrong entry field", Icmpv4Code.class, entry20.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry20.getHasMask());
        Assert.assertEquals("Wrong entry value", 7,
                ((Icmpv4CodeCase) entry20.getMatchEntryValue()).getIcmpv4Code().getIcmpv4Code().intValue());
        MatchEntry entry21 = entries.get(21);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry21.getOxmClass());
        Assert.assertEquals("Wrong entry field", ArpOp.class, entry21.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry21.getHasMask());
        Assert.assertEquals("Wrong entry value", 16,
                ((ArpOpCase) entry21.getMatchEntryValue()).getArpOp().getOpCode().intValue());
        MatchEntry entry22 = entries.get(22);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry22.getOxmClass());
        Assert.assertEquals("Wrong entry field", ArpSpa.class, entry22.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry22.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv4Address("10.0.0.9"),
                ((ArpSpaCase) entry22.getMatchEntryValue()).getArpSpa().getIpv4Address());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 FF 00"),
                ((ArpSpaCase) entry22.getMatchEntryValue()).getArpSpa().getMask());
        MatchEntry entry23 = entries.get(23);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry23.getOxmClass());
        Assert.assertEquals("Wrong entry field", ArpTpa.class, entry23.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry23.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv4Address("10.0.0.10"),
                ((ArpTpaCase) entry23.getMatchEntryValue()).getArpTpa().getIpv4Address());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 FF"),
                ((ArpTpaCase) entry23.getMatchEntryValue()).getArpTpa().getMask());
        MatchEntry entry24 = entries.get(24);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry24.getOxmClass());
        Assert.assertEquals("Wrong entry field", ArpSha.class, entry24.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry24.getHasMask());
        Assert.assertEquals("Wrong entry value", new MacAddress("00:00:00:00:00:01"),
                ((ArpShaCase) entry24.getMatchEntryValue()).getArpSha().getMacAddress());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 00 00 03"),
                ((ArpShaCase) entry24.getMatchEntryValue()).getArpSha().getMask());
        MatchEntry entry25 = entries.get(25);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry25.getOxmClass());
        Assert.assertEquals("Wrong entry field", ArpTha.class, entry25.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry25.getHasMask());
        Assert.assertEquals("Wrong entry value", new MacAddress("00:00:00:00:00:02"),
                ((ArpThaCase) entry25.getMatchEntryValue()).getArpTha().getMacAddress());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 00 00 04"),
                ((ArpThaCase) entry25.getMatchEntryValue()).getArpTha().getMask());
        MatchEntry entry26 = entries.get(26);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry26.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Src.class, entry26.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry26.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv6Address("::15"),
                ((Ipv6SrcCase) entry26.getMatchEntryValue()).getIpv6Src().getIpv6Address());
        Assert.assertArrayEquals("Wrong entry mask",
                ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 16"),
                ((Ipv6SrcCase) entry26.getMatchEntryValue()).getIpv6Src().getMask());
        MatchEntry entry27 = entries.get(27);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry27.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Dst.class, entry27.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry27.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv6Address("::17"),
                ((Ipv6DstCase) entry27.getMatchEntryValue()).getIpv6Dst().getIpv6Address());
        Assert.assertArrayEquals("Wrong entry mask",
                ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 18"),
                ((Ipv6DstCase) entry27.getMatchEntryValue()).getIpv6Dst().getMask());
        MatchEntry entry28 = entries.get(28);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry28.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Flabel.class, entry28.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry28.getHasMask());
        Assert.assertEquals("Wrong entry value", 2,
                ((Ipv6FlabelCase) entry28.getMatchEntryValue()).getIpv6Flabel().getIpv6Flabel()
                .getValue().intValue());
        MatchEntry entry29 = entries.get(29);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry29.getOxmClass());
        Assert.assertEquals("Wrong entry field", Icmpv6Type.class, entry29.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry29.getHasMask());
        Assert.assertEquals("Wrong entry value", 21,
                ((Icmpv6TypeCase) entry29.getMatchEntryValue()).getIcmpv6Type().getIcmpv6Type().intValue());
        MatchEntry entry30 = entries.get(30);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry30.getOxmClass());
        Assert.assertEquals("Wrong entry field", Icmpv6Code.class, entry30.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry30.getHasMask());
        Assert.assertEquals("Wrong entry value", 23,
                ((Icmpv6CodeCase) entry30.getMatchEntryValue()).getIcmpv6Code().getIcmpv6Code().intValue());
        MatchEntry entry31 = entries.get(31);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry31.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6NdTarget.class, entry31.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry31.getHasMask());
        Assert.assertEquals("Wrong entry value", new Ipv6Address("::20"),
                ((Ipv6NdTargetCase) entry31.getMatchEntryValue()).getIpv6NdTarget().getIpv6Address());
        MatchEntry entry32 = entries.get(32);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry32.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6NdSll.class, entry32.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry32.getHasMask());
        Assert.assertEquals("Wrong entry value", new MacAddress("00:05:00:00:00:01"),
                ((Ipv6NdSllCase) entry32.getMatchEntryValue()).getIpv6NdSll().getMacAddress());
        MatchEntry entry33 = entries.get(33);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry33.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6NdTll.class, entry33.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry33.getHasMask());
        Assert.assertEquals("Wrong entry value", new MacAddress("00:05:00:00:00:02"),
                ((Ipv6NdTllCase) entry33.getMatchEntryValue()).getIpv6NdTll().getMacAddress());
        MatchEntry entry34 = entries.get(34);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry34.getOxmClass());
        Assert.assertEquals("Wrong entry field", MplsLabel.class, entry34.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry34.getHasMask());
        Assert.assertEquals("Wrong entry value", 515,
                ((MplsLabelCase) entry34.getMatchEntryValue()).getMplsLabel().getMplsLabel().intValue());
        MatchEntry entry35 = entries.get(35);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry35.getOxmClass());
        Assert.assertEquals("Wrong entry field", MplsTc.class, entry35.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry35.getHasMask());
        Assert.assertEquals("Wrong entry value", 3,
                ((MplsTcCase) entry35.getMatchEntryValue()).getMplsTc().getTc().intValue());
        MatchEntry entry36 = entries.get(36);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry36.getOxmClass());
        Assert.assertEquals("Wrong entry field", MplsBos.class, entry36.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry36.getHasMask());
        Assert.assertEquals("Wrong entry value", true,
                ((MplsBosCase) entry36.getMatchEntryValue()).getMplsBos().getBos());
        MatchEntry entry37 = entries.get(37);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry37.getOxmClass());
        Assert.assertEquals("Wrong entry field", PbbIsid.class, entry37.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry37.getHasMask());
        Assert.assertEquals("Wrong entry value", 2,
                ((PbbIsidCase) entry37.getMatchEntryValue()).getPbbIsid().getIsid().intValue());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 01"),
                ((PbbIsidCase) entry37.getMatchEntryValue()).getPbbIsid().getMask());
        MatchEntry entry38 = entries.get(38);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry38.getOxmClass());
        Assert.assertEquals("Wrong entry field", TunnelId.class, entry38.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry38.getHasMask());
        Assert.assertArrayEquals("Wrong entry value", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 07"),
                ((TunnelIdCase) entry38.getMatchEntryValue()).getTunnelId().getTunnelId());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("00 00 00 00 00 00 00 FF"),
                ((TunnelIdCase) entry38.getMatchEntryValue()).getTunnelId().getMask());
        MatchEntry entry39 = entries.get(39);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry39.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv6Exthdr.class, entry39.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", true, entry39.getHasMask());
        Assert.assertEquals("Wrong entry value",
                new Ipv6ExthdrFlags(false, false, false, false, false, false, false, false, false),
                ((Ipv6ExthdrCase) entry39.getMatchEntryValue()).getIpv6Exthdr().getPseudoField());
        Assert.assertArrayEquals("Wrong entry mask", ByteBufUtils.hexStringToBytes("03 04"),
                ((Ipv6ExthdrCase) entry39.getMatchEntryValue()).getIpv6Exthdr().getMask());
        Assert.assertTrue("Unread data", buffer.readableBytes() == 0);
        MatchEntry entry40 = entries.get(40);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry40.getOxmClass());
        Assert.assertEquals("Wrong entry field", PacketType.class, entry40.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry40.getHasMask());
        Assert.assertEquals("Wrong entry value", 0x1894f,
                ((PacketTypeCase) entry40.getMatchEntryValue()).getPacketType().getPacketType().longValue());
    }

    /**
     * Testing header deserialization.
     */
    @Test
    public void testHeaders() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("80 00 18 04 00 01 02 03");

        MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(EncodeConstants.OF13_VERSION_ID,
                0x8000, 12);
        key.setExperimenterId(null);
        HeaderDeserializer<MatchEntry> entryDeserializer = registry.getDeserializer(key);
        MatchEntry entry = entryDeserializer.deserializeHeader(buffer);
        Assert.assertEquals("Wrong entry class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong entry field", Ipv4Dst.class, entry.getOxmMatchField());
        Assert.assertEquals("Wrong entry hasMask", false, entry.getHasMask());
        Assert.assertNull("Wrong Ipv4 address", entry.getMatchEntryValue());
    }

    /**
     * Testing standard match type.
     */
    @Test
    public void testStandardMatch() {
        ByteBuf buffer = ByteBufUtils.hexStringToByteBuf("00 00 00 10 80 00 04 08 00 00 00 00 00 00 00 01");

        Match match = matchDeserializer.deserialize(buffer);

        Assert.assertEquals("Wrong match type", StandardMatchType.class, match.getType());
        Assert.assertEquals("Wrong match entries size", 1, match.getMatchEntry().size());
    }
}
