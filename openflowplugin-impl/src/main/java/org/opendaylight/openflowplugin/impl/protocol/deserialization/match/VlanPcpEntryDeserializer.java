/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class VlanPcpEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(final ByteBuf message, final MatchBuilder builder) {
        processHeader(message);
        final Uint8 pcp = readUint8(message);

        if (builder.getVlanMatch() == null) {
            builder.setVlanMatch(new VlanMatchBuilder()
                    .setVlanPcp(new VlanPcp(pcp))
                    .build());
        } else if (builder.getVlanMatch().getVlanPcp() == null) {
            builder.setVlanMatch(new VlanMatchBuilder(builder.getVlanMatch())
                    .setVlanPcp(new VlanPcp(pcp))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "vlanMatch", "vlanPcp");
        }
    }
}
