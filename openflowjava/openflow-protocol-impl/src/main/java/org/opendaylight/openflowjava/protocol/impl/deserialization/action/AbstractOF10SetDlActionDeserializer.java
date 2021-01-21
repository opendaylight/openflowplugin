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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

abstract class AbstractOF10SetDlActionDeserializer<T extends ActionChoice>
        extends AbstractSimpleActionCaseDeserializer<T> {
    AbstractOF10SetDlActionDeserializer(final @NonNull T emptyChoice, final @NonNull Uint8 version,
            final @NonNull Uint16 type) {
        super(emptyChoice, version, type);
    }

    @Override
    protected final T deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final MacAddress macAddress = ByteBufUtils.readIetfMacAddress(input);
        input.skipBytes(ActionConstants.PADDING_IN_DL_ADDRESS_ACTION);

        return createAction(macAddress);
    }

    abstract @NonNull T createAction(@NonNull MacAddress macAddress);
}
