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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.dst._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF10SetNwDstActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10SetNwDstActionDeserializer extends AbstractActionDeserializer {

    @Override
    public Action deserialize(final ByteBuf input) {
        final ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * Short.BYTES);
        SetNwDstCaseBuilder caseBuilder = new SetNwDstCaseBuilder();
        SetNwDstActionBuilder actionBuilder = new SetNwDstActionBuilder();
        actionBuilder.setIpAddress(ByteBufUtils.readIetfIpv4Address(input));
        caseBuilder.setSetNwDstAction(actionBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        return builder.build();
    }

    @Override
    protected ActionChoice getType() {
        return new SetNwDstCaseBuilder().build();
    }

}
