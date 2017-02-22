/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyDescDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final int DESC_STR_LEN = 256;
    private static final int SERIAL_NUM_LEN = 32;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        byte[] mfrDescBytes = new byte[DESC_STR_LEN];
        message.readBytes(mfrDescBytes);

        byte[] hwDescBytes = new byte[DESC_STR_LEN];
        message.readBytes(hwDescBytes);

        byte[] swDescBytes = new byte[DESC_STR_LEN];
        message.readBytes(swDescBytes);

        byte[] serialNumBytes = new byte[SERIAL_NUM_LEN];
        message.readBytes(serialNumBytes);

        byte[] dpDescBytes = new byte[DESC_STR_LEN];
        message.readBytes(dpDescBytes);

        return new MultipartReplyDescBuilder()
                .setManufacturer(new String(mfrDescBytes).trim())
                .setHardware(new String(hwDescBytes).trim())
                .setSoftware(new String(swDescBytes).trim())
                .setSerialNumber(new String(serialNumBytes).trim())
                .setDescription(new String(dpDescBytes).trim())
                .build();
    }

}
