/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.src._case.SetNwSrcActionBuilder;

/**
 * OF10SetNwSrcActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF10SetNwSrcActionDeserializer extends AbstractOF10SetNwActionDeserializer<SetNwSrcCase> {
    public OF10SetNwSrcActionDeserializer() {
        super(new SetNwSrcCaseBuilder().build());
    }

    @Override
    SetNwSrcCase createAction(final Ipv4Address ipAddress) {
        return new SetNwSrcCaseBuilder()
            .setSetNwSrcAction(new SetNwSrcActionBuilder().setIpAddress(ipAddress).build())
            .build();
    }
}
