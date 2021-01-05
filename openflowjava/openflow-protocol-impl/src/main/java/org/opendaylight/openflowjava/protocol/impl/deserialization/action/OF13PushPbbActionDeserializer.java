/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

/**
 * OF13PushPbbActionDeserializer.
 *
 * @author michal.polkorab
 */
public final class OF13PushPbbActionDeserializer extends AbstractEtherTypeActionDeserializer<PushPbbCase> {
    public OF13PushPbbActionDeserializer() {
        super(new PushPbbCaseBuilder().build());
    }

    @Override
    PushPbbCase createAction(final EtherType etherType) {
        return new PushPbbCaseBuilder()
            .setPushPbbAction(new PushPbbActionBuilder().setEthertype(etherType).build())
            .build();
    }
}
