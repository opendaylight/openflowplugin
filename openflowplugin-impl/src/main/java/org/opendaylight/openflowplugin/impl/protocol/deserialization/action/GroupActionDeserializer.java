/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;

import io.netty.buffer.ByteBuf;

public class GroupActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf message) {
        processHeader(message);

        return new GroupActionCaseBuilder()
            .setGroupAction(new GroupActionBuilder()
                    .setGroupId(message.readUnsignedInt())
                    .build())
            .build();
    }

    @Override
    public Action deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new GroupActionCaseBuilder().build();
    }

}
