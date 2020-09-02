/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;

/**
 * Translates OxmUdpDst messages.
 *
 * @author michal.polkorab
 */
public class OxmUdpDstDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmUdpDstDeserializer() {
        super(UdpDst.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        builder.setMatchEntryValue(new UdpDstCaseBuilder()
            .setUdpDst(new UdpDstBuilder().setPort(new PortNumber(readUint16(input))).build())
            .build());
    }
}
