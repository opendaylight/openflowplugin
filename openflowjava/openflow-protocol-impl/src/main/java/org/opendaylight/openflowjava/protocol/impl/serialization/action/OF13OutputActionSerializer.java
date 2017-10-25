/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * @author michal.polkorab
 *
 */
public class OF13OutputActionSerializer extends AbstractActionSerializer {

    @Override
    public void serialize(Action action, ByteBuf outBuffer) {
        super.serialize(action, outBuffer);
        outBuffer.writeInt(((OutputActionCase) action.getActionChoice()).getOutputAction()
                .getPort().getValue().intValue());
        outBuffer.writeShort(((OutputActionCase) action.getActionChoice()).getOutputAction().getMaxLength());
        outBuffer.writeZero(ActionConstants.OUTPUT_PADDING);
    }

    @Override
    protected int getType() {
        return ActionConstants.OUTPUT_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.LARGER_ACTION_LENGTH;
    }

}
