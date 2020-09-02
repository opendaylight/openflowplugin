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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Serializes OF 1.3 CopyTtlOut actions.
 *
 * @author michal.polkorab
 */
public class OF13CopyTtlOutActionSerializer extends AbstractActionSerializer {
    public OF13CopyTtlOutActionSerializer() {
        super(ActionConstants.COPY_TTL_OUT_CODE, ActionConstants.GENERAL_ACTION_LENGTH);
    }

    @Override
    protected void serializeBody(final Action action, final ByteBuf outBuffer) {
        outBuffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);
    }
}
