/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.EnqueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.enqueue._case.EnqueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.QueueId;

/**
 * OF10EnqueueActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10EnqueueActionDeserializer extends AbstractActionDeserializer {
    @Override
    public Action deserialize(ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        EnqueueActionBuilder actionBuilder = new EnqueueActionBuilder();
        actionBuilder.setPort(new PortNumber(readUint16(input).toUint32()));
        input.skipBytes(ActionConstants.PADDING_IN_ENQUEUE_ACTION);
        actionBuilder.setQueueId(new QueueId(readUint32(input)));
        return new ActionBuilder()
                .setActionChoice(new EnqueueCaseBuilder().setEnqueueAction(actionBuilder.build()).build())
                .build();
    }

    @Override
    protected ActionChoice getType() {
        return new OutputActionCaseBuilder().build();
    }
}
