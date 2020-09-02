/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.dst._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;

/**
 * OF10SetTpDstActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10SetTpDstActionDeserializer extends AbstractActionDeserializer<SetTpDstCase> {
    public OF10SetTpDstActionDeserializer() {
        super(new SetTpDstCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        final ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * Short.BYTES);
        SetTpDstCaseBuilder caseBuilder = new SetTpDstCaseBuilder();
        SetTpDstActionBuilder actionBuilder = new SetTpDstActionBuilder();
        actionBuilder.setPort(new PortNumber(readUint16(input).toUint32()));
        caseBuilder.setSetTpDstAction(actionBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.PADDING_IN_TP_PORT_ACTION);
        return builder.build();
    }
}
