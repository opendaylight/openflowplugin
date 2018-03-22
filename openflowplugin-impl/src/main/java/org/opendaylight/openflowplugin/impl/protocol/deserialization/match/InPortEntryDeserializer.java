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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InPortEntryDeserializer extends AbstractMatchEntryDeserializer {
    private static final Logger LOG = LoggerFactory.getLogger(InPortEntryDeserializer.class);

    @Override
    public void deserializeEntry(ByteBuf message, MatchBuilder builder) {
        LOG.info("in deserializeEntry");
        processHeader(message);
        final long port = message.readUnsignedInt();

        if (Objects.isNull(builder.getInPort())) {
            builder.setInPort(new NodeConnectorId(OpenflowPortsUtil.getProtocolAgnosticPortUri(EncodeConstants
                    .OF13_VERSION_ID, port)));
        } else {
            throwErrorOnMalformed(builder, "inPort");
        }
    }

}
