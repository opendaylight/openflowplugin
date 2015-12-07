package org.opendaylight.openflowplugin.impl.services;

import com.google.common.collect.Lists;
import io.netty.util.HashedWheelTimer;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.PortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.UpdatePortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.port.update.UpdatedPortBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SalPortServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_XID = 55L;
    private static final Long DUMMY_PORT_NUMBER = 66L;
    private static final String DUMMY_MAC_ADDRESS = "AA:BB:CC:DD:EE:FF";
    SalPortServiceImpl salPortService;

    @Override
    public void initialization() {
        super.initialization();
        salPortService = new SalPortServiceImpl(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testUpdatePort() throws Exception {
        salPortService.updatePort(dummyUpdatePortInput());
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() {
        final OfHeader ofHeader = salPortService.buildRequest(new Xid(DUMMY_XID), dummyUpdatePortInput());
    }

    private UpdatePortInput dummyUpdatePortInput(){
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder concretePortBuilder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.PortBuilder();
        concretePortBuilder.setConfiguration(new PortConfig(true, true, true, true));
        concretePortBuilder.setAdvertisedFeatures(new PortFeatures(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true));
        concretePortBuilder.setPortNumber(new PortNumberUni(DUMMY_PORT_NUMBER));
        concretePortBuilder.setHardwareAddress(new MacAddress(DUMMY_MAC_ADDRESS));

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port> ports
                = Lists.newArrayList(concretePortBuilder.build());
        Port port = new PortBuilder().setPort(ports).build();
        UpdatedPort updatePort = new UpdatedPortBuilder().setPort(port).build();
        return new UpdatePortInputBuilder().setUpdatedPort(updatePort).build();
    }
}