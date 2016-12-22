/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import java.util.function.Consumer;
import java.util.function.Function;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;

import com.google.common.annotations.VisibleForTesting;

public class ActionDeserializerInjector {

    /**
     * Injects action deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        // Inject new message deserializers here using injector created by createInjector method
        final Function<Integer, Function<Class<? extends Action>, Consumer<OFDeserializer<Action>>>> injector =
                createInjector(provider, EncodeConstants.OF13_VERSION_ID);
    }

    /**
     * Create injector that will inject new deserializers into #{@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Function<Class<? extends Action>, Consumer<OFDeserializer<Action>>>> createInjector(
            final DeserializerExtensionProvider provider,
            final short version) {
        return code -> retType -> deserializer -> {
            provider.registerDeserializer(
                    new MessageCodeExperimenterKey(version, code, retType, null),
                    deserializer);
        };
    }

}
