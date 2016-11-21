/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;

public class SetVlanIdActionSerializerTest extends AbstractSetFieldActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int vlan = 1;

        final Action action = new SetVlanIdActionCaseBuilder()
                .setSetVlanIdAction(new SetVlanIdActionBuilder()
                        .setVlanId(new VlanId(vlan))
                        .build())
                .build();

        assertAction(action, out -> assertEquals(out.readShort(), vlan | (1 << 12)));
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetVlanIdActionCase.class;
    }

}
