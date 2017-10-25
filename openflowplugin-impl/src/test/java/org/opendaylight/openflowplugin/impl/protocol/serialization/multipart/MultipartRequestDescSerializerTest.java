/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.request.multipart.request.body.MultipartRequestDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.request.multipart.request.body.MultipartRequestDescBuilder;

public class MultipartRequestDescSerializerTest extends AbstractSerializerTest {
    private static final MultipartRequestDesc BODY = new MultipartRequestDescBuilder()
            .build();

    private MultipartRequestDescSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID,
                MultipartRequestDesc.class)) ;
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(BODY, out);
        assertEquals(out.readableBytes(), 0);
    }

}