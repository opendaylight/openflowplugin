/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.math.BigInteger;
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
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.phy.port._case.InPhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class PacketInMessageFactoryTest {
    private OFSerializer<PacketInMessage> factory;
    private static final byte MESSAGE_TYPE = 10;
    private static final byte PADDING = 2;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, PacketInMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        PacketInMessageBuilder builder = new PacketInMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setBufferId(256L);
        builder.setTotalLen(10);
        builder.setReason(PacketInReason.forValue(0));
        builder.setTableId(new TableId(1L));
        byte[] cookie = new byte[] { (byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01 };
        builder.setCookie(new BigInteger(1, cookie));
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setType(OxmMatchType.class);
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        InPhyPortCaseBuilder inPhyPortCaseBuilder = new InPhyPortCaseBuilder();
        InPhyPortBuilder inPhyPortBuilder = new InPhyPortBuilder();
        inPhyPortBuilder.setPortNumber(new PortNumber(42L));
        inPhyPortCaseBuilder.setInPhyPort(inPhyPortBuilder.build());
        entriesBuilder.setMatchEntryValue(inPhyPortCaseBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn((short) 4);
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        entriesBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        entries.add(entriesBuilder.build());
        matchBuilder.setMatchEntry(entries);
        builder.setMatch(matchBuilder.build());
        byte[] data = ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14");
        builder.setData(data);
        PacketInMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 66);
        Assert.assertEquals("Wrong BufferId", message.getBufferId().longValue(), serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong actions length", message.getTotalLen().intValue(),
                serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong reason", message.getReason().getIntValue(), serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong tableId", message.getTableId().getValue().intValue(),
                serializedBuffer.readUnsignedByte());
        cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        serializedBuffer.readBytes(cookie);
        Assert.assertEquals("Wrong cookie", message.getCookie(), new BigInteger(1, cookie));
        Assert.assertEquals("Wrong match type", 1, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        Assert.assertEquals("Wrong oxm class", 0x8000, serializedBuffer.readUnsignedShort());
        short fieldAndMask = serializedBuffer.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 1, fieldAndMask >> 1);
        serializedBuffer.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        Assert.assertEquals("Wrong oxm value", 42, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong oxm class", 0x8000, serializedBuffer.readUnsignedShort());
        fieldAndMask = serializedBuffer.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 9, fieldAndMask >> 1);
        serializedBuffer.skipBytes(EncodeConstants.SIZE_OF_BYTE_IN_BYTES);
        Assert.assertEquals("Wrong oxm value", 4, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(7);
        serializedBuffer.skipBytes(PADDING);
        byte[] readData = new byte[serializedBuffer.readableBytes()];
        serializedBuffer.readBytes(readData);
        Assert.assertArrayEquals("Wrong data", message.getData(), readData);
    }

}
