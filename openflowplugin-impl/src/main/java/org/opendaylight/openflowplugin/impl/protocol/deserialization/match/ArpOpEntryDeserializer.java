/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class ArpOpEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(final ByteBuf message, final MatchBuilder builder) {
        processHeader(message);
        final Layer3Match layer3Match = builder.getLayer3Match();
        final Uint16 arpOp = readUint16(message);

        if (layer3Match == null) {
            builder.setLayer3Match(new ArpMatchBuilder()
                .setArpOp(arpOp)
                .build());
        } else if (layer3Match instanceof ArpMatch && ((ArpMatch) layer3Match).getArpOp() == null) {
            builder.setLayer3Match(new ArpMatchBuilder((ArpMatch) layer3Match)
                .setArpOp(arpOp)
                .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "arpOp");
        }
    }
}
