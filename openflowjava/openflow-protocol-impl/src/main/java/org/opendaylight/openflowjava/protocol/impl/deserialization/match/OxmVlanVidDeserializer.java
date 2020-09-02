/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Translates OxmVlanVid messages.
 *
 * @author michal.polkorab
 */
public class OxmVlanVidDeserializer extends AbstractOxmMatchEntryDeserializer
        implements OFDeserializer<MatchEntry> {
    @Override
    public MatchEntry deserialize(ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addVlanVidValue(input, builder);
        return builder.build();
    }

    private static void addVlanVidValue(ByteBuf input, MatchEntryBuilder builder) {
        final VlanVidCaseBuilder caseBuilder = new VlanVidCaseBuilder();
        VlanVidBuilder vlanBuilder = new VlanVidBuilder();
        int vidEntryValue = input.readUnsignedShort();
        vlanBuilder.setCfiBit((vidEntryValue & 1 << 12) != 0); // cfi is 13-th bit
        vlanBuilder.setVlanVid(Uint16.valueOf(vidEntryValue & (1 << 12) - 1)); // value without 13-th bit
        if (builder.isHasMask()) {
            vlanBuilder.setMask(OxmDeserializerHelper
                    .convertMask(input, Short.BYTES));
        }
        caseBuilder.setVlanVid(vlanBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return VlanVid.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
