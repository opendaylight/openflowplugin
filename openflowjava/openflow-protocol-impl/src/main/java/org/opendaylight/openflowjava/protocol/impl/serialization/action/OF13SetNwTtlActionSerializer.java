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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Serializes OF 1.3 SetNwTtl actions.
 *
 * @author michal.polkorab
 */
public class OF13SetNwTtlActionSerializer extends AbstractActionSerializer {
    public OF13SetNwTtlActionSerializer() {
        super(ActionConstants.SET_NW_TTL_CODE, ActionConstants.GENERAL_ACTION_LENGTH);
    }

    @Override
    protected void serializeBody(final Action action, final ByteBuf outBuffer) {
        outBuffer.writeByte(((SetNwTtlCase) action.getActionChoice()).getSetNwTtlAction().getNwTtl().intValue());
        outBuffer.writeZero(ActionConstants.SET_NW_TTL_PADDING);
    }
}
