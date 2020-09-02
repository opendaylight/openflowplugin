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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;

/**
 * Translates OxmIpv4Src messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv4SrcDeserializer extends AbstractOxmMatchEntryDeserializer {
    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addIpv4SrcValue(input, builder);
        return builder.build();
    }

    private static void addIpv4SrcValue(final ByteBuf input, final MatchEntryBuilder builder) {
        Ipv4SrcCaseBuilder caseBuilder = new Ipv4SrcCaseBuilder();
        Ipv4SrcBuilder ipv4Builder = new Ipv4SrcBuilder();
        ipv4Builder.setIpv4Address(ByteBufUtils.readIetfIpv4Address(input));
        if (builder.isHasMask()) {
            ipv4Builder.setMask(OxmDeserializerHelper.convertMask(input, EncodeConstants.GROUPS_IN_IPV4_ADDRESS));
        }
        caseBuilder.setIpv4Src(ipv4Builder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Ipv4Src.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
