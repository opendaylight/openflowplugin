/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
 * Base serializer for empty action types.
 */
public abstract class AbstractEmptyActionSerializer extends AbstractActionSerializer {
    protected AbstractEmptyActionSerializer(final short type) {
        super(type, ActionConstants.GENERAL_ACTION_LENGTH);
    }

    @Override
    protected final void serializeBody(final Action action, final ByteBuf outBuffer) {
        outBuffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);
    }
}
