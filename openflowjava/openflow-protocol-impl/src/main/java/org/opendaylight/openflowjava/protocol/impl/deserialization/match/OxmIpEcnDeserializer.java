/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcnBuilder;

/**
 * Translates OxmIpEnc messages.
 *
 * @author michal.polkorab
 */
public class OxmIpEcnDeserializer extends AbstractOxmMatchEntryDeserializer {
    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), input);
        addIpEcnValue(input, builder);
        return builder.build();
    }

    private static void addIpEcnValue(final ByteBuf input, final MatchEntryBuilder builder) {
        builder.setMatchEntryValue(new IpEcnCaseBuilder()
            .setIpEcn(new IpEcnBuilder().setEcn(readUint8(input)).build())
            .build());
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return IpEcn.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
