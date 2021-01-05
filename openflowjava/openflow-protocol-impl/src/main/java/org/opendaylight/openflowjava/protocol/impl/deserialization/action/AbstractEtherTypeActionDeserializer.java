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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yangtools.yang.common.netty.ByteBufUtils;

abstract class AbstractEtherTypeActionDeserializer<T extends ActionChoice> extends AbstractActionCaseDeserializer<T> {
    AbstractEtherTypeActionDeserializer(final @NonNull T emptyChoice) {
        super(emptyChoice);
    }

    @Override
    protected final T deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final EtherType etherType = new EtherType(ByteBufUtils.readUint16(input));
        input.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);

        return createAction(etherType);
    }

    abstract @NonNull T createAction(@NonNull EtherType etherType);
}
