/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MultipartReplyMessageDeserializer implements OFDeserializer<MultipartReply>, DeserializerRegistryInjector {
    private static final byte PADDING_IN_MULTIPART_REPLY_HEADER = 4;

    private DeserializerRegistry registry;

    @Override
    public MultipartReply deserialize(final ByteBuf message) {
        final Uint32 xid = readUint32(message);
        final int type = message.readUnsignedShort();
        final boolean reqMore = (message.readUnsignedShort() & 0x01) != 0;
        message.skipBytes(PADDING_IN_MULTIPART_REPLY_HEADER);

        final OFDeserializer<MultipartReplyBody> deserializer = requireNonNull(registry)
            .getDeserializer(new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, type, MultipartReplyBody.class));

        return new MultipartReplyBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setXid(xid)
            .setRequestMore(reqMore)
            .setMultipartReplyBody(deserializer.deserialize(message))
            .build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
