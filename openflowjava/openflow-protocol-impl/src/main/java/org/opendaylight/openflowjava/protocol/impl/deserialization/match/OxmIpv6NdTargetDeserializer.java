/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.target._case.Ipv6NdTargetBuilder;

/**
 * Translates OxmIpv6NdTarget messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6NdTargetDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmIpv6NdTargetDeserializer() {
        super(Ipv6NdTarget.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        builder.setMatchEntryValue(new Ipv6NdTargetCaseBuilder()
            .setIpv6NdTarget(new Ipv6NdTargetBuilder().setIpv6Address(ByteBufUtils.readIetfIpv6Address(input)).build())
            .build());
    }
}
