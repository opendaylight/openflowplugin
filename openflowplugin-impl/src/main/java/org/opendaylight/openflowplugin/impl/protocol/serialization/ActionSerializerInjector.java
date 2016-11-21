/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.keys.ActionSerializerKey;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.CopyTtlInActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.CopyTtlOutActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.DecMplsTtlActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.DecNwTtlActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.GroupActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.OutputActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.PopMplsActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.PopPbbActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.PopVlanActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.PushMplsActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.PushPbbActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.PushVlanActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.SetMplsTtlActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.SetNwTtlActionSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.SetQueueActionSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;

/**
 * Util class for injecting new action serializers into OpenflowJava
 */
class ActionSerializerInjector {

    /**
     * Injects serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Inject new action serializers here using injector created by createInjector method
        final Function<Class<? extends Action>, Consumer<OFSerializer<? extends Action>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);

        injector.apply(CopyTtlInCase.class).accept(new CopyTtlInActionSerializer());
        injector.apply(CopyTtlOutCase.class).accept(new CopyTtlOutActionSerializer());
        injector.apply(DecMplsTtlCase.class).accept(new DecMplsTtlActionSerializer());
        injector.apply(DecNwTtlCase.class).accept(new DecNwTtlActionSerializer());
        injector.apply(GroupActionCase.class).accept(new GroupActionSerializer());
        injector.apply(OutputActionCase.class).accept(new OutputActionSerializer());
        injector.apply(PopMplsActionCase.class).accept(new PopMplsActionSerializer());
        injector.apply(PopPbbActionCase.class).accept(new PopPbbActionSerializer());
        injector.apply(PopVlanActionCase.class).accept(new PopVlanActionSerializer());
        injector.apply(PushMplsActionCase.class).accept(new PushMplsActionSerializer());
        injector.apply(PushPbbActionCase.class).accept(new PushPbbActionSerializer());
        injector.apply(PushVlanActionCase.class).accept(new PushVlanActionSerializer());
        injector.apply(SetMplsTtlActionCase.class).accept(new SetMplsTtlActionSerializer());
        injector.apply(SetNwTtlActionCase.class).accept(new SetNwTtlActionSerializer());
        injector.apply(SetQueueActionCase.class).accept(new SetQueueActionSerializer());
    }

    /**
     * Create injector that will inject new serializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Class<? extends Action>, Consumer<OFSerializer<? extends Action>>> createInjector(final SerializerExtensionProvider provider,
                                                                                                final byte version) {
        return type -> serializer ->
                provider.registerSerializer(
                        new ActionSerializerKey<>(version, type, null),
                        serializer);
    }
}