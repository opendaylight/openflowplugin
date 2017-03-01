/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.messages.AbstractMessageSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.request.multipart.request.body.MultipartRequestDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.request.multipart.request.body.MultipartRequestFlowTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.request.multipart.request.body.MultipartRequestPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowAggregateStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeatures;
import org.opendaylight.yangtools.yang.binding.DataContainer;

public class MultipartRequestMessageSerializer extends AbstractMessageSerializer<MultipartRequest> implements SerializerRegistryInjector {
    private static final byte PADDING_IN_MULTIPART_REQUEST_MESSAGE = 4;
    private SerializerRegistry registry;

    @Override
    public void serialize(final MultipartRequest message, final ByteBuf outBuffer) {
        final MultipartRequestBody multipartRequestBody = message.getMultipartRequestBody();
        final MultipartType multipartType = getMultipartType(multipartRequestBody);

        int index = outBuffer.writerIndex();
        super.serialize(message, outBuffer);
        outBuffer.writeShort(multipartType.getIntValue());
        outBuffer.writeShort(ByteBufUtils.fillBitMask(0, message.isRequestMore()));
        outBuffer.writeZero(PADDING_IN_MULTIPART_REQUEST_MESSAGE);

        final OFSerializer<MultipartRequestBody> serializer = registry
            .getSerializer(new MessageTypeKey<>(
                EncodeConstants.OF13_VERSION_ID,
                multipartRequestBody.getImplementedInterface()));

        serializer.serialize(multipartRequestBody, outBuffer);
        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    @Override
    protected byte getMessageType() {
        return 18;
    }

    private static MultipartType getMultipartType(final MultipartRequestBody multipartRequestBody) {
        final Class<? extends DataContainer> clazz = multipartRequestBody.getImplementedInterface();

        final MultipartType multipartType = MultipartRequestDesc.class.equals(clazz) ? MultipartType.OFPMPDESC
            : MultipartRequestFlowTableStats.class.equals(clazz) ? MultipartType.OFPMPTABLE
            : MultipartRequestGroupDesc.class.equals(clazz) ? MultipartType.OFPMPGROUPDESC
            : MultipartRequestGroupFeatures.class.equals(clazz) ? MultipartType.OFPMPGROUPFEATURES
            : MultipartRequestGroupStats.class.equals(clazz) ? MultipartType.OFPMPGROUP
            : MultipartRequestMeterFeatures.class.equals(clazz) ? MultipartType.OFPMPMETERFEATURES
            : MultipartRequestMeterStats.class.equals(clazz) ? MultipartType.OFPMPMETER
            : MultipartRequestMeterConfig.class.equals(clazz) ? MultipartType.OFPMPMETERCONFIG
            : MultipartRequestPortDesc.class.equals(clazz) ? MultipartType.OFPMPPORTDESC
            : MultipartRequestPortStats.class.equals(clazz) ? MultipartType.OFPMPPORTSTATS
            : MultipartRequestFlowStats.class.equals(clazz) ? MultipartType.OFPMPFLOW
            : MultipartRequestFlowAggregateStats.class.equals(clazz) ? MultipartType.OFPMPAGGREGATE
            : MultipartRequestTableFeatures.class.equals(clazz) ? MultipartType.OFPMPTABLEFEATURES
            : MultipartRequestQueueStats.class.equals(clazz) ? MultipartType.OFPMPQUEUE
            : MultipartRequestExperimenter.class.equals(clazz) ? MultipartType.OFPMPEXPERIMENTER
            : null;

        if (Objects.isNull(multipartType)) {
            throw new IllegalArgumentException(clazz.toString() + " is not valid multipart type");
        }

        return multipartType;
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

}
