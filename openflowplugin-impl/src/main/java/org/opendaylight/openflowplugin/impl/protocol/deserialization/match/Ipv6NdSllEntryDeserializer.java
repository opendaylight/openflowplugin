/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import java.util.Objects;

import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmDeserializerHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

import io.netty.buffer.ByteBuf;

public class Ipv6NdSllEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final MacAddress address = OxmDeserializerHelper.convertMacAddress(message);

        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv6MatchBuilder()
                    .setIpv6NdSll(address)
                    .build());
        } else if (Ipv6Match.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv6Match.class.cast(builder.getLayer3Match()).getIpv6NdSll())) {
            final Ipv6Match match = Ipv6Match.class.cast(builder.getLayer3Match());
            builder.setLayer3Match(new Ipv6MatchBuilder(match)
                    .setIpv6NdSll(address)
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv6NdSll");
        }

    }

}
