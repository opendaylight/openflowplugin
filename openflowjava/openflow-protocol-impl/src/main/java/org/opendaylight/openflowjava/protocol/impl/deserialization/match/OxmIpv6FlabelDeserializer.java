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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.flabel._case.Ipv6FlabelBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OxmIpv6FlabelDeserializer extends AbstractOxmMatchEntryDeserializer
        implements OFDeserializer<MatchEntry> {

    @Override
    public MatchEntry deserialize(ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addIpv6FlabelValue(input, builder);
        return builder.build();
    }

    private static void addIpv6FlabelValue(ByteBuf input, MatchEntryBuilder builder) {
        Ipv6FlabelCaseBuilder caseBuilder = new Ipv6FlabelCaseBuilder();
        Ipv6FlabelBuilder labelBuilder = new Ipv6FlabelBuilder();
        labelBuilder.setIpv6Flabel(new Ipv6FlowLabel(input.readUnsignedInt()));
        if (builder.isHasMask()) {
            labelBuilder.setMask(OxmDeserializerHelper.convertMask(input,
                    EncodeConstants.SIZE_OF_INT_IN_BYTES));
        }
        caseBuilder.setIpv6Flabel(labelBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Ipv6Flabel.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }

}
