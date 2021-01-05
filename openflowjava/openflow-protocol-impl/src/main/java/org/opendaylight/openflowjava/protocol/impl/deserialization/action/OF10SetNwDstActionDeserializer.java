/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.dst._case.SetNwDstActionBuilder;

/**
 * OF10SetNwDstActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetNwDstActionDeserializer extends AbstractActionCaseDeserializer<SetNwDstCase> {
    public OF10SetNwDstActionDeserializer() {
        super(new SetNwDstCaseBuilder().build());
    }

    @Override
    protected SetNwDstCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return new SetNwDstCaseBuilder()
            .setSetNwDstAction(new SetNwDstActionBuilder()
                .setIpAddress(ByteBufUtils.readIetfIpv4Address(input))
                .build())
            .build();
    }
}
