/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
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
import org.opendaylight.yangtools.yang.common.Uint32;

public class SendEchoImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_XID_VALUE = Uint32.valueOf(100);
    private static final byte[] DUMMY_DATA = "DUMMY DATA".getBytes();

    private SendEchoImpl sendEcho;

    @Override
    public void setup() {
        sendEcho = new SendEchoImpl(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testSendEcho() {
        EchoInputBuilder sendEchoInput = new EchoInputBuilder();
        sendEcho.handleServiceCall(sendEchoInput);
        verify(mockedRequestContextStack).createRequestContext();
    }

    @Test
    public void testBuildRequest() {
        EchoInputBuilder sendEchoInput = new EchoInputBuilder().setData(DUMMY_DATA);
        final OfHeader request = sendEcho.buildRequest(new Xid(DUMMY_XID_VALUE), sendEchoInput);
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof EchoInput);
        final byte[] data = ((EchoInput) request).getData();
        assertArrayEquals(DUMMY_DATA, data);
        assertEquals(OFConstants.OFP_VERSION_1_3, request.getVersion());
    }
}
