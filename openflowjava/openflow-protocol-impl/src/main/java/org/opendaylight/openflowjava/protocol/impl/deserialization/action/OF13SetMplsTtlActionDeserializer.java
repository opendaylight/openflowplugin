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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * OF13SetMplsTtlActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13SetMplsTtlActionDeserializer extends AbstractActionDeserializer<SetMplsTtlCase> {
    public OF13SetMplsTtlActionDeserializer() {
        super(new SetMplsTtlCaseBuilder().build());
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        final var ttl = readUint8(input);
        input.skipBytes(ActionConstants.SET_MPLS_TTL_PADDING);

        return new ActionBuilder()
            .setActionChoice(new SetMplsTtlCaseBuilder()
                .setSetMplsTtlAction(new SetMplsTtlActionBuilder().setMplsTtl(ttl).build())
                .build())
            .build();
    }
}
