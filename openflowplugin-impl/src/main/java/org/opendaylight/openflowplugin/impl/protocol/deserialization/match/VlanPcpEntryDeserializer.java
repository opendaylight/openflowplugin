/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;

public class VlanPcpEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final short pcp = message.readUnsignedByte();

        if (Objects.isNull(builder.getVlanMatch())) {
            builder.setVlanMatch(new VlanMatchBuilder()
                    .setVlanPcp(new VlanPcp(pcp))
                    .build());
        } else if (Objects.isNull(builder.getVlanMatch().getVlanPcp())) {
            builder.setVlanMatch(new VlanMatchBuilder(builder.getVlanMatch())
                    .setVlanPcp(new VlanPcp(pcp))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "vlanMatch", "vlanPcp");
        }
    }

}
