/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

public abstract class AbstractActionSerializer implements OFSerializer<Action>, HeaderSerializer<Action> {
    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
        outBuffer.writeShort(getLength());
    }

    @Override
    public void serializeHeader(Action input, ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
        outBuffer.writeShort(ActionConstants.ACTION_IDS_LENGTH);
    }

    /**
     * @return numeric representation of action type
     */
    protected abstract int getType();

    /**
     * @return action length
     */
    protected abstract int getLength();
}
