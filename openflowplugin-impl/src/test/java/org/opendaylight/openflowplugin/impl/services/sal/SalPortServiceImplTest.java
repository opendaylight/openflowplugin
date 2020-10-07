/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPortBuilder;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class SalPortServiceImplTest extends ServiceMocking {

    private static final Uint32 DUMMY_XID = Uint32.valueOf(55L);
    private static final Uint32 DUMMY_PORT_NUMBER = Uint32.valueOf(66L);
    private static final String DUMMY_MAC_ADDRESS = "AA:BB:CC:DD:EE:FF";
    SalPortServiceImpl salPortService;

    @Override
    protected void setup() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        salPortService = new SalPortServiceImpl(mockedRequestContextStack, mockedDeviceContext, convertorManager);
    }

    @Test
    public void testUpdatePort() {
        salPortService.updatePort(dummyUpdatePortInput());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() {
        final OfHeader ofHeader = salPortService.buildRequest(new Xid(DUMMY_XID), dummyUpdatePortInput());
    }

    private static UpdatePortInput dummyUpdatePortInput() {
        org.opendaylight.yang.gen.v1.urn
                .opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder concretePortBuilder
                = new org.opendaylight.yang.gen.v1.urn
                .opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder();
        concretePortBuilder.setConfiguration(new PortConfig(true, true, true, true));
        concretePortBuilder.setAdvertisedFeatures(new PortFeatures(true, true, true, true, true, true, true, true,
                true, true, true, true, true, true, true, true));
        concretePortBuilder.setPortNumber(new PortNumberUni(DUMMY_PORT_NUMBER));
        concretePortBuilder.setHardwareAddress(new MacAddress(DUMMY_MAC_ADDRESS));
        concretePortBuilder.setPortModOrder(Uint32.ZERO);

        Port port = new PortBuilder().setPort(BindingMap.of(concretePortBuilder.build())).build();
        UpdatedPort updatePort = new UpdatedPortBuilder().setPort(port).build();
        return new UpdatePortInputBuilder().setUpdatedPort(updatePort).build();
    }
}
