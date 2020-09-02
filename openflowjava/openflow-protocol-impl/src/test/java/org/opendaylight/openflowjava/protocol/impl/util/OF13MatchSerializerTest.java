/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.StandardMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.flabel._case.Ipv6FlabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.target._case.Ipv6NdTargetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for OF13MatchSerializer.
 *
 * @author michal.polkorab
 */
public class OF13MatchSerializerTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(OF13MatchSerializerTest.class);
    private SerializerRegistry registry;
    private OFSerializer<Match> matchSerializer;

    /**
     * Initializes serializer table and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        matchSerializer = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class));
    }

    /**
     * Test for correct serialization of Ipv4Address match entry.
     */
    @Test
    public void testIpv4Src() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Src.class);
        entriesBuilder.setHasMask(false);
        Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
        Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();
        ipv4SrcBuilder.setIpv4Address(new Ipv4Address("1.2.3.4"));
        ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);
        Match match = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong type", 1, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 22, out.readUnsignedByte());
        out.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong ip address (first number)", 1, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip address (second number)", 2, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip address (third number)", 3, out.readUnsignedByte());
        Assert.assertEquals("Wrong ip address (fourth number)", 4, out.readUnsignedByte());
    }

    /**
     * Test for correct serialization of Ipv6Address match entry.
     */
    @Test
    public void testIpv6Various() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        // ipv6 match entry with correct Ipv6 address
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Src.class);
        entriesBuilder.setHasMask(false);
        Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
        Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
        ipv6SrcBuilder.setIpv6Address(new Ipv6Address("1:2:3:4:5:6:7:8"));
        ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
        entries.add(entriesBuilder.build());
        // ipv6 match entry with abbreviated Ipv6 address
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdTarget.class);
        entriesBuilder.setHasMask(false);
        Ipv6NdTargetCaseBuilder ipv6NdTargetCaseBuilder = new Ipv6NdTargetCaseBuilder();
        Ipv6NdTargetBuilder ipv6NdTargetBuilder = new Ipv6NdTargetBuilder();
        ipv6NdTargetBuilder.setIpv6Address(new Ipv6Address("1:2::6:7:8"));
        ipv6NdTargetCaseBuilder.setIpv6NdTarget(ipv6NdTargetBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6NdTargetCaseBuilder.build());
        entries.add(entriesBuilder.build());
        // ipv6 match entry with abbreviated Ipv6 address
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Dst.class);
        entriesBuilder.setHasMask(false);
        Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
        Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
        ipv6DstBuilder.setIpv6Address(new Ipv6Address("1::8"));
        ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
        entries.add(entriesBuilder.build());
        // ipv6 match entry with abbreviated Ipv6 address
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Dst.class);
        entriesBuilder.setHasMask(false);
        ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
        ipv6DstBuilder = new Ipv6DstBuilder();
        ipv6DstBuilder.setIpv6Address(new Ipv6Address("::1"));
        ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
        entries.add(entriesBuilder.build());
        // ipv6 match entry with abbreviated Ipv6 address
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Dst.class);
        entriesBuilder.setHasMask(false);
        ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
        ipv6DstBuilder = new Ipv6DstBuilder();
        ipv6DstBuilder.setIpv6Address(new Ipv6Address("::"));
        ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);
        Match match = builder.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong type", 1, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 52, out.readUnsignedByte());
        Assert.assertEquals("Wrong entry length", 16, out.readUnsignedByte());
        Assert.assertEquals("Wrong ipv6 address", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 5, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 7, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 62, out.readUnsignedByte());
        Assert.assertEquals("Wrong entry length", 16, out.readUnsignedByte());
        Assert.assertEquals("Wrong ipv6 address", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 6, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 7, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 54, out.readUnsignedByte());
        Assert.assertEquals("Wrong entry length", 16, out.readUnsignedByte());
        Assert.assertEquals("Wrong ipv6 address", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 54, out.readUnsignedByte());
        Assert.assertEquals("Wrong entry length", 16, out.readUnsignedByte());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 54, out.readUnsignedByte());
        Assert.assertEquals("Wrong entry length", 16, out.readUnsignedByte());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong ipv6 address", 0, out.readUnsignedShort());
    }

    /**
     * Test for correct serialization of incorrect Ipv6Address match entry.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIpv6Incorrect() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        // ipv6 match entry with incorrect Ipv6 address
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Src.class);
        entriesBuilder.setHasMask(false);
        Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
        Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
        ipv6SrcBuilder.setIpv6Address(new Ipv6Address("1:2::::8"));
        ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);
        Match match = builder.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        matchSerializer.serialize(match, out);
    }

    /**
     * Test for correct serialization of Ipv4Address match entry.
     */
    @Test
    public void testIpv6Flabel() {
        Match match = buildIpv6FLabelMatch(0x0f9e8dL, false, null);

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong type", 1, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 56, out.readUnsignedByte());
        out.skipBytes(Byte.BYTES);
        byte[] label = new byte[4];
        out.readBytes(label);

        LOG.debug("label: {}", ByteBufUtils.bytesToHexString(label));
        Assert.assertArrayEquals("Wrong ipv6FLabel", new byte[]{0, 0x0f, (byte) 0x9e, (byte) 0x8d}, label);
    }

    /**
     * Test for correct serialization of Ipv4Address match entry.
     */
    @Test
    public void testIpv6FlabelWithMask() {
        Match match = buildIpv6FLabelMatch(0x0f9e8dL, true, new byte[]{0, 1, 2, 3});

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong type", 1, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong class", 0x8000, out.readUnsignedShort());
        Assert.assertEquals("Wrong field and mask", 57, out.readUnsignedByte());
        out.skipBytes(Byte.BYTES);
        byte[] label = new byte[4];
        out.readBytes(label);
        Assert.assertArrayEquals("Wrong ipv6FLabel", new byte[]{0, 0x0f, (byte) 0x9e, (byte) 0x8d}, label);
        byte[] mask = new byte[4];
        out.readBytes(mask);
        Assert.assertArrayEquals("Wrong ipv6FLabel mask", new byte[]{0, 1, 2, 3}, mask);
    }

    /**
     * Test for correct serialization of Ipv4Address match entry with wrong mask.
     */
    @Test
    public void testIpv6FlabelWithMaskBad() {
        Match match = buildIpv6FLabelMatch(0x0f9e8dL, true, new byte[]{0x0c, 0x7b, 0x6a});
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();

        try {
            matchSerializer.serialize(match, out);
            Assert.fail("incorrect length of mask ignored");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    private static Match buildIpv6FLabelMatch(long labelValue, boolean hasMask, byte[] mask) {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Flabel.class);
        entriesBuilder.setHasMask(hasMask);
        Ipv6FlabelCaseBuilder ipv6FlabelCaseBuilder = new Ipv6FlabelCaseBuilder();
        Ipv6FlabelBuilder ipv6FlabelBuilder = new Ipv6FlabelBuilder();
        ipv6FlabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(labelValue));
        ipv6FlabelBuilder.setMask(mask);
        ipv6FlabelCaseBuilder.setIpv6Flabel(ipv6FlabelBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6FlabelCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);
        Match match = builder.build();
        return match;
    }

    /**
     * Test Standard match type.
     */
    @Test
    public void testStandardMatchType() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(StandardMatchType.class);
        Match match = builder.build();
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();

        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong match type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong match length", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong padding", 0, out.readUnsignedInt());
        Assert.assertEquals("Unexpected data", 0, out.readableBytes());
    }

    /**
     * Test serialize experimenter match entry - with no experimenter
     * match entry serializer registered.
     */
    @Test(expected = IllegalStateException.class)
    public void testSerializeExperimenterMatchEntry() {
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(ExperimenterClass.class);
        builder.setOxmMatchField(OxmMatchFieldClass.class);
        builder.setHasMask(true);
        ExperimenterIdCaseBuilder caseBuilder = new ExperimenterIdCaseBuilder();
        ExperimenterBuilder expBuilder = new ExperimenterBuilder();
        expBuilder.setExperimenter(new ExperimenterId(42L));
        caseBuilder.setExperimenter(expBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
        entries.add(builder.build());
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();

        ((OF13MatchSerializer) matchSerializer).serializeMatchEntries(entries, out);
    }

    private interface OxmMatchFieldClass extends MatchField {
        // only for testing purposes
    }
}
