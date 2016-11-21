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
import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;

public class SetNwTosActionSerializerTest extends AbstractSetFieldActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int tos = 10;

        final Action action = new SetNwTosActionCaseBuilder()
                .setSetNwTosAction(new SetNwTosActionBuilder()
                        .setTos(10)
                        .build())
                .build();

        assertAction(action, out -> assertEquals(out.readUnsignedByte(), (short) ActionUtil.tosToDscp((short) tos)));
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetNwTosActionCase.class;
    }

}
