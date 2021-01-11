/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class SetNwTtlActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(final ByteBuf message) {
        processHeader(message);
        final Uint8 nwTtl = readUint8(message);
        message.skipBytes(ActionConstants.SET_NW_TTL_PADDING);

        return new SetNwTtlActionCaseBuilder()
                .setSetNwTtlAction(new SetNwTtlActionBuilder()
                        .setNwTtl(nwTtl)
                        .build())
                .build();
    }

    @Override
    public Action deserializeHeader(final ByteBuf message) {
        processHeader(message);
        return new SetNwTtlActionCaseBuilder().build();
    }
}
