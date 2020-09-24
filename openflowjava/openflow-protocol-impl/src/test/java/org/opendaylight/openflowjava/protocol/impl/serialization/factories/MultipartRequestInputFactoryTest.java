/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

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
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.desc._case.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for MultipartRequestInputFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class MultipartRequestInputFactoryTest {
    static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_01 = 3;
    static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_02 = 4;
    static final byte PADDING_IN_MULTIPART_REQUEST_METER_CONFIG_BODY = 4;
    static final byte PADDING_IN_MULTIPART_REQUEST_AGGREGATE_BODY_01 = 3;
    static final byte PADDING_IN_MULTIPART_REQUEST_AGGREGATE_BODY_02 = 4;
    static final byte PADDING_IN_MULTIPART_REQUEST_PORTSTATS_BODY = 4;
    static final byte PADDING_IN_MULTIPART_REQUEST_GROUP_BODY = 4;
    static final byte PADDING_IN_MULTIPART_REQUEST_METER_BODY = 4;

    /** padding in MultipartRequest message. */
    public static final byte PADDING_IN_MULTIPART_REQUEST_MESSAGE = 4;
    private SerializerRegistry registry;
    private OFSerializer<MultipartRequestInput> multipartFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        multipartFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, MultipartRequestInput.class));
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestFlowInputFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(1));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestFlow());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 48);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong flow", message.getMultipartRequestBody(), decodeRequestFlow(out));
    }

    private static MultipartRequestFlowCase createRequestFlow() {
        final MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder builder = new MultipartRequestFlowBuilder();
        builder.setTableId(Uint8.valueOf(8));
        builder.setOutPort(Uint32.valueOf(85));
        builder.setOutGroup(Uint32.valueOf(95));
        builder.setCookie(Uint64.valueOf("0001010101010101", 16));
        builder.setCookieMask(Uint64.valueOf("0001010101010101", 16));
        caseBuilder.setMultipartRequestFlow(builder.build());
        //TODO match field
        return caseBuilder.build();
    }

    private static MultipartRequestFlowCase decodeRequestFlow(ByteBuf output) {
        final MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder builder = new MultipartRequestFlowBuilder();
        builder.setTableId(Uint8.fromByteBits(output.readByte()));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_01);
        builder.setOutPort(Uint32.fromIntBits(output.readInt()));
        builder.setOutGroup(Uint32.fromIntBits(output.readInt()));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_02);
        builder.setCookie(Uint64.fromLongBits(output.readLong()));
        builder.setCookieMask(Uint64.fromLongBits(output.readLong()));
        caseBuilder.setMultipartRequestFlow(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestInputAggregateBodyFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(2));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestAggregate());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 48);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong aggregate", message.getMultipartRequestBody(), decodeRequestAggregate(out));
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static MultipartRequestFlags decodeMultipartRequestFlags(short input) {
        final Boolean _oFPMPFREQMORE = (input & 1 << 0) > 0;
        return new MultipartRequestFlags(_oFPMPFREQMORE);
    }


    private static MultipartRequestAggregateCase createRequestAggregate() {
        final MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder builder = new MultipartRequestAggregateBuilder();
        builder.setTableId(Uint8.valueOf(8));
        builder.setOutPort(Uint32.valueOf(85));
        builder.setOutGroup(Uint32.valueOf(95));
        builder.setCookie(Uint64.valueOf("0001010101010101", 16));
        builder.setCookieMask(Uint64.valueOf("0001010101010101", 16));
        caseBuilder.setMultipartRequestAggregate(builder.build());
        // TODO match field
        return caseBuilder.build();
    }

    private static MultipartRequestAggregateCase decodeRequestAggregate(ByteBuf output) {
        final  MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder builder = new MultipartRequestAggregateBuilder();
        builder.setTableId(Uint8.fromByteBits(output.readByte()));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_AGGREGATE_BODY_01);
        builder.setOutPort(Uint32.fromIntBits(output.readInt()));
        builder.setOutGroup(Uint32.fromIntBits(output.readInt()));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_AGGREGATE_BODY_02);
        builder.setCookie(Uint64.fromLongBits(output.readLong()));
        builder.setCookieMask(Uint64.fromLongBits(output.readLong()));
        caseBuilder.setMultipartRequestAggregate(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestInputTableFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(3));
        builder.setFlags(new MultipartRequestFlags(true));
        //multipart request for registry does not have body
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 16);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestPortStatsMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(4));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestPortStats());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 24);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong portStatsBody", message.getMultipartRequestBody(), decodeRequestPortStats(out));
    }

    private static MultipartRequestPortStatsCase createRequestPortStats() {
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder builder = new MultipartRequestPortStatsBuilder();
        builder.setPortNo(Uint32.valueOf(2251));
        caseBuilder.setMultipartRequestPortStats(builder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestPortStatsCase decodeRequestPortStats(ByteBuf output) {
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder builder = new MultipartRequestPortStatsBuilder();
        builder.setPortNo(Uint32.fromIntBits(output.readInt()));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_PORTSTATS_BODY);
        caseBuilder.setMultipartRequestPortStats(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestQueueMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(5));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestQueue());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 24);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong queueBody", message.getMultipartRequestBody(), decodeRequestQueue(out));
    }

    private static MultipartRequestQueueCase createRequestQueue() {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder builder = new MultipartRequestQueueBuilder();
        builder.setPortNo(Uint32.valueOf(2256));
        builder.setQueueId(Uint32.valueOf(2211));
        caseBuilder.setMultipartRequestQueue(builder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestQueueCase decodeRequestQueue(ByteBuf output) {
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder builder = new MultipartRequestQueueBuilder();
        builder.setPortNo(Uint32.fromIntBits(output.readInt()));
        builder.setQueueId(Uint32.fromIntBits(output.readInt()));
        caseBuilder.setMultipartRequestQueue(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestGroupMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(6));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestGroup());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 24);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong groupBody", message.getMultipartRequestBody(), decodeRequestGroup(out));
    }

    private static MultipartRequestGroupCase createRequestGroup() {
        MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        MultipartRequestGroupBuilder builder = new MultipartRequestGroupBuilder();
        builder.setGroupId(new GroupId(Uint32.valueOf(2258)));
        caseBuilder.setMultipartRequestGroup(builder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestGroupCase decodeRequestGroup(ByteBuf output) {
        MultipartRequestGroupCaseBuilder caseBuilder = new MultipartRequestGroupCaseBuilder();
        MultipartRequestGroupBuilder builder = new MultipartRequestGroupBuilder();
        builder.setGroupId(new GroupId(Uint32.fromIntBits(output.readInt())));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_GROUP_BODY);
        caseBuilder.setMultipartRequestGroup(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestMeterMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(9));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestMeter());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 24);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong meterBody", message.getMultipartRequestBody(), decodeRequestMeter(out));
    }

    private static MultipartRequestMeterCase createRequestMeter() {
        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder();
        MultipartRequestMeterBuilder builder = new MultipartRequestMeterBuilder();
        builder.setMeterId(new MeterId(Uint32.valueOf(1121)));
        caseBuilder.setMultipartRequestMeter(builder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestMeterCase decodeRequestMeter(ByteBuf output) {
        MultipartRequestMeterCaseBuilder caseBuilder = new MultipartRequestMeterCaseBuilder();
        MultipartRequestMeterBuilder builder = new MultipartRequestMeterBuilder();
        builder.setMeterId(new MeterId(Uint32.fromIntBits(output.readInt())));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_METER_BODY);
        caseBuilder.setMultipartRequestMeter(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestMeterConfigMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(10));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestMeterConfig());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 24);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
        out.skipBytes(PADDING_IN_MULTIPART_REQUEST_MESSAGE);
        Assert.assertEquals("Wrong meterConfigBody", message.getMultipartRequestBody(), decodeRequestMeterConfig(out));
    }

    private static MultipartRequestMeterConfigCase createRequestMeterConfig() {
        MultipartRequestMeterConfigCaseBuilder caseBuilder = new MultipartRequestMeterConfigCaseBuilder();
        MultipartRequestMeterConfigBuilder builder = new MultipartRequestMeterConfigBuilder();
        builder.setMeterId(new MeterId(Uint32.valueOf(1133)));
        caseBuilder.setMultipartRequestMeterConfig(builder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestMeterConfigCase decodeRequestMeterConfig(ByteBuf output) {
        MultipartRequestMeterConfigCaseBuilder caseBuilder = new MultipartRequestMeterConfigCaseBuilder();
        MultipartRequestMeterConfigBuilder builder = new MultipartRequestMeterConfigBuilder();
        builder.setMeterId(new MeterId(Uint32.fromIntBits(output.readInt())));
        output.skipBytes(PADDING_IN_MULTIPART_REQUEST_METER_CONFIG_BODY);
        caseBuilder.setMultipartRequestMeterConfig(builder.build());
        return caseBuilder.build();
    }

    /**
     * Testing of {@link MultipartRequestInputFactory} for correct translation from POJO.
     */
    @Test
    public void testMultipartRequestDescMessageFactory() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(MultipartType.forValue(0));
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setMultipartRequestBody(createRequestDesc());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, (byte) 18, 16);
        Assert.assertEquals("Wrong type", message.getType().getIntValue(), out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", message.getFlags(), decodeMultipartRequestFlags(out.readShort()));
    }

    private static MultipartRequestBody createRequestDesc() {
        MultipartRequestDescCaseBuilder caseBuilder = new MultipartRequestDescCaseBuilder();
        MultipartRequestDescBuilder builder = new MultipartRequestDescBuilder();
        caseBuilder.setMultipartRequestDesc(builder.build());
        return caseBuilder.build();
    }

}
