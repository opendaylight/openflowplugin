/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;

public class SetNwDstActionSerializerTest extends AbstractSetFieldActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final Ipv4 address = new Ipv4Builder()
                .setIpv4Address(new Ipv4Prefix("192.168.76.2/32"))
                .build();

        final Action action = new SetNwDstActionCaseBuilder()
                .setSetNwDstAction(new SetNwDstActionBuilder()
                        .setAddress(address)
                        .build())
                .build();

        assertAction(action, out -> {
            byte[] addressBytes = new byte[4];
            out.readBytes(addressBytes);
            assertArrayEquals(addressBytes, new byte[] { (byte) 192, (byte) 168, 76, 2 });
        });
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetNwDstActionCase.class;
    }

}
