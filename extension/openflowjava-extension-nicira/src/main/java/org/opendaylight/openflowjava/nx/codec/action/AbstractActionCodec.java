/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.nx.codec.match.NxmHeader;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Base class for an action codec.
 *
 * @author msunal
 */
public abstract class AbstractActionCodec implements OFSerializer<Action>, OFDeserializer<Action> {

    protected static final void serializeHeader(final int msgLength, final int subtype, final ByteBuf outBuffer) {
        outBuffer.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        writeMsgLengthVendorIdSubtypeToBuffer(msgLength, subtype, outBuffer);
    }

    private static void writeMsgLengthVendorIdSubtypeToBuffer(final int msgLength, final int subtype,
            final ByteBuf outBuffer) {
        outBuffer.writeShort(msgLength);
        outBuffer.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        outBuffer.writeShort(subtype);
    }

    protected static final ActionBuilder deserializeHeader(final ByteBuf message) {
        // size of experimenter type
        message.skipBytes(Short.BYTES);
        // size of length
        message.skipBytes(Short.BYTES);
        // vendor id
        message.skipBytes(Integer.BYTES);
        // subtype
        message.skipBytes(Short.BYTES);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(getExperimenterId());
        return actionBuilder;
    }

    protected static final ExperimenterId getExperimenterId() {
        return new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
    }

    private static int getPaddingRemainder(int nonPaddedSize) {
        int paddingRemainder = EncodeConstants.PADDING - (nonPaddedSize % EncodeConstants.PADDING);
        return paddingRemainder % EncodeConstants.PADDING;
    }

    protected static final void skipPadding(ByteBuf message, int startIndex) {
        int nonPaddedSize = message.readerIndex() - startIndex;
        message.skipBytes(getPaddingRemainder(nonPaddedSize));
    }

    protected static final void writePaddingAndSetLength(ByteBuf outBuffer, int startIndex) {
        int nonPaddedSize = outBuffer.writerIndex() - startIndex;
        outBuffer.writeZero(getPaddingRemainder(nonPaddedSize));
        outBuffer.setShort(startIndex + Short.BYTES, outBuffer.writerIndex() - startIndex);
    }

    protected static void writeNxmHeader(final Uint64 value, final ByteBuf outBuffer) {
        if (NxmHeader.isExperimenter(value)) {
            outBuffer.writeLong(value.longValue());
        } else {
            outBuffer.writeInt((int) value.longValue());
        }
    }

    protected static BigInteger readNxmHeader(final ByteBuf message) {
        int value = message.getUnsignedShort(message.readerIndex());
        byte[] bytes = new byte[value == EncodeConstants.EXPERIMENTER_VALUE ? Long.BYTES : Integer.BYTES];
        message.readBytes(bytes);
        return new BigInteger(1, bytes);
    }

}
