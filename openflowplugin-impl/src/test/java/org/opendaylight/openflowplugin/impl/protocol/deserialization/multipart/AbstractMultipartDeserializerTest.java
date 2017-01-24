/**
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public abstract class AbstractMultipartDeserializerTest  extends AbstractDeserializerTest {

    private OFDeserializer<MultipartReplyBody> deserializer;

    @Override
    protected void init() {
        deserializer = getRegistry().getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, getType(), MultipartReplyBody.class));
    }

    protected MultipartReplyBody deserializeMultipart(ByteBuf message) {
        return deserializer.deserialize(message);
    }

    protected abstract int getType();
}
