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
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmDeserializerHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

public class EthernetSourceEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final EthernetMatch ethernetMatch = builder.getEthernetMatch();
        final EthernetSourceBuilder ethernetSourceBuilder = new EthernetSourceBuilder();
        ethernetSourceBuilder.setAddress(OxmDeserializerHelper.convertMacAddress(message));

        if (hasMask) {
            ethernetSourceBuilder.setMask(OxmDeserializerHelper.convertMacAddress(message));
        }

        if (Objects.isNull(ethernetMatch)) {
            builder.setEthernetMatch(new EthernetMatchBuilder()
                    .setEthernetSource(ethernetSourceBuilder.build())
                    .build());
        } else if (Objects.isNull(ethernetMatch.getEthernetSource())) {
            builder.setEthernetMatch(new EthernetMatchBuilder(ethernetMatch)
                    .setEthernetSource(ethernetSourceBuilder.build())
                    .build());
        } else {
            throwErrorOnMalformed(builder, "ethernetMatch", "ethernetSource");
        }
    }

}
