/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.keys.TypeToClassKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyFlowAggregateStatsDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyFlowStatsDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyFlowTableStatsDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyGroupDescDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyGroupFeaturesDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyMeterStatsDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyPortStatsDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyQueueStatsDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class MultipartDeserializerInjector {

    /**
     * Injects message deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        final short version = EncodeConstants.OF13_VERSION_ID;

        // Inject main multipart reply deserializer
        injectMultipartReplyDeserializer(provider, version);

        // Inject new multipart body deserializers here using injector created by createInjector method
        final Function<Integer, Consumer<OFDeserializer<? extends MultipartReplyBody>>> injector =
                createInjector(provider, version);

        injector.apply(MultipartType.OFPMPFLOW.getIntValue()).accept(new MultipartReplyFlowStatsDeserializer());
        injector.apply(MultipartType.OFPMPAGGREGATE.getIntValue()).accept(new MultipartReplyFlowAggregateStatsDeserializer());
        injector.apply(MultipartType.OFPMPTABLE.getIntValue()).accept(new MultipartReplyFlowTableStatsDeserializer());
        injector.apply(MultipartType.OFPMPPORTSTATS.getIntValue()).accept(new MultipartReplyPortStatsDeserializer());
        injector.apply(MultipartType.OFPMPQUEUE.getIntValue()).accept(new MultipartReplyQueueStatsDeserializer());
        injector.apply(MultipartType.OFPMPGROUPDESC.getIntValue()).accept(new MultipartReplyGroupDescDeserializer());
        injector.apply(MultipartType.OFPMPGROUPFEATURES.getIntValue()).accept(new MultipartReplyGroupFeaturesDeserializer());
        injector.apply(MultipartType.OFPMPMETER.getIntValue()).accept(new MultipartReplyMeterStatsDeserializer());
    }

    /**
     * Register main multipart reply deserializer
     * @param provider OpenflowJava deserializer extension provider
     * @param version Openflow version
     */
    static void injectMultipartReplyDeserializer(final DeserializerExtensionProvider provider, final short version) {
        final short code = 19;
        final Class<? extends OfHeader> retType = MultipartReply.class;
        provider.unregisterDeserializerMapping(new TypeToClassKey(version, code));
        provider.registerDeserializerMapping(new TypeToClassKey(version, code), retType);
        provider.registerDeserializer(
                new MessageCodeKey(version, code, retType),
                new MultipartReplyMessageDeserializer());
    }

    /**
     * Create injector that will inject new deserializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Consumer<OFDeserializer<? extends MultipartReplyBody>>> createInjector(
            final DeserializerExtensionProvider provider,
            final short version) {
        return code -> deserializer -> {
            provider.registerDeserializer(
                    new MessageCodeKey(version, code, MultipartReplyBody.class),
                    deserializer);
        };
    }

}
