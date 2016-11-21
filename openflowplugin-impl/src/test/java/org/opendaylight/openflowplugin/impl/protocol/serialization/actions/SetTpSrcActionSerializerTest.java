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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;

public class SetTpSrcActionSerializerTest extends AbstractSetFieldActionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final PortNumber port = new PortNumber(20);
        final short protocol = 6; // TCP

        final Action action = new SetTpSrcActionCaseBuilder()
                .setSetTpSrcAction(new SetTpSrcActionBuilder()
                        .setPort(port)
                        .setIpProtocol(protocol)
                        .build())
                .build();

        assertAction(action, out -> assertEquals(Integer.valueOf(out.readUnsignedShort()), port.getValue()));
    }

    @Override
    protected Class<? extends Action> getClazz() {
        return SetTpSrcActionCase.class;
    }

}
