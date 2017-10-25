/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;

/**
 * Serializes ofp_match (OpenFlow v1.0) structure
 * @author michal.polkorab
 */
public class OF10MatchSerializer implements OFSerializer<MatchV10> {

    private static final byte PADDING_IN_MATCH = 1;
    private static final byte PADDING_IN_MATCH_2 = 2;
    private static final byte NW_SRC_SHIFT = 8;
    private static final byte NW_DST_SHIFT = 14;

    /**
     * Serializes ofp_match (OpenFlow v1.0)
     * @param outBuffer output ByteBuf
     * @param match match to be serialized
     */
    @Override
    public void serialize(final MatchV10 match, final ByteBuf outBuffer) {
        outBuffer.writeInt(encodeWildcards(match.getWildcards(), match.getNwSrcMask(), match.getNwDstMask()));
        outBuffer.writeShort(match.getInPort());
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(match.getDlSrc()));
        outBuffer.writeBytes(IetfYangUtil.INSTANCE.bytesFor(match.getDlDst()));
        outBuffer.writeShort(match.getDlVlan());
        outBuffer.writeByte(match.getDlVlanPcp());
        outBuffer.writeZero(PADDING_IN_MATCH);
        outBuffer.writeShort(match.getDlType());
        outBuffer.writeByte(match.getNwTos());
        outBuffer.writeByte(match.getNwProto());
        outBuffer.writeZero(PADDING_IN_MATCH_2);
        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(match.getNwSrc()));
        outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(match.getNwDst()));
        outBuffer.writeShort(match.getTpSrc());
        outBuffer.writeShort(match.getTpDst());
    }

    private static int encodeWildcards(final FlowWildcardsV10 wildcards, final short srcMask, final short dstMask) {
        int bitmask = ByteBufUtils.fillBitMask(0,
                wildcards.isINPORT(),
                wildcards.isDLVLAN(),
                wildcards.isDLSRC(),
                wildcards.isDLDST(),
                wildcards.isDLTYPE(),
                wildcards.isNWPROTO(),
                wildcards.isTPSRC(),
                wildcards.isTPDST());
        bitmask |= ByteBufUtils.fillBitMask(20,
                wildcards.isDLVLANPCP(),
                wildcards.isNWTOS());
        bitmask |= ((32 - srcMask) << NW_SRC_SHIFT);
        bitmask |= ((32 - dstMask) << NW_DST_SHIFT);
        return bitmask;
    }

}
