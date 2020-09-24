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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for FlowRemovedMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class FlowRemovedMessageFactoryTest {
    private OFSerializer<FlowRemovedMessage> factory;
    private static final byte MESSAGE_TYPE = 11;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, FlowRemovedMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        FlowRemovedMessageBuilder builder = new FlowRemovedMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setCookie(Uint64.valueOf(1234));
        builder.setPriority(Uint16.valueOf(1234));
        builder.setReason(FlowRemovedReason.forValue(2));
        builder.setTableId(new TableId(Uint32.valueOf(65)));
        builder.setDurationSec(Uint32.valueOf(1234));
        builder.setDurationNsec(Uint32.valueOf(1234));
        builder.setIdleTimeout(Uint16.valueOf(1234));
        builder.setHardTimeout(Uint16.valueOf(1234));
        builder.setPacketCount(Uint64.valueOf(1234));
        builder.setByteCount(Uint64.valueOf(1234));
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        InPhyPortCaseBuilder inPhyPortCaseBuilder = new InPhyPortCaseBuilder();
        InPhyPortBuilder inPhyPortBuilder = new InPhyPortBuilder();
        inPhyPortBuilder.setPortNumber(new PortNumber(Uint32.valueOf(42)));
        inPhyPortCaseBuilder.setInPhyPort(inPhyPortBuilder.build());
        entriesBuilder.setMatchEntryValue(inPhyPortCaseBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn(Uint8.valueOf(4));
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        entriesBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        entries.add(entriesBuilder.build());
        matchBuilder.setMatchEntry(entries);
        builder.setMatch(matchBuilder.build());
        final FlowRemovedMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();

        // simulate parent message
        serializedBuffer.writeInt(1);
        serializedBuffer.writeZero(2);
        serializedBuffer.writeShort(3);

        factory.serialize(message, serializedBuffer);

        // read parent message
        serializedBuffer.readInt();
        serializedBuffer.skipBytes(2);
        serializedBuffer.readShort();

        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 72);
        Assert.assertEquals("Wrong cookie", message.getCookie().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong priority", message.getPriority().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong reason", message.getReason().getIntValue(), serializedBuffer.readByte());
        Assert.assertEquals("Wrong Table ID", message.getTableId().getValue().intValue(),
                serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong duration sec", message.getDurationSec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong duration nsec", message.getDurationNsec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong Idle timeout", message.getIdleTimeout().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong Hard timeout", message.getIdleTimeout().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong Packet count", message.getPacketCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong Byte count", message.getByteCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong match type", 1, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong oxm class", 0x8000, serializedBuffer.readUnsignedShort());
        short fieldAndMask = serializedBuffer.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 1, fieldAndMask >> 1);
        serializedBuffer.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong oxm value", 42, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong oxm class", 0x8000, serializedBuffer.readUnsignedShort());
        fieldAndMask = serializedBuffer.readUnsignedByte();
        Assert.assertEquals("Wrong oxm hasMask", 0, fieldAndMask & 1);
        Assert.assertEquals("Wrong oxm field", 9, fieldAndMask >> 1);
        serializedBuffer.skipBytes(Byte.BYTES);
        Assert.assertEquals("Wrong oxm value", 4, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(7);
    }

}
