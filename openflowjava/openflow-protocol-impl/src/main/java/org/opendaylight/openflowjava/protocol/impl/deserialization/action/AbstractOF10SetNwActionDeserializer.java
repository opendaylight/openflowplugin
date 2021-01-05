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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;

abstract class AbstractOF10SetNwActionDeserializer<T extends ActionChoice> extends AbstractActionCaseDeserializer<T> {
    AbstractOF10SetNwActionDeserializer(final @NonNull T emptyChoice) {
        super(emptyChoice);
    }

    @Override
    protected final T deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);

        return createAction(ByteBufUtils.readIetfIpv4Address(input));
    }

    abstract @NonNull T createAction(@NonNull Ipv4Address ipAddress);
}
