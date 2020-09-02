/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.bos._case.MplsBosBuilder;

/**
 * Translates OxmMplsBos messages.
 *
 * @author michal.polkorab
 */
public class OxmMplsBosDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmMplsBosDeserializer() {
        super(MplsBos.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        builder.setMatchEntryValue(new MplsBosCaseBuilder()
            .setMplsBos(new MplsBosBuilder().setBos(input.readUnsignedByte() != 0).build())
            .build());
    }
}
