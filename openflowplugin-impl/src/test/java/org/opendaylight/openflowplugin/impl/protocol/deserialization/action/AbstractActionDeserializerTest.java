/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;

public abstract class AbstractActionDeserializerTest extends AbstractDeserializerTest {

    private OFDeserializer<Action> deserializer;

    @Override
    protected void init() {
        deserializer = getRegistry().getDeserializer(
                new MessageCodeExperimenterKey(EncodeConstants.OF13_VERSION_ID, getType(), Action.class, null));
    }

    protected void writeHeader(ByteBuf message) {
        message.writeShort(getType());
        message.writeShort(getLength());
    }

    protected Action deserializeAction(ByteBuf message) {
        return deserializer.deserialize(message);
    }

    protected abstract short getType();
    protected abstract short getLength();

}
