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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class IpDscpEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(final ByteBuf message, final MatchBuilder builder) {
        processHeader(message);
        final Uint8 dscp = readUint8(message);

        if (builder.getIpMatch() == null) {
            builder.setIpMatch(new IpMatchBuilder()
                    .setIpDscp(new Dscp(dscp))
                    .build());
        } else if (builder.getIpMatch().getIpDscp() == null) {
            builder.setIpMatch(new IpMatchBuilder(builder.getIpMatch())
                    .setIpDscp(new Dscp(dscp))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "ipMatch", "ipDscp");
        }
    }
}
