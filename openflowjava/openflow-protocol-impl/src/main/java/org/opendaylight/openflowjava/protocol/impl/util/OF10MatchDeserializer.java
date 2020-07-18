/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Deserializes ofp_match (OpenFlow v1.0) structure.
 *
 * @author michal.polkorab
 */
public class OF10MatchDeserializer implements OFDeserializer<MatchV10> {

    private static final byte PADDING_IN_MATCH = 1;
    private static final byte PADDING_IN_MATCH_2 = 2;
    private static final byte NW_SRC_BITS = 6;
    private static final byte NW_SRC_SHIFT = 8;
    private static final int NW_SRC_MASK = (1 << NW_SRC_BITS) - 1 << NW_SRC_SHIFT;
    private static final byte NW_DST_BITS = 6;
    private static final byte NW_DST_SHIFT = 14;
    private static final int NW_DST_MASK = (1 << NW_DST_BITS) - 1 << NW_DST_SHIFT;

    @Override
    public MatchV10 deserialize(final ByteBuf input) {
        MatchV10Builder builder = new MatchV10Builder();
        long wildcards = input.readUnsignedInt();
        builder.setWildcards(createWildcards(wildcards));
        builder.setNwSrcMask(decodeNwSrcMask(wildcards));
        builder.setNwDstMask(decodeNwDstMask(wildcards));
        builder.setInPort(readUint16(input));
        builder.setDlSrc(ByteBufUtils.readIetfMacAddress(input));
        builder.setDlDst(ByteBufUtils.readIetfMacAddress(input));

        builder.setDlVlan(readUint16(input));
        builder.setDlVlanPcp(readUint8(input));
        input.skipBytes(PADDING_IN_MATCH);
        builder.setDlType(readUint16(input));
        builder.setNwTos(readUint8(input));
        builder.setNwProto(readUint8(input));
        input.skipBytes(PADDING_IN_MATCH_2);
        builder.setNwSrc(ByteBufUtils.readIetfIpv4Address(input));
        builder.setNwDst(ByteBufUtils.readIetfIpv4Address(input));
        builder.setTpSrc(readUint16(input));
        builder.setTpDst(readUint16(input));
        return builder.build();
    }

    /**
     * Decodes FlowWildcards.
     *
     * @param input input ByteBuf
     * @return decoded FlowWildcardsV10
     */
    public static FlowWildcardsV10 createWildcards(final long input) {
        boolean inPort = (input & 1 << 0) != 0;
        boolean dlVLAN = (input & 1 << 1) != 0;
        boolean dlSrc = (input & 1 << 2) != 0;
        boolean dlDst = (input & 1 << 3) != 0;
        boolean dlType = (input & 1 << 4) != 0;
        boolean nwProto = (input & 1 << 5) != 0;
        boolean tpSrc = (input & 1 << 6) != 0;
        boolean tpDst = (input & 1 << 7) != 0;
        boolean dlVLANpcp = (input & 1 << 20) != 0;
        boolean nwTos = (input & 1 << 21) != 0;
        return new FlowWildcardsV10(dlDst, dlSrc, dlType, dlVLAN,
                dlVLANpcp, inPort, nwProto, nwTos, tpDst, tpSrc);
    }

    /**
     * Decodes NwSrcMask from FlowWildcards (represented as uint32).
     *
     * @param input binary FlowWildcards
     * @return decoded NwSrcMask
     */
    public static @NonNull Uint8 decodeNwSrcMask(final long input) {
        return Uint8.valueOf(Math.max(32 - ((input & NW_SRC_MASK) >> NW_SRC_SHIFT), 0));
    }

    /**
     * Decodes NwDstMask from FlowWildcards (represented as uint32).
     *
     * @param input binary FlowWildcards
     * @return decoded NwDstMask
     */
    public static @NonNull Uint8 decodeNwDstMask(final long input) {
        return Uint8.valueOf(Math.max(32 - ((input & NW_DST_MASK) >> NW_DST_SHIFT), 0));
    }
}
