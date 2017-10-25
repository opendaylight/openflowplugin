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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;

public class IpDscpEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final short dscp = message.readUnsignedByte();

        if (Objects.isNull(builder.getIpMatch())) {
            builder.setIpMatch(new IpMatchBuilder()
                    .setIpDscp(new Dscp(dscp))
                    .build());
        } else if (Objects.isNull(builder.getIpMatch().getIpDscp())) {
            builder.setIpMatch(new IpMatchBuilder(builder.getIpMatch())
                    .setIpDscp(new Dscp(dscp))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "ipMatch", "ipDscp");
        }
    }

}
