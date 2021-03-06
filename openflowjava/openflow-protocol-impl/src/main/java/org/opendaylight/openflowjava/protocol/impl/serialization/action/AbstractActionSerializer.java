/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Base class for an action serializer.
 *
 * @author michal.polkorab
 */
public abstract class AbstractActionSerializer implements OFSerializer<Action>, HeaderSerializer<Action> {
    private final short type;
    private final short length;

    protected AbstractActionSerializer(final short type, final short length) {
        this.type = type;
        this.length = length;
    }

    @Override
    public final void serialize(final Action input, final ByteBuf outBuffer) {
        outBuffer.writeShort(type);
        outBuffer.writeShort(length);
        serializeBody(input, outBuffer);
    }

    @Override
    public final void serializeHeader(final Action input, final ByteBuf outBuffer) {
        outBuffer.writeShort(type);
        outBuffer.writeShort(ActionConstants.ACTION_IDS_LENGTH);
    }

    /**
     * Returns the numeric representation of action type.
     *
     * @return Numeric representation of action type.
     */
    protected final short type() {
        return type;
    }

    protected abstract void serializeBody(Action input, ByteBuf outBuffer);
}
