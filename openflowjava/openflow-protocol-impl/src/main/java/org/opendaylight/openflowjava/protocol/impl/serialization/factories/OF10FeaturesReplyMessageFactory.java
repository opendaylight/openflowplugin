/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;

/**
 * Translates FeaturesReply messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10FeaturesReplyMessageFactory implements OFSerializer<GetFeaturesOutput> {

    private static final byte PADDING = 3;
    private static final byte MESSAGE_TYPE = 6;

    @Override
    public void serialize(final GetFeaturesOutput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeLong(message.getDatapathId().longValue());
        outBuffer.writeInt(message.getBuffers().intValue());
        outBuffer.writeByte(message.getTables().intValue());
        outBuffer.writeZero(PADDING);
        outBuffer.writeInt(createCapabilities(message.getCapabilitiesV10()));
        outBuffer.writeInt(createActionsV10(message.getActionsV10()));
        for (PhyPort port : message.getPhyPort()) {
            outBuffer.writeShort(port.getPortNo().intValue());
            outBuffer.writeBytes(IetfYangUtil.macAddressBytes(port.getHwAddr()));
            writeName(port.getName(), outBuffer);
            writePortConfig(port.getConfigV10(), outBuffer);
            writePortState(port.getStateV10(), outBuffer);
            writePortFeature(port.getCurrentFeaturesV10(), outBuffer);
            writePortFeature(port.getAdvertisedFeaturesV10(), outBuffer);
            writePortFeature(port.getSupportedFeaturesV10(), outBuffer);
            writePortFeature(port.getPeerFeaturesV10(), outBuffer);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static void writePortFeature(final PortFeaturesV10 feature, final ByteBuf outBuffer) {
        outBuffer.writeInt(ByteBufUtils.fillBitMask(
            feature.get_10mbHd(),
            feature.get_10mbFd(),
            feature.get_100mbHd(),
            feature.get_100mbFd(),
            feature.get_1gbHd(),
            feature.get_1gbFd(),
            feature.get_10gbFd(),
            feature.getCopper(),
            feature.getFiber(),
            feature.getAutoneg(),
            feature.getPause(),
            feature.getPauseAsym()));
    }

    private static void writePortState(final PortStateV10 state, final ByteBuf outBuffer) {
        outBuffer.writeInt(ByteBufUtils.fillBitMask(
            state.getLinkDown(),
            state.getBlocked(),
            state.getLive(),
            state.getStpListen(),
            state.getStpLearn(),
            state.getStpForward(),
            state.getStpBlock(),
            state.getStpMask()));
    }

    private static void writePortConfig(final PortConfigV10 config, final ByteBuf outBuffer) {
        outBuffer.writeInt(ByteBufUtils.fillBitMask(
            config.getPortDown(),
            config.getNoStp(),
            config.getNoRecv(),
            config.getNoRecvStp(),
            config.getNoFlood(),
            config.getNoFwd(),
            config.getNoPacketIn()));
    }

    private static int createCapabilities(final CapabilitiesV10 capabilities) {
        return ByteBufUtils.fillBitMask(
            capabilities.getOFPCFLOWSTATS(),
            capabilities.getOFPCTABLESTATS(),
            capabilities.getOFPCPORTSTATS(),
            capabilities.getOFPCSTP(),
            capabilities.getOFPCRESERVED(),
            capabilities.getOFPCIPREASM(),
            capabilities.getOFPCQUEUESTATS(),
            capabilities.getOFPCARPMATCHIP());
    }

    private static int createActionsV10(final ActionTypeV10 action) {
        return ByteBufUtils.fillBitMask(
            action.getOFPATOUTPUT(),
            action.getOFPATSETVLANVID(),
            action.getOFPATSETVLANPCP(),
            action.getOFPATSTRIPVLAN(),
            action.getOFPATSETDLSRC(),
            action.getOFPATSETDLDST(),
            action.getOFPATSETNWSRC(),
            action.getOFPATSETNWDST(),
            action.getOFPATSETNWTOS(),
            action.getOFPATSETTPSRC(),
            action.getOFPATSETTPDST(),
            action.getOFPATENQUEUE(),
            action.getOFPATVENDOR());
    }

    private static void writeName(final String name, final ByteBuf outBuffer) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        if (nameBytes.length < 16) {
            byte[] nameBytesPadding = new byte[16];
            int index = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[index] = b;
                index++;
            }
            for (; index < 16; index++) {
                nameBytesPadding[index] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }
}
