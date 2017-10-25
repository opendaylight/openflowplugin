/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.strip.vlan.action._case.StripVlanActionBuilder;

public class StripVlanActionSerializerTest extends AbstractSetFieldActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final Action action = new StripVlanActionCaseBuilder()
                .setStripVlanAction(new StripVlanActionBuilder()
                        .build())
                .build();

        assertAction(action, out -> {
            assertEquals(out.readUnsignedShort(), (1 << 12));
            byte[] mask = new byte[2];
            out.readBytes(mask);
            assertArrayEquals(mask, new byte[] { 16, 0 });
        });
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return StripVlanActionCase.class;
    }

}
