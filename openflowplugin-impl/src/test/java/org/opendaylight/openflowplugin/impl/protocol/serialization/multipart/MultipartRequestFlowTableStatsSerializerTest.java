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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.request.multipart.request.body.MultipartRequestFlowTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.request.multipart.request.body.MultipartRequestFlowTableStatsBuilder;

public class MultipartRequestFlowTableStatsSerializerTest extends AbstractSerializerTest {
    private static final MultipartRequestFlowTableStats BODY = new MultipartRequestFlowTableStatsBuilder()
            .build();

    private MultipartRequestFlowTableStatsSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID,
                MultipartRequestFlowTableStats.class)) ;
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(BODY, out);
        assertEquals(out.readableBytes(), 0);
    }

}