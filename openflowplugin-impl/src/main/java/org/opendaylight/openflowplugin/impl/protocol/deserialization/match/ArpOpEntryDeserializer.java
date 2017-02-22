/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;

public class ArpOpEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final Layer3Match layer3Match = builder.getLayer3Match();
        final int arpOp = message.readUnsignedShort();

        if (Objects.isNull(layer3Match)) {
            builder.setLayer3Match(new ArpMatchBuilder()
                .setArpOp(arpOp)
                .build());
        } else if (ArpMatch.class.isInstance(layer3Match)
            && Objects.isNull(ArpMatch.class.cast(layer3Match).getArpOp())) {
            builder.setLayer3Match(new ArpMatchBuilder(ArpMatch.class.cast(layer3Match))
                .setArpOp(arpOp)
                .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "arpOp");
        }
    }

}
