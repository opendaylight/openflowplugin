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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

public class Ipv6NdTargetEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final Ipv6Address address = ByteBufUtils.readIetfIpv6Address(message);

        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv6MatchBuilder()
                    .setIpv6NdTarget(address)
                    .build());
        } else if (builder.getLayer3Match() instanceof Ipv6Match
            && Objects.isNull(((Ipv6Match) builder.getLayer3Match()).getIpv6NdTarget())) {
            final Ipv6Match match = (Ipv6Match) builder.getLayer3Match();
            builder.setLayer3Match(new Ipv6MatchBuilder(match)
                    .setIpv6NdTarget(address)
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv6NdTarget");
        }

    }

}
