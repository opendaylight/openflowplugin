/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.exthdr._case.Ipv6ExthdrBuilder;

/**
 * Translates OxmIpv6Ext messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6ExtHdrDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmIpv6ExtHdrDeserializer() {
        super(Ipv6Exthdr.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final Ipv6ExthdrBuilder extHdrBuilder = new Ipv6ExthdrBuilder()
                .setPseudoField(convertPseudofields(input));
        if (builder.isHasMask()) {
            extHdrBuilder.setMask(OxmDeserializerHelper.convertMask(input, Short.BYTES));
        }
        builder.setMatchEntryValue(new Ipv6ExthdrCaseBuilder().setIpv6Exthdr(extHdrBuilder.build()).build());
    }

    private static Ipv6ExthdrFlags convertPseudofields(final ByteBuf input) {
        int bitmap = input.readUnsignedShort();
        final Boolean nonext = (bitmap & 1 << 0) != 0;
        final Boolean esp = (bitmap & 1 << 1) != 0;
        final Boolean auth = (bitmap & 1 << 2) != 0;
        final Boolean dest = (bitmap & 1 << 3) != 0;
        final Boolean frag = (bitmap & 1 << 4) != 0;
        final Boolean router = (bitmap & 1 << 5) != 0;
        final Boolean hop = (bitmap & 1 << 6) != 0;
        final Boolean unrep = (bitmap & 1 << 7) != 0;
        final Boolean unseq = (bitmap & 1 << 8) != 0;
        return new Ipv6ExthdrFlags(auth, dest, esp, frag, hop, nonext, router, unrep, unseq);
    }
}
