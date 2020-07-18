/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PacketType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PacketTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.packet.type._case.PacketTypeBuilder;

public class OxmPacketTypeDeserializer extends AbstractOxmMatchEntryDeserializer implements OFDeserializer<MatchEntry> {
    @Override
    protected Class<? extends MatchField> getOxmField() {
        return PacketType.class;
    }

    @Override
    protected Class<? extends OxmClassBase> getOxmClass() {
        return OpenflowBasicClass.class;
    }

    @Override
    public MatchEntry deserialize(ByteBuf message) {
        final MatchEntryBuilder builder = processHeader(getOxmClass(), getOxmField(), message);
        PacketTypeCaseBuilder caseBuilder = new PacketTypeCaseBuilder();
        PacketTypeBuilder packetTypeBuilder = new PacketTypeBuilder();
        packetTypeBuilder.setPacketType(readUint32(message));
        caseBuilder.setPacketType(packetTypeBuilder.build());
        builder.setMatchEntryValue(caseBuilder.build());
        return builder.build();
    }
}
