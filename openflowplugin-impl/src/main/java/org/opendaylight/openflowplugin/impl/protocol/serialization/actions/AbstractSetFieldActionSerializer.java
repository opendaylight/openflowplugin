/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;

public abstract class AbstractSetFieldActionSerializer extends AbstractActionSerializer<Action> {
    private final SerializerLookup registry;

    protected AbstractSetFieldActionSerializer(final SerializerLookup registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public void serialize(final Action input, final ByteBuf outBuffer) {
        final OFSerializer<Action> serializer = registry.getSerializer(
            new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, SetFieldCase.class));

        serializer.serialize(buildAction(input), outBuffer);
    }

    /**
     * Build #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase}
     * from #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action}.
     *
     * @param input input action
     * @return set field action
     */
    protected abstract SetFieldCase buildAction(Action input);

    @Override
    protected int getType() {
        return ActionConstants.SET_FIELD_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }
}
