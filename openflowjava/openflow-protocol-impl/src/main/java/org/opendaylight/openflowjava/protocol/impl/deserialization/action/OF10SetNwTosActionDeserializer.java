/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.tos._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF10SetNwTosActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10SetNwTosActionDeserializer extends AbstractActionDeserializer<SetNwTosCase> {
    public OF10SetNwTosActionDeserializer() {
        super(new SetNwTosCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        final ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * Short.BYTES);
        SetNwTosCaseBuilder caseBuilder = new SetNwTosCaseBuilder();
        SetNwTosActionBuilder tosBuilder = new SetNwTosActionBuilder();
        tosBuilder.setNwTos(readUint8(input));
        caseBuilder.setSetNwTosAction(tosBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.PADDING_IN_SET_NW_TOS_ACTION);
        return builder.build();
    }
}
