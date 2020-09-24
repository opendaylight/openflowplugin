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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10FeaturesReplyMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10FeaturesReplyMessageFactoryTest {
    private OFSerializer<GetFeaturesOutput> factory;
    private static final byte MESSAGE_TYPE = 6;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, GetFeaturesOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        GetFeaturesOutputBuilder builder = new GetFeaturesOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setDatapathId(Uint64.ONE);
        builder.setBuffers(Uint32.ONE);
        builder.setTables(Uint8.ONE);
        builder.setCapabilitiesV10(new CapabilitiesV10(true, false, true, false, true, false, true, false));
        builder.setActionsV10(
                new ActionTypeV10(true, false, true, false, true, false, true, false, true, false, true, false, true));
        builder.setPhyPort(createPorts());
        GetFeaturesOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 80);
        Assert.assertEquals("Wrong datapath id", message.getDatapathId().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong n buffers", message.getBuffers().longValue(), serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong n tables", message.getTables().shortValue(), serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(3);
        Assert.assertEquals("Wrong capabilities", message.getCapabilitiesV10(),
                createCapabilities(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong actions", message.getActionsV10(), createActionsV10(serializedBuffer.readInt()));
        PhyPort port = message.getPhyPort().get(0);
        Assert.assertEquals("Wrong port No", port.getPortNo().intValue(), serializedBuffer.readShort());
        byte[] address = new byte[6];
        serializedBuffer.readBytes(address);
        Assert.assertEquals("Wrong MacAddress", port.getHwAddr().getValue().toLowerCase(),
                new MacAddress(ByteBufUtils.macAddressToString(address)).getValue().toLowerCase());
        byte[] name = new byte[16];
        serializedBuffer.readBytes(name);
        Assert.assertEquals("Wrong name", port.getName(), new String(name).trim());
        Assert.assertEquals("Wrong config", port.getConfigV10(), createPortConfig(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong state", port.getStateV10(), createPortState(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong current", port.getCurrentFeaturesV10(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong advertised", port.getAdvertisedFeaturesV10(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong supported", port.getSupportedFeaturesV10(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong peer", port.getPeerFeaturesV10(), createPortFeatures(serializedBuffer.readInt()));

    }

    private static List<PhyPort> createPorts() {
        final List<PhyPort> ports = new ArrayList<>();
        PhyPortBuilder builder = new PhyPortBuilder();
        builder.setPortNo(Uint32.ONE);
        builder.setHwAddr(new MacAddress("94:de:80:a6:61:40"));
        builder.setName("Port name");
        builder.setConfigV10(new PortConfigV10(true, false, true, false, true, false, true));
        builder.setStateV10(new PortStateV10(true, false, true, false, true, false, true, false));
        builder.setCurrentFeaturesV10(
                new PortFeaturesV10(true, false, true, false, true, false, true, false, true, false, true, false));
        builder.setAdvertisedFeaturesV10(
                new PortFeaturesV10(true, false, true, false, true, false, true, false, true, false, true, false));
        builder.setSupportedFeaturesV10(
                new PortFeaturesV10(true, false, true, false, true, false, true, false, true, false, true, false));
        builder.setPeerFeaturesV10(
                new PortFeaturesV10(true, false, true, false, true, false, true, false, true, false, true, false));
        ports.add(builder.build());
        return ports;
    }

    private static PortConfigV10 createPortConfig(long input) {
        final Boolean _portDown = (input & 1 << 0) > 0;
        final Boolean _noStp = (input & 1 << 1) > 0;
        final Boolean _noRecv = (input & 1 << 2) > 0;
        final Boolean _noRecvStp = (input & 1 << 3) > 0;
        final Boolean _noFlood = (input & 1 << 4) > 0;
        final Boolean _noFwd = (input & 1 << 5) > 0;
        final Boolean _noPacketIn = (input & 1 << 6) > 0;
        return new PortConfigV10(_noFlood, _noFwd, _noPacketIn, _noRecv, _noRecvStp, _noStp, _portDown);
    }

    private static PortFeaturesV10 createPortFeatures(long input) {
        final Boolean _10mbHd = (input & 1 << 0) > 0;
        final Boolean _10mbFd = (input & 1 << 1) > 0;
        final Boolean _100mbHd = (input & 1 << 2) > 0;
        final Boolean _100mbFd = (input & 1 << 3) > 0;
        final Boolean _1gbHd = (input & 1 << 4) > 0;
        final Boolean _1gbFd = (input & 1 << 5) > 0;
        final Boolean _10gbFd = (input & 1 << 6) > 0;
        final Boolean _copper = (input & 1 << 7) > 0;
        final Boolean _fiber = (input & 1 << 8) > 0;
        final Boolean _autoneg = (input & 1 << 9) > 0;
        final Boolean _pause = (input & 1 << 10) > 0;
        final Boolean _pauseAsym = (input & 1 << 11) > 0;
        return new PortFeaturesV10(_100mbFd, _100mbHd, _10gbFd, _10mbFd, _10mbHd, _1gbFd, _1gbHd, _autoneg, _copper,
                _fiber, _pause, _pauseAsym);
    }

    private static PortStateV10 createPortState(long input) {
        final Boolean _linkDown = (input & 1 << 0) > 0;
        final Boolean _blocked = (input & 1 << 1) > 0;
        final Boolean _live = (input & 1 << 2) > 0;
        final Boolean _stpListen = (input & 1 << 3) > 0;
        final Boolean _stpLearn = (input & 1 << 4) > 0;
        final Boolean _stpForward = (input & 1 << 5) > 0;
        final Boolean _stpBlock = (input & 1 << 6) > 0;
        final Boolean _stpMask = (input & 1 << 7) > 0;
        return new PortStateV10(_blocked, _linkDown, _live, _stpBlock, _stpForward, _stpLearn, _stpListen, _stpMask);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static CapabilitiesV10 createCapabilities(long input) {
        final Boolean _oFPCFLOWSTATS = (input & 1 << 0) > 0;
        final Boolean _oFPCTABLESTATS = (input & 1 << 1) > 0;
        final Boolean _oFPCPORTSTATS = (input & 1 << 2) > 0;
        final Boolean _oFPCSTP = (input & 1 << 3) > 0;
        final Boolean _oFPCRESERVED = (input & 1 << 4) > 0;
        final Boolean _oFPCIPREASM = (input & 1 << 5) > 0;
        final Boolean _oFPCQUEUESTATS = (input & 1 << 6) > 0;
        final Boolean _oFPCARPMATCHIP = (input & 1 << 7) > 0;
        return new CapabilitiesV10(_oFPCARPMATCHIP, _oFPCFLOWSTATS, _oFPCIPREASM, _oFPCPORTSTATS, _oFPCQUEUESTATS,
                _oFPCRESERVED, _oFPCSTP, _oFPCTABLESTATS);
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static ActionTypeV10 createActionsV10(long input) {
        final Boolean _oFPATOUTPUT = (input & 1 << 0) > 0;
        final Boolean _oFPATSETVLANVID = (input & 1 << 1) > 0;
        final Boolean _oFPATSETVLANPCP = (input & 1 << 2) > 0;
        final Boolean _oFPATSTRIPVLAN = (input & 1 << 3) > 0;
        final Boolean _oFPATSETDLSRC = (input & 1 << 4) > 0;
        final Boolean _oFPATSETDLDST = (input & 1 << 5) > 0;
        final Boolean _oFPATSETNWSRC = (input & 1 << 6) > 0;
        final Boolean _oFPATSETNWDST = (input & 1 << 7) > 0;
        final Boolean _oFPATSETNWTOS = (input & 1 << 8) > 0;
        final Boolean _oFPATSETTPSRC = (input & 1 << 9) > 0;
        final Boolean _oFPATSETTPDST = (input & 1 << 10) > 0;
        final Boolean _oFPATENQUEUE = (input & 1 << 11) > 0;
        final Boolean _oFPATVENDOR = (input & 1 << 12) > 0;
        return new ActionTypeV10(_oFPATENQUEUE, _oFPATOUTPUT, _oFPATSETDLDST, _oFPATSETDLSRC, _oFPATSETNWDST,
                _oFPATSETNWSRC, _oFPATSETNWTOS, _oFPATSETTPDST, _oFPATSETTPSRC, _oFPATSETVLANPCP, _oFPATSETVLANVID,
                _oFPATSTRIPVLAN, _oFPATVENDOR);

    }
}
