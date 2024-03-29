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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.desc._case.MultipartRequestDescBuilder;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Unit tests for MultipartRequestDescInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class MultipartRequestDescInputMessageFactoryTest {

    private OFDeserializer<MultipartRequestInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, 18, MultipartRequestInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 01 00 00 00 00");
        MultipartRequestInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        Assert.assertEquals("Wrong type", MultipartType.forValue(0), deserializedMessage.getType());
        Assert.assertEquals("Wrong flags", new MultipartRequestFlags(true), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong aggregate", createRequestDesc(), deserializedMessage.getMultipartRequestBody());
    }

    private static MultipartRequestDescCase createRequestDesc() {
        MultipartRequestDescCaseBuilder caseBuilder = new MultipartRequestDescCaseBuilder();
        MultipartRequestDescBuilder builder = new MultipartRequestDescBuilder();
        builder.setEmpty(Empty.value());
        caseBuilder.setMultipartRequestDesc(builder.build());
        return caseBuilder.build();
    }

}
