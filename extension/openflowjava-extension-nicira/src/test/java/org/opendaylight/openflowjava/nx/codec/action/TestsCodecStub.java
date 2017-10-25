/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

public class TestsCodecStub extends AbstractActionCodec {

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        // TODO Auto-generated method stub

    }

    @Override
    public Action deserialize(ByteBuf message) {
        // TODO Auto-generated method stub
        return null;
    }

}
