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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmDeserializerHelper;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

public class Ipv6ExtHeaderEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final Ipv6ExtHeaderBuilder extHeaderBuilder = new Ipv6ExtHeaderBuilder()
            .setIpv6Exthdr(message.readUnsignedShort());

        if (hasMask) {
            final byte[] mask = OxmDeserializerHelper.convertMask(message, EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
            extHeaderBuilder.setIpv6ExthdrMask(ByteUtil.bytesToUnsignedShort(mask));
        }

        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv6MatchBuilder()
                    .setIpv6ExtHeader(extHeaderBuilder.build())
                    .build());
        } else if (Ipv6Match.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv6Match.class.cast(builder.getLayer3Match()).getIpv6ExtHeader())) {
            final Ipv6Match match = Ipv6Match.class.cast(builder.getLayer3Match());
            builder.setLayer3Match(new Ipv6MatchBuilder(match)
                    .setIpv6ExtHeader(extHeaderBuilder.build())
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv6ExtHeader");
        }

    }

}
