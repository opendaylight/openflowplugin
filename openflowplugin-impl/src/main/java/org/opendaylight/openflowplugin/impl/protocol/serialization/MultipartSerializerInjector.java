/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestDescSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestExperimenterSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestFlowAggregateStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestFlowStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestFlowTableStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestGroupDescSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestGroupFeaturesSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestGroupStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestMeterConfigSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestMeterFeaturesSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestMeterStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestPortDescSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestPortStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestQueueStatsSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.MultipartRequestTableFeaturesSerializer;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body.MultipartRequestTableFeatures;

/**
 * Util class for injecting new multipart serializers into OpenflowJava
 */
class MultipartSerializerInjector {

    /**
     * Injects multipart serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new message serializers here using injector created by createInjector method
        final Function<Class<? extends MultipartRequestBody>, Consumer<OFSerializer<MultipartRequestBody>>> injector =
            createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        MultipartMatchFieldSerializerInjector.injectSerializers(provider);
        MultipartTableFeaturesSerializerInjector.injectSerializers(provider);

        injector.apply(MultipartRequestDesc.class).accept(new MultipartRequestDescSerializer());
        injector.apply(MultipartRequestFlowTableStats.class).accept(new MultipartRequestFlowTableStatsSerializer());
        injector.apply(MultipartRequestGroupDesc.class).accept(new MultipartRequestGroupDescSerializer());
        injector.apply(MultipartRequestGroupFeatures.class).accept(new MultipartRequestGroupFeaturesSerializer());
        injector.apply(MultipartRequestGroupStats.class).accept(new MultipartRequestGroupStatsSerializer());
        injector.apply(MultipartRequestMeterFeatures.class).accept(new MultipartRequestMeterFeaturesSerializer());
        injector.apply(MultipartRequestMeterStats.class).accept(new MultipartRequestMeterStatsSerializer());
        injector.apply(MultipartRequestMeterConfig.class).accept(new MultipartRequestMeterConfigSerializer());
        injector.apply(MultipartRequestPortDesc.class).accept(new MultipartRequestPortDescSerializer());
        injector.apply(MultipartRequestPortStats.class).accept(new MultipartRequestPortStatsSerializer());
        injector.apply(MultipartRequestQueueStats.class).accept(new MultipartRequestQueueStatsSerializer());
        injector.apply(MultipartRequestFlowStats.class).accept(new MultipartRequestFlowStatsSerializer());
        injector.apply(MultipartRequestFlowAggregateStats.class).accept(new MultipartRequestFlowAggregateStatsSerializer());
        injector.apply(MultipartRequestExperimenter.class).accept(new MultipartRequestExperimenterSerializer());
        injector.apply(MultipartRequestTableFeatures.class).accept(new MultipartRequestTableFeaturesSerializer());
    }

    /**
     * Create injector that will inject new multipart serializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Class<? extends MultipartRequestBody>, Consumer<OFSerializer<MultipartRequestBody>>> createInjector(
        final SerializerExtensionProvider provider,
        final byte version) {
        return type -> serializer ->
            provider.registerSerializer(
                new MessageTypeKey<>(version, type),
                serializer);
    }

}
