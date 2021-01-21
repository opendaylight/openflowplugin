/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

abstract class AbstractOF10SetTpActionDeserializer<T extends ActionChoice>
        extends AbstractSimpleActionCaseDeserializer<T> {
    AbstractOF10SetTpActionDeserializer(final @NonNull T emptyChoice, final @NonNull Uint8 version,
            final @NonNull Uint16 type) {
        super(emptyChoice, version, type);
    }

    @Override
    protected final T deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final PortNumber port = new PortNumber(readUint16(input).toUint32());
        input.skipBytes(ActionConstants.PADDING_IN_TP_PORT_ACTION);

        return createAction(port);
    }

    abstract @NonNull T createAction(@NonNull PortNumber port);
}
