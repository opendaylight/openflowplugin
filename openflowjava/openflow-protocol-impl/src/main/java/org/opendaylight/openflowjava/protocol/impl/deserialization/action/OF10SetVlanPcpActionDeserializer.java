/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.pcp._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF10SetVlanPcpActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10SetVlanPcpActionDeserializer extends AbstractActionDeserializer<SetVlanPcpCase> {
    public OF10SetVlanPcpActionDeserializer() {
        super(new SetVlanPcpCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping
            .ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * Short.BYTES);
        SetVlanPcpCaseBuilder caseBuilder = new SetVlanPcpCaseBuilder();
        SetVlanPcpActionBuilder actionBuilder = new SetVlanPcpActionBuilder();
        actionBuilder.setVlanPcp(readUint8(input));
        caseBuilder.setSetVlanPcpAction(actionBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.PADDING_IN_SET_VLAN_PCP_ACTION);
        return builder.build();
    }
}
