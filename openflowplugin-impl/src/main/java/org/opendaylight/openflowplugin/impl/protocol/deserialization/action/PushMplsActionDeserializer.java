/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

import io.netty.buffer.ByteBuf;

public class PushMplsActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf message) {
        processHeader(message);
        final int ethType = message.readUnsignedShort();
        message.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);

        return new PushMplsActionCaseBuilder()
            .setPushMplsAction(new PushMplsActionBuilder()
                    .setEthernetType(ethType)
                    .build())
            .build();
    }

    @Override
    public Action deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new PushMplsActionCaseBuilder().build();
    }

}
