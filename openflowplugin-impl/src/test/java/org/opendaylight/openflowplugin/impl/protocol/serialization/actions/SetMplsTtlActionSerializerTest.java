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
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;

public class SetMplsTtlActionSerializerTest extends AbstractActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final short mpls = (short) 10;

        final Action action = new SetMplsTtlActionCaseBuilder()
                .setSetMplsTtlAction(new SetMplsTtlActionBuilder()
                        .setMplsTtl(mpls)
                        .build())
                .build();

        assertAction(action, out -> {
            assertEquals(out.readUnsignedByte(), mpls);
            out.skipBytes(ActionConstants.SET_MPLS_TTL_PADDING);
        });
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetMplsTtlActionCase.class;
    }

    @Override
    protected int getType() {
        return ActionConstants.SET_MPLS_TTL_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }
}
