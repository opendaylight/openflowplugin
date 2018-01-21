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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;

/**
 * Unit tests for OF10StatsRequestInputAggregateFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10StatsRequestInputAggregateFactoryTest {
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
        ByteBuf bb = BufferHelper.buildBuffer("00 02 00 00 00 00 00 00 00 33 00 01 02 "
                + "03 04 05 05 04 03 02 01 00 00 34 35 00 00 36 37 38 00 00 0a 00 00 01 "
                + "0a 00 00 02 00 39 00 3a 2a 00 19 fd");
        MultipartRequestInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV10(deserializedMessage);

        Assert.assertEquals("Wrong type", 2, deserializedMessage.getType().getIntValue());
        Assert.assertEquals("Wrong flags", new MultipartRequestFlags(false), deserializedMessage.getFlags());
        Assert.assertEquals("Wrong body", createMultipartRequestBody(), deserializedMessage.getMultipartRequestBody());
    }

    private static MultipartRequestBody createMultipartRequestBody() {
        final MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        final MultipartRequestAggregateBuilder aggregateBuilder = new MultipartRequestAggregateBuilder();
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(
                new FlowWildcardsV10(false, false, false, false, false, false, false, false, false, false));
        matchBuilder.setNwSrcMask((short) 32);
        matchBuilder.setNwDstMask((short) 32);
        matchBuilder.setInPort(51);
        matchBuilder.setDlSrc(new MacAddress("00:01:02:03:04:05"));
        matchBuilder.setDlDst(new MacAddress("05:04:03:02:01:00"));
        matchBuilder.setDlVlan(52);
        matchBuilder.setDlVlanPcp((short) 53);
        matchBuilder.setDlType(54);
        matchBuilder.setNwTos((short) 55);
        matchBuilder.setNwProto((short) 56);
        matchBuilder.setNwSrc(new Ipv4Address("10.0.0.1"));
        matchBuilder.setNwDst(new Ipv4Address("10.0.0.2"));
        matchBuilder.setTpSrc(57);
        matchBuilder.setTpDst(58);
        aggregateBuilder.setMatchV10(matchBuilder.build());
        aggregateBuilder.setTableId((short) 42);
        aggregateBuilder.setOutPort(6653L);
        caseBuilder.setMultipartRequestAggregate(aggregateBuilder.build());
        return caseBuilder.build();
    }
}
