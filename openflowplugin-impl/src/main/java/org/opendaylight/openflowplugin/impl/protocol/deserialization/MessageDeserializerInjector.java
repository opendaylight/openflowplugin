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
import org.opendaylight.openflowplugin.impl.protocol.deserialization.messages.AsyncConfigMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.messages.FlowMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.messages.GroupMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.messages.MeterMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.messages.PacketInMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.messages.PortMessageDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart.MultipartReplyMessageDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;

final class MessageDeserializerInjector {
    private MessageDeserializerInjector() {
        // Hidden on purpose
    }

    /**
     * Injects message deserializers into provided.
     * {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     *
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Inject new message deserializers here using injector created by createInjector method
        final Function<Integer, Function<Class<? extends OfHeader>, Consumer<OFDeserializer<? extends OfHeader>>>>
                injector = createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(10).apply(org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709
                .PacketInMessage.class).accept(new PacketInMessageDeserializer(provider));
        injector.apply(19).apply(MultipartReply.class).accept(new MultipartReplyMessageDeserializer(provider));
        injector.apply(27).apply(AsyncConfigMessage.class).accept(new AsyncConfigMessageDeserializer());
    }

    /**
     * Reverts original message deserializers in provided.
     * {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     *
     * @param provider OpenflowJava deserializer extension provider
     */
    static void revertDeserializers(final DeserializerExtensionProvider provider) {
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 10));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 10),
                PacketInMessage.class);
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 19));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 19),
                MultipartReplyMessage.class);
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 27));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 27),
                GetAsyncOutput.class);
    }

    /**
     * Create injector that will inject new deserializers into.
     * #{@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     *
     * @param provider OpenflowJava deserializer extension provider
     * @param version  Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Function<Class<? extends OfHeader>, Consumer<OFDeserializer<? extends OfHeader>>>>
                createInjector(final DeserializerExtensionProvider provider, final short version) {
        return code -> retType -> deserializer -> {
            provider.unregisterDeserializerMapping(new TypeToClassKey(version, code));
            provider.registerDeserializerMapping(new TypeToClassKey(version, code), retType);
            provider.registerDeserializer(new MessageCodeKey(version, code, retType), deserializer);
        };
    }

    @VisibleForTesting
    static void injectLegacyDeserializers(final DeserializerExtensionProvider provider) {
        final Function<Integer, Function<Class<? extends OfHeader>, Consumer<OFDeserializer<? extends OfHeader>>>>
                injector = createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(14).apply(FlowMessage.class).accept(new FlowMessageDeserializer(provider));
        injector.apply(15).apply(GroupMessage.class).accept(new GroupMessageDeserializer(provider));
        injector.apply(29).apply(MeterMessage.class).accept(new MeterMessageDeserializer(provider));
        injector.apply(16).apply(PortMessage.class).accept(new PortMessageDeserializer());
    }

    @VisibleForTesting
    static void revertLegacyDeserializers(final DeserializerExtensionProvider provider) {
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 14));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 14),
                FlowModInput.class);
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 15));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 15),
                GroupModInput.class);
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 29));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 29),
                MeterModInput.class);
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 16));
        provider.registerDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 16),
                PortModInput.class);
        provider.unregisterDeserializerMapping(new TypeToClassKey(EncodeConstants.OF13_VERSION_ID, 19));
    }
}
