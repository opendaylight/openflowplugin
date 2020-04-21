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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;

public class TcpSourcePortEntryDeserializer extends AbstractMatchEntryDeserializer {

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
            builder.setLayer4Match(new TcpMatchBuilder()
                    .setTcpSourcePort(new PortNumberRange(portRange))
                    .build());
        } else if (builder.getLayer4Match() instanceof TcpMatch
            && ((TcpMatch) builder.getLayer4Match()).getTcpSourcePort() == null) {
            builder.setLayer4Match(new TcpMatchBuilder((TcpMatch) builder.getLayer4Match())
                    .setTcpSourcePort(new PortNumberRange(portRange))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer4Match", "tcpSource");
        }
    }
}
