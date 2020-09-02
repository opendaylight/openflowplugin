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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4TypeBuilder;

/**
 * Translates OxmIcmpv4Type messages.
 *
 * @author michal.polkorab
 */
public class OxmIcmpv4TypeDeserializer extends AbstractOxmMatchEntryDeserializer {
    @Override
    public MatchEntry deserialize(final ByteBuf input) {
        return processHeader(getOxmClass(), getOxmField(), input)
                .setMatchEntryValue(new Icmpv4TypeCaseBuilder()
                    .setIcmpv4Type(new Icmpv4TypeBuilder().setIcmpv4Type(readUint8(input)).build())
                    .build())
                .build();
    }

    @Override
    protected Class<? extends MatchField> getOxmField() {
        return Icmpv4Type.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }
}
