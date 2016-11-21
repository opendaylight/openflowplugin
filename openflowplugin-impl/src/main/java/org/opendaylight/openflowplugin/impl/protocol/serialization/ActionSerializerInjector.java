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
import org.opendaylight.openflowplugin.impl.protocol.serialization.keys.ActionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;

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
    }
}