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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

abstract class AbstractActionCaseDeserializer<T extends ActionChoice> extends AbstractActionDeserializer<T> {
    AbstractActionCaseDeserializer(final @NonNull T emptyChoice) {
        super(emptyChoice);
    }

    @Override
    public final Action deserialize(final ByteBuf input) {
        return new ActionBuilder().setActionChoice(deserializeAction(input)).build();
    }

    protected abstract @NonNull T deserializeAction(@NonNull ByteBuf input);
}
