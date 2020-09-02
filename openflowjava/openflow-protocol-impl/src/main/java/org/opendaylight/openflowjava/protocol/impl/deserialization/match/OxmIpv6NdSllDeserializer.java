/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.sll._case.Ipv6NdSllBuilder;

/**
 * Translates OxmIpv6NdSll messages.
 *
 * @author michal.polkorab
 */
public class OxmIpv6NdSllDeserializer extends AbstractOxmMatchEntryDeserializer {

    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addIpv6NdSllValue(input, builder);
        return builder.build();
    }

    private static void addIpv6NdSllValue(final ByteBuf input, final MatchEntryBuilder builder) {
        Ipv6NdSllCaseBuilder caseBuilder = new Ipv6NdSllCaseBuilder();
        Ipv6NdSllBuilder ndBuilder = new Ipv6NdSllBuilder();
        ndBuilder.setMacAddress(OxmDeserializerHelper.convertMacAddress(input));
        caseBuilder.setIpv6NdSll(ndBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Ipv6NdSll.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
