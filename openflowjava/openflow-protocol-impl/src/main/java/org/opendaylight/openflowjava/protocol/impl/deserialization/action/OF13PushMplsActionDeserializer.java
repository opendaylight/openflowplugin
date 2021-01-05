/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

/**
 * OF13PushMplsActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF13PushMplsActionDeserializer extends AbstractEtherTypeActionDeserializer<PushMplsCase> {
    public OF13PushMplsActionDeserializer() {
        super(new PushMplsCaseBuilder().build());
    }

    @Override
    PushMplsCase createAction(final EtherType etherType) {
        return new PushMplsCaseBuilder()
            .setPushMplsAction(new PushMplsActionBuilder().setEthertype(etherType).build())
            .build();
    }
}
