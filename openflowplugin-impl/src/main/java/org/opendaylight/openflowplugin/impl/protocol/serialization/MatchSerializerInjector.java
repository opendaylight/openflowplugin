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
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializer;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerRegistry;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MatchEntrySerializerKeyImpl;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MatchSerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 * Util class for injecting new match serializers into OpenflowJava
 */
public class MatchSerializerInjector {

    /**
     * Injects match serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    public static void injectSerializers(final SerializerExtensionProvider provider) {
        final MatchSerializer serializer = new MatchSerializer();
        provider.registerSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class),
                serializer);

        // Inject all match entry serializers to match serializers using injector created by createInjector method
        final Function<Integer, Function<Integer, Consumer<MatchEntrySerializer>>> injector =
                createInjector(serializer, EncodeConstants.OF13_VERSION_ID);
    }

    /**
     * Create injector that will inject new match entry serializers into #{@link org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerRegistry}
     * @param registry Match entry serializer registry
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Function<Integer, Consumer<MatchEntrySerializer>>> createInjector(
            final MatchEntrySerializerRegistry registry,
            final byte version) {
        return oxmClass -> oxmField -> serializer ->
                registry.registerEntrySerializer(
                        new MatchEntrySerializerKeyImpl(version, oxmClass, oxmField),
                        serializer);
    }
}
