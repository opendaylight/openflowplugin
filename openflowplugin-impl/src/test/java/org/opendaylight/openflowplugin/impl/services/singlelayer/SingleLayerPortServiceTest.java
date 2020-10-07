/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortOutput;
import org.opendaylight.yangtools.yang.common.Uint32;

public class SingleLayerPortServiceTest extends ServiceMocking {
    private static final Uint32 PORT_ID = Uint32.valueOf(42);
    private SingleLayerPortService<UpdatePortOutput> service;

    @Override
    protected void setup() {
        service = new SingleLayerPortService<>(mockedRequestContextStack, mockedDeviceContext, UpdatePortOutput.class);
    }

    @Test
    public void buildRequest() {
        final Port input = new PortBuilder()
                .setPortNumber(new PortNumberUni(PORT_ID))
                .setPortModOrder(Uint32.ZERO)
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(PortMessage.class, ofHeader.implementedInterface());

        final PortMessage result = (PortMessage) ofHeader;

        assertEquals(PORT_ID, result.getPortNumber().getUint32());
    }
}
