/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * Base class for action deserializers.
 *
 * @author michal.polkorab
 */
public abstract class AbstractActionDeserializer<T extends ActionChoice> implements OFDeserializer<Action>,
        HeaderDeserializer<Action> {
    private final @NonNull Action header;

    protected AbstractActionDeserializer(final @NonNull T emptyChoice) {
        this.header = new ActionBuilder().setActionChoice(requireNonNull(emptyChoice)).build();
    }

    @Override
    public final Action deserializeHeader(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return header;
    }
}
