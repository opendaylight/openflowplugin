package org.opendaylight.openflowplugin.impl.services;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class EchoServiceTest extends ServiceMocking {

    private static final Long DUMMY_XID_VALUE = 100L;
    private static final byte[] DUMMY_DATA = "DUMMY DATA".getBytes();
    EchoService echoService;

    @Override
    public void setup() {
        echoService = new EchoService(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testSendEcho() throws Exception {
        EchoInputBuilder sendEchoInput = new EchoInputBuilder();
        echoService.handleServiceCall(sendEchoInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() throws Exception {
        EchoInputBuilder sendEchoInput = new EchoInputBuilder().setData(DUMMY_DATA);
        final OfHeader request = this.echoService.buildRequest(new Xid(DUMMY_XID_VALUE), sendEchoInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof EchoInput);
        final byte[] data = ((EchoInput) request).getData();
        assertArrayEquals(DUMMY_DATA, data);
        assertEquals(OFConstants.OFP_VERSION_1_3, request.getVersion().shortValue());
    }
}