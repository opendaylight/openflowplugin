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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Helper class for buffers.
 *
 * @author michal.polkorab
 */
public abstract class BufferHelper {

    public static final Uint32 DEFAULT_XID = Uint32.valueOf(0x01020304L);
    private static final byte[] XID = new byte[] { 0x01, 0x02, 0x03, 0x04 };

    /**
     * Build a ByteBuffer.
     *
     * @param payload the payload data
     * @return ByteBuf filled with OpenFlow protocol message without first 4
     *         bytes
     */
    public static ByteBuf buildBuffer(final byte[] payload) {
        ByteBuf bb = UnpooledByteBufAllocator.DEFAULT.buffer();
        bb.writeBytes(XID);
        bb.writeBytes(payload);
        return bb;
    }

    /**
     * Build a ByteBuffer.
     *
     * @param payload String in hex format
     * @return ByteBuf filled with OpenFlow protocol message without first 4
     *         bytes
     */
    public static ByteBuf buildBuffer(final String payload) {
        return buildBuffer(ByteBufUtils.hexStringToBytes(payload));
    }

    /**
     * Build a ByteBuffer.
     *
     * @return ByteBuf filled with OpenFlow protocol header message without first 4
     *         bytes
     */
    public static ByteBuf buildBuffer() {
        ByteBuf bb = UnpooledByteBufAllocator.DEFAULT.buffer();
        bb.writeBytes(XID);
        bb.writeBytes(new byte[0]);
        return bb;
    }

    /**
     * Use version 1.3 for encoded message.
     *
     * @param input ByteBuf to be checked for correct OpenFlow Protocol header
     * @param msgType type of received message
     * @param length expected length of message in header
     */
    public static void checkHeaderV13(final ByteBuf input, final byte msgType, final int length) {
        checkHeader(input, msgType, length, (short) EncodeConstants.OF13_VERSION_ID);
    }

    /**
     * Checks a 1.3 header.
     *
     * @param ofHeader OpenFlow protocol header
     */
    public static void checkHeaderV13(final OfHeader ofHeader) {
        checkHeader(ofHeader, EncodeConstants.OF_VERSION_1_3);
    }

    /**
     * Use version 1.0 for encoded message.
     *
     * @param input ByteBuf to be checked for correct OpenFlow Protocol header
     * @param msgType type of received message
     * @param length expected length of message in header
     */
    public static void checkHeaderV10(final ByteBuf input, final byte msgType, final int length) {
        checkHeader(input, msgType, length, (short) EncodeConstants.OF10_VERSION_ID);
    }

    /**
     * Checks a 1.0 header.
     *
     * @param ofHeader OpenFlow protocol header
     */
    public static void checkHeaderV10(final OfHeader ofHeader) {
        checkHeader(ofHeader, EncodeConstants.OF_VERSION_1_0);
    }

    private static void checkHeader(final ByteBuf input, final byte msgType, final int length, final Short version) {
        Assert.assertEquals("Wrong version", version, Short.valueOf(input.readByte()));
        Assert.assertEquals("Wrong type", msgType, input.readByte());
        Assert.assertEquals("Wrong length", length, input.readUnsignedShort());
        Assert.assertEquals("Wrong Xid", DEFAULT_XID, Uint32.valueOf(input.readUnsignedInt()));
    }

    /**
     * Check version and xid of OFP header.
     *
     * @param ofHeader OpenFlow protocol header
     * @param version OpenFlow protocol version
     */
    public static void checkHeader(final OfHeader ofHeader, final Uint8 version) {
        Assert.assertEquals("Wrong version", version, ofHeader.getVersion());
        Assert.assertEquals("Wrong Xid", DEFAULT_XID, ofHeader.getXid());
    }

    /**
     * Sets up a header.
     *
     * @param builder builder
     * @param version wire protocol number used
     */
    public static void setupHeader(final Object builder, final int version) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method = builder.getClass().getMethod("setVersion", Short.class);
        method.invoke(builder, (short) version);
        Method m2 = builder.getClass().getMethod("setXid", Uint32.class);
        m2.invoke(builder, BufferHelper.DEFAULT_XID);
    }

    /**
     * Decode message.
     *
     * @param decoder decoder instance
     * @param bb data input buffer
     * @return message decoded pojo
     */
    public static <E extends DataContainer> E deserialize(final OFDeserializer<E> decoder, final ByteBuf bb) {
        return decoder.deserialize(bb);
    }

}
