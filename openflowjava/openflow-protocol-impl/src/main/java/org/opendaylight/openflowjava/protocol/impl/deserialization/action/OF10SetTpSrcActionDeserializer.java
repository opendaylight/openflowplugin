/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.src._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;

/**
 * OF10SetTpSrcActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetTpSrcActionDeserializer extends AbstractOF10SetTpActionDeserializer<SetTpSrcCase> {
    public OF10SetTpSrcActionDeserializer() {
        super(new SetTpSrcCaseBuilder().build());
    }

    @Override
    SetTpSrcCase createAction(final PortNumber port) {
        return new SetTpSrcCaseBuilder().setSetTpSrcAction(new SetTpSrcActionBuilder().setPort(port).build()).build();
    }
}
