/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowAggregateStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;

public class MultipartRequestFlowAggregateStatsSerializer implements OFSerializer<MultipartRequestBody>, SerializerRegistryInjector {

    private static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_01 = 3;
    private static final byte PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_02 = 4;
    private SerializerRegistry registry;

    @Override
    public void serialize(final MultipartRequestBody multipartRequestBody, final ByteBuf byteBuf) {
        final MultipartRequestFlowAggregateStats multipartRequestFlowAggregateStats = MultipartRequestFlowAggregateStats
            .class
            .cast(multipartRequestBody);

        byteBuf.writeByte(MoreObjects.firstNonNull(multipartRequestFlowAggregateStats.getTableId(), OFConstants.OFPTT_ALL).byteValue());
        byteBuf.writeZero(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_01);
        byteBuf.writeInt(MoreObjects.firstNonNull(multipartRequestFlowAggregateStats.getOutPort(), OFConstants.OFPP_ANY).intValue());
        byteBuf.writeInt(MoreObjects.firstNonNull(multipartRequestFlowAggregateStats.getOutGroup(), OFConstants.OFPG_ANY).intValue());
        byteBuf.writeZero(PADDING_IN_MULTIPART_REQUEST_FLOW_BODY_02);
        byteBuf.writeLong(MoreObjects.firstNonNull(multipartRequestFlowAggregateStats.getCookie(), new FlowCookie(OFConstants.DEFAULT_COOKIE)).getValue().longValue());
        byteBuf.writeLong(MoreObjects.firstNonNull(multipartRequestFlowAggregateStats.getCookieMask(), new FlowCookie(OFConstants.DEFAULT_COOKIE_MASK)).getValue().longValue());

        registry.<Match, OFSerializer<Match>>getSerializer(
            new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class))
            .serialize(multipartRequestFlowAggregateStats.getMatch(), byteBuf);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
