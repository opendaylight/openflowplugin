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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;

/**
 * Translates OxmIcmpv6Code messages.
 *
 * @author michal.polkorab
 */
public class OxmIcmpv6CodeDeserializer extends AbstractOxmMatchEntryDeserializer {
    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        return processHeader(getOxmClass(), getOxmField(), input)
                .setMatchEntryValue(new Icmpv6CodeCaseBuilder()
                    .setIcmpv6Code(new Icmpv6CodeBuilder().setIcmpv6Code(readUint8(input)).build())
                    .build())
                .build();
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Icmpv6Code.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
