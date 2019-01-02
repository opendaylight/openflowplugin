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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;

public class SctpSourcePortEntryDeserializer extends AbstractMatchEntryDeserializer {

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        processHeader(message);
        final int port = message.readUnsignedShort();

        if (Objects.isNull(builder.getLayer4Match())) {
            builder.setLayer4Match(new SctpMatchBuilder()
                    .setSctpSourcePort(new PortNumber(port))
                    .build());
        } else if (builder.getLayer4Match() instanceof SctpMatch
            && Objects.isNull(((SctpMatch) builder.getLayer4Match()).getSctpSourcePort())) {
            builder.setLayer4Match(new SctpMatchBuilder((SctpMatch) builder.getLayer4Match())
                    .setSctpSourcePort(new PortNumber(port))
                    .build());
        } else {
            throwErrorOnMalformed(builder, "layer4Match", "sctpSourcePort");
        }
    }

}
