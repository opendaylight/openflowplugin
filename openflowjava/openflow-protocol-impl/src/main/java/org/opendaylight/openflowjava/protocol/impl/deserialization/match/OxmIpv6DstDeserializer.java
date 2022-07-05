/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;

/**
 * Translates OxmIpv6Dst messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6DstDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmIpv6DstDeserializer() {
        super(Ipv6Dst.VALUE);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final Ipv6DstBuilder ipv6Builder = new Ipv6DstBuilder()
                .setIpv6Address(ByteBufUtils.readIetfIpv6Address(input));
        if (builder.getHasMask()) {
            ipv6Builder.setMask(OxmDeserializerHelper.convertMask(input,
                EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES));
        }
        builder.setMatchEntryValue(new Ipv6DstCaseBuilder().setIpv6Dst(ipv6Builder.build()).build());
    }
}
