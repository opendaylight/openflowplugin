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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.spa._case.ArpSpaBuilder;

/**
 * Translates OxmArpSpa messages.
 *
 * @author michal.polkorab
 */
public class OxmArpSpaDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmArpSpaDeserializer() {
        super(ArpSpa.VALUE);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final ArpSpaBuilder arpBuilder = new ArpSpaBuilder()
                .setIpv4Address(ByteBufUtils.readIetfIpv4Address(input));
        if (builder.getHasMask()) {
            arpBuilder.setMask(OxmDeserializerHelper.convertMask(input, EncodeConstants.GROUPS_IN_IPV4_ADDRESS));
        }
        builder.setMatchEntryValue(new ArpSpaCaseBuilder().setArpSpa(arpBuilder.build()).build());
    }
}
