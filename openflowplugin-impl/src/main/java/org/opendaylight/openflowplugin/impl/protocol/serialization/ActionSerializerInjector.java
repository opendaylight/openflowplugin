/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.actions.AbstractActionSerializer;
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
import org.opendaylight.openflowplugin.impl.protocol.serialization.keys.ActionSerializerKey;
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

/**
 * Util class for injecting new action serializers into OpenflowJava
 */
class ActionSerializerInjector {

    /**
     * Injects serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        // Helper serialization function
        final Function<Class<? extends Action>, Consumer<AbstractActionSerializer>> registrator =
                type -> serializer ->
                        provider.registerSerializer(
                                new ActionSerializerKey<>(EncodeConstants.OF13_VERSION_ID, type, null),
                                serializer);

        // Action serializers
        registrator.apply(CopyTtlInCase.class).accept(new CopyTtlInActionSerializer());
        registrator.apply(CopyTtlOutCase.class).accept(new CopyTtlOutActionSerializer());
        registrator.apply(DecMplsTtlCase.class).accept(new DecMplsTtlActionSerializer());
        registrator.apply(DecNwTtlCase.class).accept(new DecNwTtlActionSerializer());
        registrator.apply(GroupActionCase.class).accept(new GroupActionSerializer());
        registrator.apply(OutputActionCase.class).accept(new OutputActionSerializer());
        registrator.apply(PopMplsActionCase.class).accept(new PopMplsActionSerializer());
        registrator.apply(PopPbbActionCase.class).accept(new PopPbbActionSerializer());
        registrator.apply(PopVlanActionCase.class).accept(new PopVlanActionSerializer());
        registrator.apply(PushMplsActionCase.class).accept(new PushMplsActionSerializer());
        registrator.apply(PushPbbActionCase.class).accept(new PushPbbActionSerializer());
        registrator.apply(PushVlanActionCase.class).accept(new PushVlanActionSerializer());
    }
}