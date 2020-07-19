/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.OpenflowUtils;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionTypeV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;

/**
 * Translates FeaturesReply messages (OpenFlow v1.0).
 *
 * @author michal.polkorab
 */
public class OF10FeaturesReplyMessageFactory implements OFDeserializer<GetFeaturesOutput> {

    private static final byte PADDING_IN_FEATURES_REPLY_HEADER = 3;

    @Override
    public GetFeaturesOutput deserialize(final ByteBuf rawMessage) {
        GetFeaturesOutputBuilder builder = new GetFeaturesOutputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_0)
                .setXid(readUint32(rawMessage))
                .setDatapathId(readUint64(rawMessage))
                .setBuffers(readUint32(rawMessage))
                .setTables(readUint8(rawMessage));
        rawMessage.skipBytes(PADDING_IN_FEATURES_REPLY_HEADER);
        builder.setCapabilitiesV10(createCapabilitiesV10(rawMessage.readUnsignedInt()));
        builder.setActionsV10(createActionsV10(rawMessage.readUnsignedInt()));
        List<PhyPort> ports = new ArrayList<>();
        while (rawMessage.readableBytes() > 0) {
            ports.add(deserializePort(rawMessage));
        }
        builder.setPhyPort(ports);
        return builder.build();
    }

    private static CapabilitiesV10 createCapabilitiesV10(final long input) {
        final Boolean flowStats = (input & 1 << 0) != 0;
        final Boolean tableStats = (input & 1 << 1) != 0;
        final Boolean portStats = (input & 1 << 2) != 0;
        final Boolean stp = (input & 1 << 3) != 0;
        final Boolean reserved = (input & 1 << 4) != 0;
        final Boolean ipReasm = (input & 1 << 5) != 0;
        final Boolean queueStats = (input & 1 << 6) != 0;
        final Boolean arpMatchIp = (input & 1 << 7) != 0;
        return new CapabilitiesV10(arpMatchIp, flowStats, ipReasm,
                portStats, queueStats, reserved, stp, tableStats);
    }

    private static ActionTypeV10 createActionsV10(final long input) {
        final Boolean output = (input & 1 << 0) != 0;
        final Boolean setVLANvid = (input & 1 << 1) != 0;
        final Boolean setVLANpcp = (input & 1 << 2) != 0;
        final Boolean stripVLAN = (input & 1 << 3) != 0;
        final Boolean setDLsrc = (input & 1 << 4) != 0;
        final Boolean setDLdst = (input & 1 << 5) != 0;
        final Boolean setNWsrc = (input & 1 << 6) != 0;
        final Boolean setNWdst = (input & 1 << 7) != 0;
        final Boolean setNWtos = (input & 1 << 8) != 0;
        final Boolean setTPsrc = (input & 1 << 9) != 0;
        final Boolean setTPdst = (input & 1 << 10) != 0;
        final Boolean enqueue = (input & 1 << 11) != 0;
        final Boolean vendor = (input & 1 << 12) != 0;
        return new ActionTypeV10(enqueue, output, setDLdst, setDLsrc,
                setNWdst, setNWsrc, setNWtos, setTPdst, setTPsrc,
                setVLANpcp, setVLANvid, stripVLAN, vendor);
    }

    private static PhyPort deserializePort(final ByteBuf rawMessage) {
        PhyPortBuilder builder = new PhyPortBuilder();
        builder.setPortNo(readUint16(rawMessage).toUint32());
        builder.setHwAddr(ByteBufUtils.readIetfMacAddress(rawMessage));
        builder.setName(ByteBufUtils.decodeNullTerminatedString(rawMessage, EncodeConstants.MAX_PORT_NAME_LENGTH));
        builder.setConfigV10(OpenflowUtils.createPortConfig(rawMessage.readUnsignedInt()));
        builder.setStateV10(OpenflowUtils.createPortState(rawMessage.readUnsignedInt()));
        builder.setCurrentFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setAdvertisedFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setSupportedFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        builder.setPeerFeaturesV10(OpenflowUtils.createPortFeatures(rawMessage.readUnsignedInt()));
        return builder.build();
    }
}
