/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

/**
 * @author michal.polkorab
 *
 */
public class OF13PushPbbActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(ByteBuf input) {
        ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        PushPbbCaseBuilder caseBuilder = new PushPbbCaseBuilder();
        PushPbbActionBuilder pbbBuilder = new PushPbbActionBuilder();
        pbbBuilder.setEthertype(new EtherType(input.readUnsignedShort()));
        caseBuilder.setPushPbbAction(pbbBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);
        return builder.build();
    }

    @Override
    protected ActionChoice getType() {
        return new PushPbbCaseBuilder().build();
    }

}
