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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.pcp._case.VlanPcpBuilder;

/**
 * Translates OxmVlanPcp messages.
 *
 * @author michal.polkorab
 */
public class OxmVlanPcpDeserializer extends AbstractOxmMatchEntryDeserializer
        implements OFDeserializer<MatchEntry> {

    @Override
    public MatchEntry deserialize(ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addVlanPcpValue(input, builder);
        return builder.build();
    }

    private static void addVlanPcpValue(ByteBuf input, MatchEntryBuilder builder) {
        VlanPcpCaseBuilder caseBuilder = new VlanPcpCaseBuilder();
        VlanPcpBuilder vlanBuilder = new VlanPcpBuilder();
        vlanBuilder.setVlanPcp(input.readUnsignedByte());
        caseBuilder.setVlanPcp(vlanBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return VlanPcp.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
