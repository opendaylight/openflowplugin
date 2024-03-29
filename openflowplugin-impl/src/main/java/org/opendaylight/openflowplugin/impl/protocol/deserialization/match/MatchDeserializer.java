/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializerRegistry;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchDeserializer implements OFDeserializer<Match>, HeaderDeserializer<Match>,
        MatchEntryDeserializerRegistry, MatchEntryDeserializer, DeserializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MatchDeserializer.class);

    private final Map<MatchEntryDeserializerKey, MatchEntryDeserializer> entryRegistry = new HashMap<>();
    private final MatchPath matchPath;

    private DeserializerRegistry registry = null;

    public MatchDeserializer(final MatchPath matchPath) {
        this.matchPath = matchPath;
    }

    @Override
    public Match deserialize(final ByteBuf inBuffer) {
        if (inBuffer.readableBytes() <= 0) {
            return null;
        }

        final MatchBuilder builder = new MatchBuilder();

        // OFP do not have any method to differentiate between OXM and standard match, so we do not care about type
        inBuffer.readUnsignedShort();
        final int length = inBuffer.readUnsignedShort();

        final int startIndex = inBuffer.readerIndex();
        final int entriesLength = length - 2 * Short.BYTES;

        while (inBuffer.readerIndex() - startIndex < entriesLength) {
            deserializeEntry(inBuffer, builder);
        }

        int paddingRemainder = length % EncodeConstants.PADDING;

        if (paddingRemainder != 0) {
            inBuffer.skipBytes(EncodeConstants.PADDING - paddingRemainder);
        }

        return builder.build();
    }

    @Override
    public Match deserializeHeader(final ByteBuf inBuffer) {
        final MatchBuilder builder = new MatchBuilder();
        deserializeEntry(inBuffer, builder);
        return builder.build();
    }

    @Override
    public void deserializeEntry(final ByteBuf inBuffer, final MatchBuilder builder) {
        if (inBuffer.readableBytes() <= 0) {
            return;
        }
        int oxmClass = inBuffer.getUnsignedShort(inBuffer.readerIndex());
        int oxmField = inBuffer.getUnsignedByte(inBuffer.readerIndex() + Short.BYTES) >>> 1;

        final MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(
                EncodeConstants.OF_VERSION_1_3, oxmClass, oxmField);

        if (oxmClass == EncodeConstants.EXPERIMENTER_VALUE) {
            long expId = inBuffer.getUnsignedInt(inBuffer.readerIndex() + Short.BYTES + 2 * Byte.BYTES);
            key.setExperimenterId(Uint32.valueOf(expId));
        }

        final MatchEntryDeserializer entryDeserializer = entryRegistry.get(key);

        if (entryDeserializer != null) {
            entryDeserializer.deserializeEntry(inBuffer, builder);
        } else {
            final OFDeserializer<MatchEntry> deserializer = registry.getDeserializer(key);
            MatchExtensionHelper.injectExtension(EncodeConstants.OF_VERSION_1_3,
                    deserializer.deserialize(inBuffer), builder, matchPath);
        }
    }

    @Override
    public void registerEntryDeserializer(final MatchEntryDeserializerKey key,
            final MatchEntryDeserializer deserializer) {
        if (key == null || deserializer == null) {
            throw new IllegalArgumentException("MatchEntryDeserializerKey or Deserializer is null");
        }

        final MatchEntryDeserializer desInRegistry = entryRegistry.put(key, deserializer);

        if (desInRegistry != null) {
            LOG.debug("Deserializer for key {} overwritten. Old deserializer: {}, new deserializer: {}", key,
                    desInRegistry.getClass().getName(), deserializer.getClass().getName());
        }
    }

    @Override
    public boolean unregisterEntryDeserializer(final MatchEntryDeserializerKey key) {
        if (key == null) {
            throw new IllegalArgumentException("MatchEntryDeserializerKey is null");
        }

        return entryRegistry.remove(key) != null;
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
