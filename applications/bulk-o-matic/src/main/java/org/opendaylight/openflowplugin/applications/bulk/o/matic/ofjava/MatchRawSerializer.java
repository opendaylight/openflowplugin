/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IetfInetUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes ofp_match (OpenFlow v1.3)
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class MatchRawSerializer implements OFSerializer<Match> {
    private static final Logger LOG = LoggerFactory.getLogger(MatchRawSerializer.class);
    private static final byte OXM_MATCH_TYPE_CODE = 1;

    // Pre-calculated masks for the 33 possible values. Do not give them out, but clone() them as they may
    // end up being leaked and vulnerable.
    private static final byte[][] IPV4_MASKS;

    static {
        final byte[][] tmp = new byte[33][];
        for (int i = 0; i <= 32; ++i) {
            final int mask = 0xffffffff << (32 - i);
            tmp[i] = new byte[]{(byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8), (byte) mask};
        }

        IPV4_MASKS = tmp;
    }

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        if (match == null) {
            LOG.debug("Match is null");
            return;
        }

        int matchStartIndex = outBuffer.writerIndex();

        // With OpenflowPlugin models, we cannot check difference between OXM and Standard match type
        // so all matches will be OXM
        outBuffer.writeShort(OXM_MATCH_TYPE_CODE);
        int matchLengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // Serialize match entries
        if (match.getEthernetMatch() != null) {
            if (match.getEthernetMatch().getEthernetType() != null) {
                writeOxmFieldAndLength(outBuffer, OxmMatchConstants.ETH_TYPE, false, EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
                outBuffer.writeShort(match.getEthernetMatch().getEthernetType().getType().getValue().shortValue());
            }
        }

        if (match.getLayer3Match() != null) {
            if (Ipv4Match.class.equals(match.getLayer3Match().getImplementedInterface())) {
                final Ipv4Match ipv4Match = Ipv4Match.class.cast(match.getLayer3Match());

                if (ipv4Match.getIpv4Source() != null) {
                    final String addressWithMask = ipv4Match.getIpv4Source().getValue();
                    final Iterator<String> addressParts = Splitter.on('/').split(addressWithMask).iterator();
                    final Ipv4Address address = new Ipv4Address(addressParts.next());
                    final byte[] mask = extractIpv4Mask(addressParts);
                    final boolean hasMask = mask != null;

                    writeOxmFieldAndLength(outBuffer, OxmMatchConstants.IPV4_SRC, hasMask, EncodeConstants.SIZE_OF_INT_IN_BYTES);
                    outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(address));

                    if (hasMask) {
                       outBuffer.writeBytes(mask);
                    }
                }

                if (ipv4Match.getIpv4Destination() != null) {
                    final String addressWithMask = ipv4Match.getIpv4Destination().getValue();
                    final Iterator<String> addressParts = Splitter.on('/').split(addressWithMask).iterator();
                    final Ipv4Address address = new Ipv4Address(addressParts.next());
                    final byte[] mask = extractIpv4Mask(addressParts);
                    final boolean hasMask = mask != null;

                    writeOxmFieldAndLength(outBuffer, OxmMatchConstants.IPV4_DST, hasMask, EncodeConstants.SIZE_OF_INT_IN_BYTES);
                    outBuffer.writeBytes(IetfInetUtil.INSTANCE.ipv4AddressBytes(address));

                    if (hasMask) {
                       outBuffer.writeBytes(mask);
                    }
                }
            }
        }

        // Length of ofp_match (excluding padding)
        int matchLength = outBuffer.writerIndex() - matchStartIndex;
        outBuffer.setShort(matchLengthIndex, matchLength);
        int paddingRemainder = matchLength % EncodeConstants.PADDING;

        if (paddingRemainder != 0) {
            outBuffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }
    }

    private static byte[] extractIpv4Mask(final Iterator<String> addressParts) {
        final int prefix;
        if (addressParts.hasNext()) {
            int potentionalPrefix = Integer.parseInt(addressParts.next());
            prefix = potentionalPrefix < 32 ? potentionalPrefix : 0;
        } else {
            prefix = 0;
        }

        if (prefix != 0) {
            // clone() is necessary to protect our constants
            return IPV4_MASKS[prefix].clone();
        }

        return null;
    }


    private static void writeOxmFieldAndLength(ByteBuf out, int fieldValue, boolean hasMask, int lengthArg) {
        int fieldAndMask = fieldValue << 1;
        int length = lengthArg;

        if (hasMask) {
            fieldAndMask |= 1;
            length *= 2;
        }


        out.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        out.writeByte(fieldAndMask);
        out.writeByte(length);
    }
}
