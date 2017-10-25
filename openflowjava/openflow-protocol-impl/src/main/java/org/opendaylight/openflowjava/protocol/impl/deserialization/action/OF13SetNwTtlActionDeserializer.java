/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * @author michal.polkorab
 *
 */
public class OF13SetNwTtlActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf input) {
        ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        SetNwTtlCaseBuilder caseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder actionBuilder = new SetNwTtlActionBuilder();
        actionBuilder.setNwTtl(input.readUnsignedByte());
        caseBuilder.setSetNwTtlAction(actionBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.SET_NW_TTL_PADDING);
        return builder.build();
    }

    @Override
    protected ActionChoice getType() {
        return new SetNwTtlCaseBuilder().build();
    }

}
