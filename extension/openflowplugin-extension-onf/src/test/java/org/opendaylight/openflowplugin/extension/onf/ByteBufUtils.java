/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import com.google.common.base.Splitter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.List;
import org.junit.Assert;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Abstract class for common ByteBuf util methods.
 */
public abstract class ByteBufUtils {

    private static final Splitter HEXSTRING_SPLITTER =  Splitter.onPattern("\\s+").omitEmptyStrings();
    private static final byte[] XID = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    private static final Uint32 DEFAULT_XID = Uint32.valueOf(0x01020304L);

    public static ByteBuf hexStringToByteBuf(final String hexSrc) {
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(hexStringToBytes(hexSrc));
        return out;
    }

    public static ByteBuf buildBuf(final String hexSrc) {
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(XID);
        out.writeBytes(hexStringToBytes(hexSrc));
        return out;
    }

    public static void checkHeaderV13(OfHeader ofHeader) {
        Assert.assertEquals("Wrong version", Uint8.valueOf(EncodeConstants.OF13_VERSION_ID), ofHeader.getVersion());
        Assert.assertEquals("Wrong Xid", DEFAULT_XID, ofHeader.getXid());
    }

    private static byte[] hexStringToBytes(final String hexSrc) {
        List<String> byteChips = HEXSTRING_SPLITTER.splitToList(hexSrc);
        byte[] result = new byte[byteChips.size()];
        int index = 0;
        for (String chip : byteChips) {
            result[index] = (byte) Short.parseShort(chip, 16);
            index++;
        }
        return result;
    }

}
