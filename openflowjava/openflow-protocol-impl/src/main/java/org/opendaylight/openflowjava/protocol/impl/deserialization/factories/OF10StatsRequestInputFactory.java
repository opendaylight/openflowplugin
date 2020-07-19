/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.desc._case.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yangtools.yang.common.Empty;

/**
 * Translates MultipartRequestInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR") // FB doesn't recognize Objects.requireNonNull
public class OF10StatsRequestInputFactory
        implements OFDeserializer<MultipartRequestInput>, DeserializerRegistryInjector {
    private DeserializerRegistry registry;
    private static final byte FLOW_PADDING_1 = 1;
    private static final byte AGGREGATE_PADDING_1 = 1;

    @Override
    public MultipartRequestInput deserialize(ByteBuf rawMessage) {
        Objects.requireNonNull(registry);

        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_0)
                .setXid(readUint32(rawMessage));
        int type = rawMessage.readUnsignedShort();
        final MultipartType multipartType = getMultipartType(type);
        builder.setType(multipartType);
        builder.setFlags(getMultipartRequestFlags(rawMessage.readUnsignedShort()));
        switch (multipartType) {
            case OFPMPDESC:
                builder.setMultipartRequestBody(setDesc(rawMessage));
                break;
            case OFPMPFLOW:
                builder.setMultipartRequestBody(setFlow(rawMessage));
                break;
            case OFPMPAGGREGATE:
                builder.setMultipartRequestBody(setAggregate(rawMessage));
                break;
            case OFPMPTABLE:
                builder.setMultipartRequestBody(setTable(rawMessage));
                break;
            case OFPMPPORTSTATS:
                builder.setMultipartRequestBody(setPortStats(rawMessage));
                break;
            case OFPMPQUEUE:
                builder.setMultipartRequestBody(setQueue(rawMessage));
                break;
            case OFPMPEXPERIMENTER:
                builder.setMultipartRequestBody(setExperimenter(rawMessage));
                break;
            default:
                break;
        }
        return builder.build();
    }

    private static MultipartRequestExperimenterCase setExperimenter(ByteBuf input) {
        MultipartRequestExperimenterCaseBuilder caseBuilder = new MultipartRequestExperimenterCaseBuilder();
        MultipartRequestExperimenterBuilder experimenterBuilder = new MultipartRequestExperimenterBuilder();
        caseBuilder.setMultipartRequestExperimenter(experimenterBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestQueueCase setQueue(ByteBuf input) {
        final MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder queueBuilder = new MultipartRequestQueueBuilder();
        queueBuilder.setPortNo(readUint16(input).toUint32());
        input.skipBytes(2);
        queueBuilder.setQueueId(readUint32(input));
        caseBuilder.setMultipartRequestQueue(queueBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestPortStatsCase setPortStats(ByteBuf input) {
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder portBuilder = new MultipartRequestPortStatsBuilder();
        portBuilder.setPortNo(readUint16(input).toUint32());
        caseBuilder.setMultipartRequestPortStats(portBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestTableCase setTable(ByteBuf input) {
        MultipartRequestTableCaseBuilder caseBuilder = new MultipartRequestTableCaseBuilder();
        MultipartRequestTableBuilder tableBuilder = new MultipartRequestTableBuilder();
        tableBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestTable(tableBuilder.build());
        return caseBuilder.build();
    }

    private MultipartRequestAggregateCase setAggregate(ByteBuf input) {
        final MultipartRequestAggregateCaseBuilder caseBuilder = new MultipartRequestAggregateCaseBuilder();
        MultipartRequestAggregateBuilder aggregateBuilder = new MultipartRequestAggregateBuilder();
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        aggregateBuilder.setMatchV10(matchDeserializer.deserialize(input));
        aggregateBuilder.setTableId(readUint8(input));
        input.skipBytes(AGGREGATE_PADDING_1);
        aggregateBuilder.setOutPort(readUint16(input).toUint32());
        caseBuilder.setMultipartRequestAggregate(aggregateBuilder.build());
        return caseBuilder.build();
    }

    private MultipartRequestFlowCase setFlow(ByteBuf input) {
        final MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder flowBuilder = new MultipartRequestFlowBuilder();
        OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
        flowBuilder.setMatchV10(matchDeserializer.deserialize(input));
        flowBuilder.setTableId(readUint8(input));
        input.skipBytes(FLOW_PADDING_1);
        flowBuilder.setOutPort(readUint16(input).toUint32());
        caseBuilder.setMultipartRequestFlow(flowBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartRequestDescCase setDesc(ByteBuf input) {
        MultipartRequestDescCaseBuilder caseBuilder = new MultipartRequestDescCaseBuilder();
        MultipartRequestDescBuilder descBuilder = new MultipartRequestDescBuilder();
        descBuilder.setEmpty(Empty.getInstance());
        caseBuilder.setMultipartRequestDesc(descBuilder.build());
        return caseBuilder.build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static MultipartRequestFlags getMultipartRequestFlags(int input) {
        final Boolean _oFPMPFREQMORE = (input & 1 << 0) != 0;
        MultipartRequestFlags flag = new MultipartRequestFlags(_oFPMPFREQMORE);
        return flag;
    }

    private static MultipartType getMultipartType(int input) {
        return MultipartType.forValue(input);
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
