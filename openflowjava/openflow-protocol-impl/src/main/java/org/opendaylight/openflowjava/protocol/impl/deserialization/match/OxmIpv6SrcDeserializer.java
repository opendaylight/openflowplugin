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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;

/**
 * Translates OxmIpv6Src messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6SrcDeserializer extends AbstractOxmMatchEntryDeserializer
        implements OFDeserializer<MatchEntry> {

    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addIpv6SrcValue(input, builder);
        return builder.build();
    }

    private static void addIpv6SrcValue(final ByteBuf input, final MatchEntryBuilder builder) {
        Ipv6SrcCaseBuilder caseBuilder = new Ipv6SrcCaseBuilder();
        Ipv6SrcBuilder ipv6Builder = new Ipv6SrcBuilder();
        ipv6Builder.setIpv6Address(ByteBufUtils.readIetfIpv6Address(input));
        if (builder.isHasMask()) {
            ipv6Builder.setMask(OxmDeserializerHelper.convertMask(
                    input, EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES));
        }
        caseBuilder.setIpv6Src(ipv6Builder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Ipv6Src.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
