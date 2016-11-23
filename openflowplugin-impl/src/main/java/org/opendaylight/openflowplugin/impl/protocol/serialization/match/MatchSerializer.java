/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.stream.Stream;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchSerializer implements OFSerializer<Match>, HeaderSerializer<Match> {
    private static final Logger LOG = LoggerFactory.getLogger(MatchSerializer.class);
    private static final byte OXM_MATCH_TYPE_CODE = 1;
    private final Stream<AbstractMatchEntrySerializer> entries;

    /**
     * Create new instance of MatchSerializer
     * @param entries Match entry serializers
     */
    public MatchSerializer(final Stream<AbstractMatchEntrySerializer> entries) {
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

        // Serialize match entries
        entries.filter(entry -> entry.matchTypeCheck(match)).forEach(entry -> entry.serialize(match, outBuffer));

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
        entries.filter(entry -> entry.matchTypeCheck(match)).forEach(entry -> entry.serialize(match, outBuffer));
    }
}
