/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for MultipartRequestAggregateInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class MultipartRequestAggregateInputMessageFactoryTest {

    private OFDeserializer<MultipartRequestInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 18, MultipartRequestInput.class));

    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 02 00 01 00 00 00 00 08 00 "
                + "00 00 00 00 00 55 00 00 00 5f 00 00 00 00 00 01 01 01 01 01 " + "01 01 00 01 01 01 01 01 01 01");
        MultipartRequestInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        Assert.assertEquals("Wrong type", MultipartType.forValue(2), deserializedMessage.getType());
        Assert.assertEquals("Wrong flags", new MultipartRequestFlags(true), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong aggregate", createRequestAggregate(), deserializedMessage.getMultipartRequestBody());
    }

    private static MultipartRequestAggregateCase createRequestAggregate() {
        final MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder builder = new MultipartRequestAggregateBuilder();
        builder.setTableId((short) 8);
        builder.setOutPort(Uint32.valueOf(85));
        builder.setOutGroup(Uint32.valueOf(95));
        byte[] cookie = new byte[] { 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 };
        builder.setCookie(new BigInteger(1, cookie));
        byte[] cookieMask = new byte[] { 0x00, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01 };
        builder.setCookieMask(new BigInteger(1, cookieMask));
        caseBuilder.setMultipartRequestAggregate(builder.build());
        return caseBuilder.build();
    }

}
