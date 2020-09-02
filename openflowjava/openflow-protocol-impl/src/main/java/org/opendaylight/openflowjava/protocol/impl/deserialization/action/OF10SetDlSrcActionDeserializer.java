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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.src._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF10SetDlSrcActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF10SetDlSrcActionDeserializer extends AbstractActionDeserializer<SetDlSrcCase> {
    public OF10SetDlSrcActionDeserializer() {
        super(new SetDlSrcCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        final ActionBuilder builder = new ActionBuilder();
        input.skipBytes(2 * Short.BYTES);
        SetDlSrcCaseBuilder caseBuilder = new SetDlSrcCaseBuilder();
        SetDlSrcActionBuilder actionBuilder = new SetDlSrcActionBuilder();
        actionBuilder.setDlSrcAddress(ByteBufUtils.readIetfMacAddress(input));
        caseBuilder.setSetDlSrcAction(actionBuilder.build());
        builder.setActionChoice(caseBuilder.build());
        input.skipBytes(ActionConstants.PADDING_IN_DL_ADDRESS_ACTION);
        return builder.build();
    }
}
