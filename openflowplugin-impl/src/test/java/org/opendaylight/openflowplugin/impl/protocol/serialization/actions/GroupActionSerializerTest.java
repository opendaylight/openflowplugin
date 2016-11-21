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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;

public class GroupActionSerializerTest extends AbstractActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long group = 10L;

        final Action action = new GroupActionCaseBuilder()
                .setGroupAction(new GroupActionBuilder()
                        .setGroupId(group)
                        .build())
                .build();

        assertAction(action, out -> assertEquals(out.readUnsignedInt(), group));
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return GroupActionCase.class;
    }

    @Override
    protected int getType() {
        return ActionConstants.GROUP_CODE;
    }

    @Override
    protected int getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

}
