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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4DstBuilder;

/**
 * Translates OxmIpv4Dst messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv4DstDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmIpv4DstDeserializer() {
        super(Ipv4Dst.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final Ipv4DstBuilder ipv4Builder = new Ipv4DstBuilder()
                .setIpv4Address(ByteBufUtils.readIetfIpv4Address(input));
        if (builder.isHasMask()) {
            ipv4Builder.setMask(OxmDeserializerHelper.convertMask(input, EncodeConstants.GROUPS_IN_IPV4_ADDRESS));
        }
        builder.setMatchEntryValue(new Ipv4DstCaseBuilder().setIpv4Dst(ipv4Builder.build()).build());
    }
}
