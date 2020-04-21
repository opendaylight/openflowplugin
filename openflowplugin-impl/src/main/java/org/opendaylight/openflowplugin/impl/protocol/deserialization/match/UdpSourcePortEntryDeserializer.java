/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.PortNumberRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;

public class UdpSourcePortEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        boolean hasMask = processHeader(message);
        int startPort = message.readUnsignedShort();
        int endPort;
        String portRange;
        if (hasMask) {
            endPort = message.readUnsignedShort();
            portRange = startPort + "/" + endPort;
        } else {
            portRange = String.valueOf(startPort);
        }

        if (builder.getLayer4Match() == null) {
            builder.setLayer4Match(new UdpMatchBuilder()
                    .setUdpSourcePort(new PortNumberRange(portRange))
                    .build());
        } else if (builder.getLayer4Match() instanceof UdpMatch
                && ((UdpMatch) builder.getLayer4Match()).getUdpSourcePort() == null) {
            builder.setLayer4Match(new UdpMatchBuilder((UdpMatch) builder.getLayer4Match())
                    .setUdpSourcePort(new PortNumberRange(portRange))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer4Match", "udpSourcePort");
        }
    }
}
