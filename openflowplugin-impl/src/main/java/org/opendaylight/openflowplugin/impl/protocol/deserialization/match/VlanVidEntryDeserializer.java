/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;

public class VlanVidEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        final boolean hasMask = processHeader(message);
        final VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        final int vlanVidValue = message.readUnsignedShort();

        if (hasMask) {
            message.skipBytes(Short.BYTES); // Skip mask
            vlanIdBuilder
                    .setVlanId(new VlanId(0))
                    .setVlanIdPresent(true);
        } else {
            final boolean vidPresent = (vlanVidValue & 1 << 12) != 0;

            vlanIdBuilder
                    .setVlanId(new VlanId(vidPresent ? vlanVidValue & (1 << 12) - 1 : vlanVidValue))
                    .setVlanIdPresent(vidPresent);
        }

        if (builder.getVlanMatch() == null) {
            builder.setVlanMatch(new VlanMatchBuilder()
                    .setVlanId(vlanIdBuilder.build())
                    .build());
        } else if (builder.getVlanMatch().getVlanId() == null) {
            builder.setVlanMatch(new VlanMatchBuilder(builder.getVlanMatch())
                    .setVlanId(vlanIdBuilder.build())
                    .build());
        } else {
            throwErrorOnMalformed(builder, "vlanMatch", "vlanVid");
        }
    }
}
