/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.StandardMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serializes ofp_match (OpenFlow v1.3).
 *
 * @author michal.polkorab
 * @author timotej.kubas
 */
public class OF13MatchSerializer implements OFSerializer<Match> {
    private static final Logger LOG = LoggerFactory.getLogger(OF13MatchSerializer.class);
    private static final byte STANDARD_MATCH_TYPE_CODE = 0;
    private static final byte OXM_MATCH_TYPE_CODE = 1;

    private final SerializerLookup registry;

    public OF13MatchSerializer(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public void serialize(final Match match, final ByteBuf outBuffer) {
        if (match == null) {
            LOG.debug("Match is null");
            return;
        }
        final int matchStartIndex = outBuffer.writerIndex();
        serializeType(match, outBuffer);
        int matchLengthIndex = outBuffer.writerIndex();
        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        serializeMatchEntries(match.getMatchEntry(), outBuffer);
        // Length of ofp_match (excluding padding)
        int matchLength = outBuffer.writerIndex() - matchStartIndex;
        outBuffer.setShort(matchLengthIndex, matchLength);
        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            outBuffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }
    }

    private static void serializeType(final Match match, final ByteBuf out) {
        if (match.getType().isAssignableFrom(StandardMatchType.class)) {
            out.writeShort(STANDARD_MATCH_TYPE_CODE);
        } else if (match.getType().isAssignableFrom(OxmMatchType.class)) {
            out.writeShort(OXM_MATCH_TYPE_CODE);
        }
    }

    /**
     * Serializes MatchEntries.
     *
     * @param matchEntries list of match entries (oxm_fields)
     * @param out output ByteBuf
     */
    public void serializeMatchEntries(final List<MatchEntry> matchEntries, final ByteBuf out) {
        if (matchEntries == null) {
            LOG.debug("Match entries are null");
            return;
        }
        for (MatchEntry entry : matchEntries) {
            MatchEntrySerializerKey<?, ?> key = new MatchEntrySerializerKey<>(
                    EncodeConstants.OF13_VERSION_ID, entry.getOxmClass(), entry.getOxmMatchField());
            if (entry.getOxmClass().equals(ExperimenterClass.class)) {
                ExperimenterIdCase entryValue = (ExperimenterIdCase) entry.getMatchEntryValue();
                key.setExperimenterId(entryValue.getExperimenter().getExperimenter().getValue());
            } else {
                key.setExperimenterId(null);
            }
            OFSerializer<MatchEntry> entrySerializer = registry.getSerializer(key);
            entrySerializer.serialize(entry, out);
        }
    }
}
