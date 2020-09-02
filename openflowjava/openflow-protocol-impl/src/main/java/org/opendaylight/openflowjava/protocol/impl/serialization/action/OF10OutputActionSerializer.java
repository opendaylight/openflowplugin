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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Serializes OF 1.0 Output actions.
 *
 * @author michal.polkorab
 */
public class OF10OutputActionSerializer extends AbstractActionSerializer {
    public OF10OutputActionSerializer() {
        super(ActionConstants.OUTPUT_CODE, ActionConstants.GENERAL_ACTION_LENGTH);
    }

    @Override
    protected void serializeBody(final Action action, final ByteBuf outBuffer) {
        final OutputAction outputAction = ((OutputActionCase) action.getActionChoice()).getOutputAction();
        outBuffer.writeShort(outputAction.getPort().getValue().intValue());
        outBuffer.writeShort(outputAction.getMaxLength().intValue());
    }
}
