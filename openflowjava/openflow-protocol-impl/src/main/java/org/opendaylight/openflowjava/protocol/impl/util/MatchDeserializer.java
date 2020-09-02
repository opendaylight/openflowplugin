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
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.StandardMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;

/**
 * Deserializes ofp_match (OpenFlow v1.3) and its oxm_fields structures.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class MatchDeserializer implements OFDeserializer<Match> {
    private final DeserializerRegistry registry;

    public MatchDeserializer(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public Match deserialize(final ByteBuf input) {
        if (input.readableBytes() > 0) {
            MatchBuilder builder = new MatchBuilder();
            int type = input.readUnsignedShort();
            int length = input.readUnsignedShort();
            switch (type) {
                case 0:
                    builder.setType(StandardMatchType.class);
                    break;
                case 1:
                    builder.setType(OxmMatchType.class);
                    break;
                default:
                    break;
            }
            CodeKeyMaker keyMaker = CodeKeyMakerFactory
                    .createMatchEntriesKeyMaker(EncodeConstants.OF13_VERSION_ID);
            List<MatchEntry> entries = ListDeserializer.deserializeList(EncodeConstants.OF13_VERSION_ID,
                    length - 2 * Short.BYTES, input, keyMaker, registry);
            builder.setMatchEntry(entries);
            int paddingRemainder = length % EncodeConstants.PADDING;
            if (paddingRemainder != 0) {
                input.skipBytes(EncodeConstants.PADDING - paddingRemainder);
            }
            return builder.build();
        }
        return null;
    }
}
