/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.experimenter._case.MultipartRequestExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueue;

/**
 * Translates StatsRequest messages.
 *
 * @author michal.polkorab
 */
public class OF10StatsRequestInputFactory implements OFSerializer<MultipartRequestInput> {
    private static final byte MESSAGE_TYPE = 16;
    private static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY = 1;
    private static final byte PADDING_IN_MULTIPART_REQUEST_AGGREGATE_BODY = 1;
    private static final byte PADDING_IN_MULTIPART_REQUEST_PORT_BODY = 6;
    private static final byte PADING_IN_QUEUE_BODY = 2;

    private final SerializerLookup registry;

    public OF10StatsRequestInputFactory(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public void serialize(final MultipartRequestInput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.OFHEADER_SIZE);
        outBuffer.writeShort(message.getType().getIntValue());
        outBuffer.writeShort(createMultipartRequestFlagsBitmask(message.getFlags()));
        if (message.getMultipartRequestBody() instanceof MultipartRequestDescCase) {
            serializeDescBody();
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestFlowCase) {
            serializeFlowBody(message.getMultipartRequestBody(), outBuffer);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestAggregateCase) {
            serializeAggregateBody(message.getMultipartRequestBody(), outBuffer);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestTableCase) {
            serializeTableBody();
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestPortStatsCase) {
            serializePortBody(message.getMultipartRequestBody(), outBuffer);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestQueueCase) {
            serializeQueueBody(message.getMultipartRequestBody(), outBuffer);
        } else if (message.getMultipartRequestBody() instanceof MultipartRequestExperimenterCase) {
            serializeExperimenterBody(message.getMultipartRequestBody(), outBuffer);
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static int createMultipartRequestFlagsBitmask(final MultipartRequestFlags flags) {
        return ByteBufUtils.fillBitMask(0, flags.isOFPMPFREQMORE());
    }

    private void serializeDescBody() {
        // The body of MultiPartRequestDesc is empty
    }

    private void serializeTableBody() {
     // The body of MultiPartTable is empty
    }

    private void serializeFlowBody(final MultipartRequestBody multipartRequestBody, final ByteBuf output) {
        MultipartRequestFlowCase flowCase = (MultipartRequestFlowCase) multipartRequestBody;
        MultipartRequestFlow flow = flowCase.getMultipartRequestFlow();
        OFSerializer<MatchV10> matchSerializer = registry.getSerializer(new MessageTypeKey<>(
                EncodeConstants.OF10_VERSION_ID, MatchV10.class));
        matchSerializer.serialize(flow.getMatchV10(), output);
        output.writeByte(flow.getTableId().shortValue());
        output.writeZero(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY);
        output.writeShort(flow.getOutPort().intValue());
    }

    private void serializeAggregateBody(final MultipartRequestBody multipartRequestBody, final ByteBuf output) {
        MultipartRequestAggregateCase aggregateCase = (MultipartRequestAggregateCase) multipartRequestBody;
        MultipartRequestAggregate aggregate = aggregateCase.getMultipartRequestAggregate();
        OFSerializer<MatchV10> matchSerializer = registry.getSerializer(new MessageTypeKey<>(
                EncodeConstants.OF10_VERSION_ID, MatchV10.class));
        matchSerializer.serialize(aggregate.getMatchV10(), output);
        output.writeByte(aggregate.getTableId().shortValue());
        output.writeZero(PADDING_IN_MULTIPART_REQUEST_AGGREGATE_BODY);
        output.writeShort(aggregate.getOutPort().intValue());
    }

    private static void serializePortBody(final MultipartRequestBody multipartRequestBody, final ByteBuf output) {
        MultipartRequestPortStatsCase portstatsCase = (MultipartRequestPortStatsCase) multipartRequestBody;
        MultipartRequestPortStats portstats = portstatsCase.getMultipartRequestPortStats();
        output.writeShort(portstats.getPortNo().intValue());
        output.writeZero(PADDING_IN_MULTIPART_REQUEST_PORT_BODY);
    }

    private static void serializeQueueBody(final MultipartRequestBody multipartRequestBody, final ByteBuf output) {
        MultipartRequestQueueCase queueCase = (MultipartRequestQueueCase) multipartRequestBody;
        MultipartRequestQueue queue = queueCase.getMultipartRequestQueue();
        output.writeShort(queue.getPortNo().intValue());
        output.writeZero(PADING_IN_QUEUE_BODY);
        output.writeInt(queue.getQueueId().intValue());
    }

    private void serializeExperimenterBody(final MultipartRequestBody multipartRequestBody, final ByteBuf output) {
        MultipartRequestExperimenterCase expCase = (MultipartRequestExperimenterCase) multipartRequestBody;
        MultipartRequestExperimenter experimenter = expCase.getMultipartRequestExperimenter();
        final long expId = experimenter.getExperimenter().getValue().longValue();

        // write experimenterId
        output.writeInt((int) expId);

        OFSerializer<ExperimenterDataOfChoice> serializer = registry.getSerializer(
                ExperimenterSerializerKeyFactory.createMultipartRequestSerializerKey(
                        EncodeConstants.OF10_VERSION_ID, expId,
                        -1 /* in order not to collide with OF >= 1.3 codecs*/));
        serializer.serialize(experimenter.getExperimenterDataOfChoice(), output);
    }
}
