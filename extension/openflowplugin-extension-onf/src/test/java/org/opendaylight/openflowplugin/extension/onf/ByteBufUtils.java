/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.List;
import org.junit.Assert;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * Abstract class for common ByteBuf util methods.
 */
public abstract class ByteBufUtils {

    private static final Splitter HEXSTRING_SPLITTER =  Splitter.onPattern("\\s+").omitEmptyStrings();
    private static final byte[] XID = new byte[] { 0x01, 0x02, 0x03, 0x04 };
    public static final Long DEFAULT_XID = 0x01020304L;

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
        final Short version = EncodeConstants.OF13_VERSION_ID;
        Assert.assertEquals("Wrong version", version, ofHeader.getVersion());
        Assert.assertEquals("Wrong Xid", DEFAULT_XID, ofHeader.getXid());
    }

    private static byte[] hexStringToBytes(final String hexSrc) {
        List<String> byteChips = Lists.newArrayList(HEXSTRING_SPLITTER.split(hexSrc));
        byte[] result = new byte[byteChips.size()];
        int i = 0;
        for (String chip : byteChips) {
            result[i] = (byte) Short.parseShort(chip, 16);
            i++;
        }
        return result;
    }

}
