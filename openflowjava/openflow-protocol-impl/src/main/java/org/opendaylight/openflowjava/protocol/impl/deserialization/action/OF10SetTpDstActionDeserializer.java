/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.dst._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;

/**
 * OF10SetTpDstActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetTpDstActionDeserializer extends AbstractOF10SetTpActionDeserializer<SetTpDstCase> {
    public OF10SetTpDstActionDeserializer() {
        super(new SetTpDstCaseBuilder().build());
    }

    @Override
    SetTpDstCase createAction(final PortNumber port) {
        return new SetTpDstCaseBuilder().setSetTpDstAction(new SetTpDstActionBuilder().setPort(port).build()).build();
    }
}
