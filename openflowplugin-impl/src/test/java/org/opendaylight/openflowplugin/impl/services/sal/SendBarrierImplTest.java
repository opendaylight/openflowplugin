/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link SendBarrierImpl}.
 */
public class SendBarrierImplTest extends ServiceMocking {
    private static final Uint32 DUMMY_XID_VALUE = Uint32.valueOf(100);

    private SendBarrierImpl sendBarrier;

    @Override
    protected void setup() {
        sendBarrier = new SendBarrierImpl(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void testBuildRequest() {
        final var request = sendBarrier.buildRequest(new Xid(DUMMY_XID_VALUE), buildSendBarrierInput());
        assertEquals(DUMMY_XID_VALUE, request.getXid());
        assertTrue(request instanceof BarrierInput);
    }

    @Test
    public void testSendBarrier() {
        sendBarrier.invoke(buildSendBarrierInput());
        verify(mockedRequestContextStack).createRequestContext();
    }

    private @NonNull SendBarrierInput buildSendBarrierInput() {
        return new SendBarrierInputBuilder()
            .setNode(new NodeRef(mockedDeviceInfo.getNodeInstanceIdentifier().toIdentifier()))
            .build();
    }
}
