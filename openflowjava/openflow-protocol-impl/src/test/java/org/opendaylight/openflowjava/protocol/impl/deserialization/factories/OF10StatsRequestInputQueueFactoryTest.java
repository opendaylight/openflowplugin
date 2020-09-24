/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10StatsRequestInputQueueFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10StatsRequestInputQueueFactoryTest {
    private OFDeserializer<MultipartRequestInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 16, MultipartRequestInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 05 00 00 00 0f 00 00 00 00 00 10");
        MultipartRequestInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV10(deserializedMessage);

        Assert.assertEquals("Wrong type", 5, deserializedMessage.getType().getIntValue());
        Assert.assertEquals("Wrong flags", new MultipartRequestFlags(false), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong body", createMultipartRequestBody(), deserializedMessage.getMultipartRequestBody());
    }

    private static MultipartRequestBody createMultipartRequestBody() {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder queueBuilder = new MultipartRequestQueueBuilder();
        queueBuilder.setPortNo(Uint32.valueOf(15));
        queueBuilder.setQueueId(Uint32.valueOf(16));
        caseBuilder.setMultipartRequestQueue(queueBuilder.build());
        return caseBuilder.build();
    }
}
