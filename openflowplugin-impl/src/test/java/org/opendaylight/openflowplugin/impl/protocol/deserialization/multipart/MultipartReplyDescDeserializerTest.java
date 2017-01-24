/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyDescDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final int DESC_STR_LEN = 256;
    private static final int SERIAL_NUM_LEN = 32;
    private static final String MANUFACTURER = "Company";
    private static final String HARDWARE = "HW";
    private static final String SOFTWARE = "SW";
    private static final String SERIAL_NUMBER = "12345678";
    private static final String DESCRIPTION = "Description";

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(MANUFACTURER.getBytes());
        buffer.writeZero(DESC_STR_LEN - MANUFACTURER.length());
        buffer.writeBytes(HARDWARE.getBytes());
        buffer.writeZero(DESC_STR_LEN - HARDWARE.length());
        buffer.writeBytes(SOFTWARE.getBytes());
        buffer.writeZero(DESC_STR_LEN - SOFTWARE.length());
        buffer.writeBytes(SERIAL_NUMBER.getBytes());
        buffer.writeZero(SERIAL_NUM_LEN - SERIAL_NUMBER.length());
        buffer.writeBytes(DESCRIPTION.getBytes());
        buffer.writeZero(DESC_STR_LEN - DESCRIPTION.length());

        final MultipartReplyDesc reply = (MultipartReplyDesc) deserializeMultipart(buffer);
        assertEquals(MANUFACTURER, reply.getManufacturer().trim());
        assertEquals(HARDWARE, reply.getHardware().trim());
        assertEquals(SOFTWARE, reply.getSoftware().trim());
        assertEquals(SERIAL_NUMBER, reply.getSerialNumber().trim());
        assertEquals(DESCRIPTION, reply.getDescription().trim());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPDESC.getIntValue();
    }
}