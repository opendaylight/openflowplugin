/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

/**
 * Common class for AbstractActionDeserializers which do not carry any data beyond the header.
 */
public abstract class AbstractEmptyActionDeserializer<T extends ActionChoice> extends AbstractActionDeserializer<T> {
    protected AbstractEmptyActionDeserializer(final @NonNull T emptyChoice) {
        super(emptyChoice);
    }

    @Override
    public final Action deserialize(final ByteBuf input) {
        input.skipBytes(ActionConstants.PADDING_IN_ACTION_HEADER);
        return deserializeHeader(input);
    }
}
