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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tpa._case.ArpTpaBuilder;

/**
 * Translates OxmArpTpa messages.
 *
 * @author michal.polkorab
 */
public class OxmArpTpaDeserializer extends AbstractOxmMatchEntryDeserializer {
    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addArpTpaValue(input, builder);
        return builder.build();
    }

    private static void addArpTpaValue(final ByteBuf input, final MatchEntryBuilder builder) {
        ArpTpaCaseBuilder caseBuilder = new ArpTpaCaseBuilder();
        ArpTpaBuilder arpBuilder = new ArpTpaBuilder();
        arpBuilder.setIpv4Address(ByteBufUtils.readIetfIpv4Address(input));
        if (builder.isHasMask()) {
            arpBuilder.setMask(OxmDeserializerHelper.convertMask(input, EncodeConstants.GROUPS_IN_IPV4_ADDRESS));
        }
        caseBuilder.setArpTpa(arpBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return ArpTpa.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
