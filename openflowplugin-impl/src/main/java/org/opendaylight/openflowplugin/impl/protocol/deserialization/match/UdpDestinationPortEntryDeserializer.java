/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;

public class UdpDestinationPortEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        boolean hasMask = processHeader(message);
        final int port = message.readUnsignedShort();
        int portMask = 0;
        if (hasMask) {
            portMask = message.readUnsignedShort();
        }

        if (builder.getLayer4Match() == null) {
            UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder()
                    .setUdpDestinationPort(new PortNumber(port));
            if (hasMask) {
                udpMatchBuilder.setUdpDestinationPortMask(new PortNumber(portMask));
            }
            builder.setLayer4Match(udpMatchBuilder.build());
        } else if (builder.getLayer4Match() instanceof UdpMatch
                && ((UdpMatch) builder.getLayer4Match()).getUdpDestinationPort() == null) {
            UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder((UdpMatch) builder.getLayer4Match())
                    .setUdpDestinationPort(new PortNumber(port));
            if (hasMask) {
                udpMatchBuilder.setUdpDestinationPortMask(new PortNumber(portMask));
            }
            builder.setLayer4Match(udpMatchBuilder.build());
        } else {
            throwErrorOnMalformed(builder, "layer4Match", "udpDestinationPort");
        }
    }
}
