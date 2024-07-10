/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPortBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePortImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_XID = Uint32.valueOf(55L);
    private static final Uint32 DUMMY_PORT_NUMBER = Uint32.valueOf(66L);
    private static final String DUMMY_MAC_ADDRESS = "AA:BB:CC:DD:EE:FF";

    private UpdatePortImpl updatePort;

    @Override
    protected void setup() {
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();
        updatePort = new UpdatePortImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testUpdatePort() {
        updatePort.invoke(dummyUpdatePortInput());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() {
        assertNotNull(updatePort.buildRequest(new Xid(DUMMY_XID), dummyUpdatePortInput()));
    }

    private static UpdatePortInput dummyUpdatePortInput() {
        return new UpdatePortInputBuilder()
            .setUpdatedPort(new UpdatedPortBuilder()
                .setPort(new PortBuilder()
                    .setPort(BindingMap.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925
                        .port.mod.port.PortBuilder()
                        .setConfiguration(new PortConfig(true, true, true, true))
                        .setAdvertisedFeatures(new PortFeatures(true, true, true, true, true, true, true, true, true,
                            true, true, true, true, true, true, true))
                        .setPortNumber(new PortNumberUni(DUMMY_PORT_NUMBER))
                        .setHardwareAddress(new MacAddress(DUMMY_MAC_ADDRESS))
                        .setPortModOrder(Uint32.ZERO)
                        .build()))
                    .build())
                .build())
            .build();
    }
}
