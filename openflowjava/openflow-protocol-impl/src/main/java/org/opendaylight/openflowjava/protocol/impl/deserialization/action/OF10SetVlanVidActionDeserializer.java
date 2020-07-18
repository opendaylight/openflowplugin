/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.vid._case.SetVlanVidActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF10SetVlanVidActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10SetVlanVidActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf input) {
        input.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        final ActionBuilder builder = new ActionBuilder()
                .setActionChoice(new SetVlanVidCaseBuilder()
                    .setSetVlanVidAction(new SetVlanVidActionBuilder().setVlanVid(readUint16(input)).build())
                    .build());
        input.skipBytes(ActionConstants.PADDING_IN_SET_VLAN_VID_ACTION);
        return builder.build();
    }

    @Override
    protected ActionChoice getType() {
        return new SetVlanVidCaseBuilder().build();
    }

}
