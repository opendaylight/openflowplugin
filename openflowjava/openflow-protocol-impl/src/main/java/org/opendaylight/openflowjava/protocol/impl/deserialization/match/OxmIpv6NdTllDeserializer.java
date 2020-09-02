/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.tll._case.Ipv6NdTllBuilder;

/**
 * Translates OxmIpv6NdTll messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6NdTllDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmIpv6NdTllDeserializer() {
        super(Ipv6NdTll.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        builder.setMatchEntryValue(new Ipv6NdTllCaseBuilder()
            .setIpv6NdTll(new Ipv6NdTllBuilder().setMacAddress(OxmDeserializerHelper.convertMacAddress(input)).build())
            .build());
    }
}
