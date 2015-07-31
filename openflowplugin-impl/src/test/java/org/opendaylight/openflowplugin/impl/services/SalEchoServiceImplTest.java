package org.opendaylight.openflowplugin.impl.services;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.echo.service.rev150305.SendEchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

public class SalEchoServiceImplTest extends ServiceMocking {

    private static final Long DUMMY_XID_VALUE = 100L;
    private static final byte[] DUMMY_DATA = "DUMMY DATA".getBytes();
    SalEchoServiceImpl salEchoService;

    @Test
    public void testSendEcho() throws Exception {
        salEchoService = new SalEchoServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        SendEchoInput sendEchoInput = new SendEchoInputBuilder().build();
        salEchoService.sendEcho(sendEchoInput);
        verify(mockedRequestContextStack).createRequestContext();;
    }

    @Test
    public void testBuildRequest() throws Exception {
        salEchoService = new SalEchoServiceImpl(mockedRequestContextStack, mockedDeviceContext);
        SendEchoInput sendEchoInput = new SendEchoInputBuilder().setData(DUMMY_DATA).build();
        final OfHeader request = this.salEchoService.buildRequest(new Xid(DUMMY_XID_VALUE), sendEchoInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof EchoInput);
        final byte[] data = ((EchoInput) request).getData();
        assertArrayEquals(DUMMY_DATA, data);
    }
}