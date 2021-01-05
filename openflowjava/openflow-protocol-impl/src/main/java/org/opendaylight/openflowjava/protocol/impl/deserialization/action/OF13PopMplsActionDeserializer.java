/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

/**
 * OF13PopMplsActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF13PopMplsActionDeserializer extends AbstractEtherTypeActionDeserializer<PopMplsCase> {
    public OF13PopMplsActionDeserializer() {
        super(new PopMplsCaseBuilder().build());
    }

    @Override
    PopMplsCase createAction(final EtherType etherType) {
        return new PopMplsCaseBuilder()
            .setPopMplsAction(new PopMplsActionBuilder().setEthertype(etherType).build())
            .build();
    }
}
