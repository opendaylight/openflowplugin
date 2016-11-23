/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.AbstractMatchEntrySerializer;
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
        // Add new match entry serializers to this list (order matters!)
        final List<AbstractMatchEntrySerializer> entrySerializers = new ArrayList<>();

        // Register all match entries to MatchSerializer and then inject it to provider
        provider.registerSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class),
                new MatchSerializer(entrySerializers));
    }
}
