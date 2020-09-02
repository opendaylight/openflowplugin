/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.action;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Shared serializer for empty action types.
 */
public final class EmptyActionSerializer extends AbstractActionSerializer {
    public EmptyActionSerializer(final short type) {
        super(type, ActionConstants.GENERAL_ACTION_LENGTH);
    }

    @Override
    protected void serializeBody(final Action action, final ByteBuf outBuffer) {
        outBuffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type()).toString();
    }
}
