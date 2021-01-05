/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.dst._case.SetDlDstActionBuilder;

/**
 * OF10SetDlDstActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetDlDstActionDeserializer extends AbstractOF10SetDlActionDeserializer<SetDlDstCase> {
    public OF10SetDlDstActionDeserializer() {
        super(new SetDlDstCaseBuilder().build());
    }

    @Override
    SetDlDstCase createAction(final MacAddress macAddress) {
        return new SetDlDstCaseBuilder()
            .setSetDlDstAction(new SetDlDstActionBuilder().setDlDstAddress(macAddress).build())
            .build();
    }
}
