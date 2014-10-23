/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.codec;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

import com.google.common.base.Preconditions;

/**
 * byte buffer tools
 */
public abstract class CodecUtil {

    /** padding size for 8 bytes alignment */
    private static final int PADDING_8 = 8;

    /**
     * @param currentLength
     * @return padding in order to reach total length as multiplier of 8 (bytes)
     */
    public static int computePadding8(int currentLength) {
        int padding = PADDING_8 - (currentLength % PADDING_8);
        return padding % PADDING_8;
    }

    /**
     * @param buffer
     * @param startIdx
     * @return 2B length of action
     */
    public static int getCofActionLength(ByteBuf buffer, int startIdx) {
        return buffer.getUnsignedShort(startIdx + EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
    }

    /**
     * @param buffer
     * @param startIdx start of element (e.g. action)
     * @param expectedLength
     */
    public static void finishElementReading(ByteBuf buffer, int startIdx, int expectedLength) {
        int readerIndex = buffer.readerIndex();
        int elementEndIndex = startIdx+expectedLength;
        Preconditions.checkArgument(elementEndIndex >= readerIndex, 
                "reader index already passed expected length of element: "
                        +elementEndIndex+" < "+readerIndex);
        buffer.skipBytes(elementEndIndex - readerIndex);
    }

    /**
     * @param source
     * @return byte[] with stripped trailing zeroes
     */
    public static byte[] stripTrailingZeroes(byte[] source) {
        int zeroOffset = 0;
        for (int idx = source.length - 1; idx >= 0; idx--) {
            zeroOffset = idx + 1;
            if (source[idx] != 0) break;
        }
        byte[] dest = new byte[zeroOffset];
        System.arraycopy(source, 0, dest, 0, zeroOffset);
        return dest; 
    }
    
}
