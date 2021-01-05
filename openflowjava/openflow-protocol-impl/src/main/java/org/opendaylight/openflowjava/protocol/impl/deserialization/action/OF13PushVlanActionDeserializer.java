/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

/**
 * OF13PushVlanActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF13PushVlanActionDeserializer extends AbstractEtherTypeActionDeserializer<PushVlanCase> {
    public OF13PushVlanActionDeserializer() {
        super(new PushVlanCaseBuilder().build());
    }

    @Override
    PushVlanCase createAction(final EtherType etherType) {
        return new PushVlanCaseBuilder()
            .setPushVlanAction(new PushVlanActionBuilder().setEthertype(etherType).build())
            .build();
    }
}
