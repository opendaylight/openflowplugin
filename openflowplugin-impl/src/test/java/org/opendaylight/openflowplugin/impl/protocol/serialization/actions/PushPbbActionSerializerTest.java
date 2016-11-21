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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;

public class PushPbbActionSerializerTest extends AbstractActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final int ethType = 10;

        final Action action = new PushPbbActionCaseBuilder()
                .setPushPbbAction(new PushPbbActionBuilder()
                        .setEthernetType(ethType)
                        .build())
                .build();

        assertAction(action, out -> {
            assertEquals(out.readUnsignedShort(), ethType);
            out.skipBytes(ActionConstants.ETHERTYPE_ACTION_PADDING);
        });
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return PushPbbActionCase.class;
    }

    @Override
    protected int getType() {
        return ActionConstants.PUSH_PBB_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

}
