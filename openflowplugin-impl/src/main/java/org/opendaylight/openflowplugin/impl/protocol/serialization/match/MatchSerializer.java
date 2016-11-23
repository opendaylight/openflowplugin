/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchSerializer implements OFSerializer<Match>, HeaderSerializer<Match>, SerializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MatchSerializer.class);
    private static final byte OXM_MATCH_TYPE_CODE = 1;
    private final List<AbstractMatchEntrySerializer> entries;
    private SerializerRegistry registry;

    /**
     * Create new instance of MatchSerializer
     * @param entries Match entry serializers
     */
    public MatchSerializer(final List<AbstractMatchEntrySerializer> entries) {
        this.entries = entries;
    }

    @Override
    public void serialize(Match match, ByteBuf outBuffer) {
        if (match == null) {
            LOG.debug("Match is null");
            return;
        }

        // Save start index in buffer
        int matchStartIndex = outBuffer.writerIndex();

        // With OpenflowPlugin models, we cannot check difference between OXM and Standard match type
        // so all matches will be OXM
        outBuffer.writeShort(OXM_MATCH_TYPE_CODE);

        // Save length of match type
        int matchLengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // Small hack to be able to serialize only match entries externally
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
    public void serializeHeader(Match match, ByteBuf outBuffer) {
        if (match == null) {
            LOG.debug("Match is null");
            return;
        }

        // Serialize match entries
        entries.stream().filter(entry -> entry.matchTypeCheck(match)).forEach(entry -> entry.serialize(match, outBuffer));

        // Serialize match extensions
        ExtensionResolvers.getMatchExtensionResolver().getExtension(match).transform(extensions -> {
            if (Objects.nonNull(extensions)) {
                extensions.getExtensionList().forEach(extension-> {
                    // TODO: Remove also extension converters
                    final MatchEntry entry = OFSessionUtil
                            .getExtensionConvertorProvider()
                            .<MatchEntry>getConverter(new ConverterExtensionKey<>(
                                    extension.getExtensionKey(),
                                    OFConstants.OFP_VERSION_1_3))
                            .convert(extension.getExtension());

                    final MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                            EncodeConstants.OF13_VERSION_ID, entry.getOxmClass(), entry.getOxmMatchField());

                    // If entry is experimenter, set experimenter ID to key
                    if (entry.getOxmClass().equals(ExperimenterClass.class)) {
                        key.setExperimenterId(ExperimenterIdCase.class.cast(entry.getMatchEntryValue())
                                .getExperimenter().getExperimenter().getValue());
                    }

                    final OFSerializer<MatchEntry> entrySerializer = registry.getSerializer(key);
                    entrySerializer.serialize(entry, outBuffer);
                });
            }

            return extensions;
        });
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
