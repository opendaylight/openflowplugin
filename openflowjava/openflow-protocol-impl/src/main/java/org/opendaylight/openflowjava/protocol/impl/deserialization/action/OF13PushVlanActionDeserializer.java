/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yangtools.yang.common.netty.ByteBufUtils;

/**
 * OF13PushVlanActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF13PushVlanActionDeserializer extends AbstractActionCaseDeserializer<PushVlanCase> {
    public OF13PushVlanActionDeserializer() {
        super(new PushVlanCaseBuilder().build());
    }

    @Override
    protected PushVlanCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var etherType = ByteBufUtils.readUint16(input);
        input.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);

        return new PushVlanCaseBuilder()
            .setPushVlanAction(new PushVlanActionBuilder().setEthertype(new EtherType(etherType)).build())
            .build();
    }
}
