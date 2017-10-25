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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;

public class SetQueueActionSerializerTest extends AbstractActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long queue = 10;

        final Action action = new SetQueueActionCaseBuilder()
                .setSetQueueAction(new SetQueueActionBuilder()
                        .setQueueId(queue)
                        .build())
                .build();

        assertAction(action, out -> assertEquals(out.readUnsignedInt(), queue));
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetQueueActionCase.class;
    }

    @Override
    protected int getType() {
        return ActionConstants.SET_QUEUE_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

}
