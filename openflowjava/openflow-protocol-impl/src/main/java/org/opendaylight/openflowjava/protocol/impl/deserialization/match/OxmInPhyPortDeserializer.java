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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.phy.port._case.InPhyPortBuilder;

/**
 * Translates OxmInPhyPort messages.
 *
 * @author michal.polkorab
 */
public class OxmInPhyPortDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmInPhyPortDeserializer() {
        super(InPhyPort.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        builder.setMatchEntryValue(new InPhyPortCaseBuilder()
            .setInPhyPort(new InPhyPortBuilder().setPortNumber(new PortNumber(readUint32(input))).build())
            .build());
    }
}
