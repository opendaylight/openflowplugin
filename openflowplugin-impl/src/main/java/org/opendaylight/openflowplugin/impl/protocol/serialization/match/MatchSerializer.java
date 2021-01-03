/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializer;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerRegistry;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchSerializer implements OFSerializer<Match>, HeaderSerializer<Match>,
        MatchEntrySerializerRegistry, SerializerRegistryInjector {

    private static final Logger LOG = LoggerFactory.getLogger(MatchSerializer.class);
    private static final byte OXM_MATCH_TYPE_CODE = 1;

    private final Map<org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerKey,
            MatchEntrySerializer> entryRegistry = new LinkedHashMap<>();
    private SerializerRegistry registry;

    @Override
    public void serialize(final Match match, final ByteBuf outBuffer) {
        // Save start index in buffer
        final int matchStartIndex = outBuffer.writerIndex();

        // With OpenflowPlugin models, we cannot check difference between OXM and Standard match type
        // so all matches will be OXM
        outBuffer.writeShort(OXM_MATCH_TYPE_CODE);

        // Save length of match type
        int matchLengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // Small hack to be able to serialize only match entryRegistry externally
        serializeHeader(match, outBuffer);

        // Length of ofp_match (excluding padding)
        int matchLength = outBuffer.writerIndex() - matchStartIndex;
        outBuffer.setShort(matchLengthIndex, matchLength);

        // If we have any remaining padding, write it at end
        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            outBuffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }
    }

    @Override
    public void serializeHeader(final Match match, final ByteBuf outBuffer) {
        if (match == null) {
            LOG.debug("Match is null, skipping serialization of match entries");
            return;
        }

        // Serialize match entries
        entryRegistry.values().forEach(value -> value.serializeIfPresent(match, outBuffer));

        // Serialize match extensions
        ExtensionResolvers.getMatchExtensionResolver().getExtension(match)
            .ifPresent(extensions -> serializeExtensionList(extensions.nonnullExtensionList().values(), outBuffer));
    }

    private void serializeExtensionList(final Collection<ExtensionList> extensionList, final ByteBuf outBuffer) {
        final ExtensionConverterProvider provider = OFSessionUtil.getExtensionConvertorProvider();

        // TODO: Remove also extension converters
        for (ExtensionList extension : extensionList) {
            if (provider == null) {
                LOG.warn("No extension provider found, ignoring match entry {}",
                    extension.getExtension().implementedInterface());
                continue;
            }

            final ConvertorToOFJava<MatchEntry> converter = provider.getConverter(
                new ConverterExtensionKey<>(extension.getExtensionKey(), OFConstants.OFP_VERSION_1_3));
            if (converter == null) {
                LOG.warn("Converter for match entry {} for version {} not found.",
                    extension.getExtension().implementedInterface(), OFConstants.OFP_VERSION_1_3);
                continue;
            }

            final MatchEntry entry = converter.convert(extension.getExtension());
            final MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                EncodeConstants.OF13_VERSION_ID, entry.getOxmClass(), entry.getOxmMatchField());

            // If entry is experimenter, set experimenter ID to key
            if (entry.getOxmClass().equals(ExperimenterClass.class)) {
                key.setExperimenterId(((ExperimenterIdCase) entry.getMatchEntryValue())
                    .getExperimenter().getExperimenter().getValue());
            }

            final OFSerializer<MatchEntry> serializer = registry.getSerializer(key);
            if (serializer != null) {
                serializer.serialize(entry, outBuffer);
            } else {
                LOG.warn("Serializer for match entry {} for version {} not found.", key, OFConstants.OFP_VERSION_1_3);
            }
        }
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = requireNonNull(serializerRegistry);
    }

    @Override
    public void registerEntrySerializer(
            final org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerKey key,
            final MatchEntrySerializer serializer) {
        if (key == null || serializer == null) {
            throw new IllegalArgumentException("MatchEntrySerializerKey or Serializer is null");
        }

        final MatchEntrySerializer seInRegistry = entryRegistry.put(key, serializer);
        if (seInRegistry != null) {
            LOG.debug("Serializer for key {} overwritten. Old serializer: {}, new serializer: {}", key,
                    seInRegistry.getClass().getName(), serializer.getClass().getName());
        }
    }

    @Override
    public boolean unregisterEntrySerializer(
            final org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerKey key) {
        return entryRegistry.remove(key) != null;
    }
}
