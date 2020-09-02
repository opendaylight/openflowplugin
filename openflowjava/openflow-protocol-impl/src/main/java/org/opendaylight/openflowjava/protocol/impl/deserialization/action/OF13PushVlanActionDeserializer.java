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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yangtools.yang.common.netty.ByteBufUtils;

/**
 * OF13PushVlanActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13PushVlanActionDeserializer extends AbstractActionDeserializer {
    @Override
    public Action deserialize(ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final ActionBuilder builder = new ActionBuilder()
                .setActionChoice(new PushVlanCaseBuilder()
                    .setPushVlanAction(new PushVlanActionBuilder()
                        .setEthertype(new EtherType(ByteBufUtils.readUint16(input)))
                        .build())
                    .build());
        input.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);
        return builder.build();
    }

    @Override
    protected ActionChoice getType() {
        return new PushVlanCaseBuilder().build();
    }
}
