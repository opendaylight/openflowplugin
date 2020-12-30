/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.flabel._case.Ipv6FlabelBuilder;

/**
 * Translates Ipv6Flabel messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6FlabelDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmIpv6FlabelDeserializer() {
        super(Ipv6Flabel.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final Ipv6FlabelBuilder labelBuilder = new Ipv6FlabelBuilder()
                .setIpv6Flabel(new Ipv6FlowLabel(readUint32(input)));
        if (builder.getHasMask()) {
            labelBuilder.setMask(OxmDeserializerHelper.convertMask(input, Integer.BYTES));
        }
        builder.setMatchEntryValue(new Ipv6FlabelCaseBuilder().setIpv6Flabel(labelBuilder.build()).build());
    }
}
