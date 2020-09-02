/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Translates OxmVlanVid messages.
 *
 * @author michal.polkorab
 */
public class OxmVlanVidDeserializer extends AbstractOxmMatchEntryDeserializer {
    public OxmVlanVidDeserializer() {
        super(VlanVid.class);
    }

    @Override
    protected void deserialize(final ByteBuf input, final MatchEntryBuilder builder) {
        final int vidEntryValue = input.readUnsignedShort();
        final VlanVidBuilder vlanBuilder = new VlanVidBuilder()
                .setCfiBit((vidEntryValue & 1 << 12) != 0) // cfi is 13-th bit
                .setVlanVid(Uint16.valueOf(vidEntryValue & (1 << 12) - 1)); // value without 13-th bit
        if (builder.isHasMask()) {
            vlanBuilder.setMask(OxmDeserializerHelper.convertMask(input, Short.BYTES));
        }
        builder.setMatchEntryValue(new VlanVidCaseBuilder().setVlanVid(vlanBuilder.build()).build());
    }
}
