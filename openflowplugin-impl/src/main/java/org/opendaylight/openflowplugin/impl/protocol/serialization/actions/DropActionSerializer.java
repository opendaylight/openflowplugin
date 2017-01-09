/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

public class DropActionSerializer extends AbstractActionSerializer {

    @Override
    public void serialize(Action action, ByteBuf outBuffer) {
        // NOOP
    }

    @Override
    public void serializeHeader(Action action, ByteBuf outBuffer) {
        // NOOP
    }

    @Override
    protected int getLength() {
        return 0;
    }

    @Override
    protected int getType() {
        return 0;
    }
}
