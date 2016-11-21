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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcActionBuilder;

public class SetDlSrcActionSerializerTest extends AbstractSetFieldActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final MacAddress address = new MacAddress("00:01:02:03:04:05");

        final Action action = new SetDlSrcActionCaseBuilder()
                .setSetDlSrcAction(new SetDlSrcActionBuilder()
                        .setAddress(address)
                        .build())
                .build();

        assertAction(action, out -> {
            byte[] addressBytes = new byte[6];
            out.readBytes(addressBytes);
            assertEquals(new MacAddress(ByteBufUtils.macAddressToString(addressBytes)).getValue(), address.getValue());
        });
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetDlSrcActionCase.class;
    }

}
