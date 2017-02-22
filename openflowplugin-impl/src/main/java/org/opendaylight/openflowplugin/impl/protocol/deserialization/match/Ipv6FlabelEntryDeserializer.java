/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import java.util.Objects;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmDeserializerHelper;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;

import io.netty.buffer.ByteBuf;

public class Ipv6FlabelEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final Ipv6LabelBuilder ipv6labelBuilder = new Ipv6LabelBuilder()
            .setIpv6Flabel(new Ipv6FlowLabel(message.readUnsignedInt()));

        if (hasMask) {
            final byte[] mask = OxmDeserializerHelper.convertMask(message, EncodeConstants.SIZE_OF_INT_IN_BYTES);
            ipv6labelBuilder.setFlabelMask(new Ipv6FlowLabel(ByteUtil.bytesToUnsignedInt(mask)));
        }

        if (Objects.isNull(builder.getLayer3Match())) {
            builder.setLayer3Match(new Ipv6MatchBuilder()
                    .setIpv6Label(ipv6labelBuilder.build())
                    .build());
        } else if (Ipv6Match.class.isInstance(builder.getLayer3Match())
            && Objects.isNull(Ipv6Match.class.cast(builder.getLayer3Match()).getIpv6Label())) {
            final Ipv6Match match = Ipv6Match.class.cast(builder.getLayer3Match());
            builder.setLayer3Match(new Ipv6MatchBuilder(match)
                    .setIpv6Label(ipv6labelBuilder.build())
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer3Match", "ipv6Label");
        }

    }

}
