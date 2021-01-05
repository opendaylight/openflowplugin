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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;

/**
 * OF13OutputActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13OutputActionDeserializer extends AbstractActionDeserializer<OutputActionCase> {
    public OF13OutputActionDeserializer() {
        super(new OutputActionCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var action = new OutputActionBuilder()
            .setPort(new PortNumber(readUint32(input)))
            .setMaxLength(readUint16(input))
            .build();
        input.skipBytes(ActionConstants.OUTPUT_PADDING);

        return new ActionBuilder()
            .setActionChoice(new OutputActionCaseBuilder().setOutputAction(action).build())
            .build();
    }
}
