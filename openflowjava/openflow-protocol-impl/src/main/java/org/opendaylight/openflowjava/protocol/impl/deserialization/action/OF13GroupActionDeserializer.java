/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF13GroupActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13GroupActionDeserializer extends AbstractActionDeserializer {
    @Override
    public Action deserialize(ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return new ActionBuilder()
                .setActionChoice(new GroupCaseBuilder()
                    .setGroupAction(new GroupActionBuilder().setGroupId(readUint32(input)).build())
                    .build())
                .build();
    }

    @Override
    protected ActionChoice getType() {
        return new GroupCaseBuilder().build();
    }
}
