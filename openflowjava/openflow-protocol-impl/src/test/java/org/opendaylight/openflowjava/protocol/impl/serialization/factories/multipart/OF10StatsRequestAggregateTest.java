/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories.multipart;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.OF10StatsRequestInputFactory;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10StatsRequestAggregate.
 *
 * @author michal.polkorab
 */
public class OF10StatsRequestAggregateTest {

    private SerializerRegistry registry;
    private OFSerializer<MultipartRequestInput> statsFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        statsFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, MultipartRequestInput.class));
    }

    /**
     * Tests {@link OF10StatsRequestInputFactory} for correct serialization.
     */
    @Test
    public void test() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPAGGREGATE);
        builder.setFlags(new MultipartRequestFlags(false));
        final MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        final MultipartRequestAggregateBuilder aggBuilder = new MultipartRequestAggregateBuilder();
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true,
                true, true, true, true));
        matchBuilder.setNwSrcMask((short) 8);
        matchBuilder.setNwDstMask((short) 16);
        matchBuilder.setInPort(Uint16.valueOf(51));
        matchBuilder.setDlSrc(new MacAddress("00:01:02:03:04:05"));
        matchBuilder.setDlDst(new MacAddress("05:04:03:02:01:00"));
        matchBuilder.setDlVlan(Uint16.valueOf(52));
        matchBuilder.setDlVlanPcp((short) 53);
        matchBuilder.setDlType(Uint16.valueOf(54));
        matchBuilder.setNwTos((short) 55);
        matchBuilder.setNwProto((short) 56);
        matchBuilder.setNwSrc(new Ipv4Address("10.0.0.1"));
        matchBuilder.setNwDst(new Ipv4Address("10.0.0.2"));
        matchBuilder.setTpSrc(Uint16.valueOf(57));
        matchBuilder.setTpDst(Uint16.valueOf(58));
        aggBuilder.setMatchV10(matchBuilder.build());
        aggBuilder.setTableId((short) 5);
        aggBuilder.setOutPort(Uint32.valueOf(42));
        caseBuilder.setMultipartRequestAggregate(aggBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        statsFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 56);
        Assert.assertEquals("Wrong type", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        out.skipBytes(40); // skip match check
        Assert.assertEquals("Wrong table-id", 5, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong out port", 42, out.readUnsignedShort());
    }
}
