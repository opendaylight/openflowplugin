/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.src._case.SetDlSrcActionBuilder;

/**
 * OF10SetDlSrcActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetDlSrcActionDeserializer extends AbstractOF10SetDlActionDeserializer<SetDlSrcCase> {
    public OF10SetDlSrcActionDeserializer() {
        super(new SetDlSrcCaseBuilder().build());
    }

    @Override
    SetDlSrcCase createAction(final MacAddress macAddress) {
        return new SetDlSrcCaseBuilder()
            .setSetDlSrcAction(new SetDlSrcActionBuilder().setDlSrcAddress(macAddress).build())
            .build();
    }
}
