/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;

/**
 * OF13GroupActionDeserializer.
 *
 * @author michal.polkorab
 */
public class OF13GroupActionDeserializer extends AbstractActionCaseDeserializer<GroupCase> {
    public OF13GroupActionDeserializer() {
        super(new GroupCaseBuilder().build());
    }

    @Override
    protected GroupCase deserializeAction(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return new GroupCaseBuilder()
            .setGroupAction(new GroupActionBuilder().setGroupId(readUint32(input)).build())
            .build();
    }
}
