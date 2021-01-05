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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.src._case.SetNwSrcActionBuilder;

/**
 * OF10SetNwSrcActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetNwSrcActionDeserializer extends AbstractActionCaseDeserializer<SetNwSrcCase> {
    public OF10SetNwSrcActionDeserializer() {
        super(new SetNwSrcCaseBuilder().build());
    }

    @Override
    protected SetNwSrcCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return new SetNwSrcCaseBuilder()
            .setSetNwSrcAction(new SetNwSrcActionBuilder()
                .setIpAddress(ByteBufUtils.readIetfIpv4Address(input))
                .build())
            .build();
    }
}
