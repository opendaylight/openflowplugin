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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;

/**
 * @author michal.polkorab
 *
 */
public class OF13OutputActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf input) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder actionBuilder = new OutputActionBuilder();
        actionBuilder.setPort(new PortNumber(input.readUnsignedInt()));
        actionBuilder.setMaxLength(input.readUnsignedShort());
        caseBuilder.setOutputAction(actionBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.OUTPUT_PADDING);
        return builder.build();
    }

    @Override
    protected ActionChoice getType() {
        return new OutputActionCaseBuilder().build();
    }

}
