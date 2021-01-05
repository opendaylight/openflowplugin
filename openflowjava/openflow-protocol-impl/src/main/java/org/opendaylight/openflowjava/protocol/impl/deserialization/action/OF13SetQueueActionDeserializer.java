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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.queue._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF13SetQueueActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13SetQueueActionDeserializer extends AbstractActionDeserializer<SetQueueCase> {
    public OF13SetQueueActionDeserializer() {
        super(new SetQueueCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);

        return new ActionBuilder()
            .setActionChoice(new SetQueueCaseBuilder()
                .setSetQueueAction(new SetQueueActionBuilder().setQueueId(readUint32(input)).build())
                .build())
            .build();
    }
}
