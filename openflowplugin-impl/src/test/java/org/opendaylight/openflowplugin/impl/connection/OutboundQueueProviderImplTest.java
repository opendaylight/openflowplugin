/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.connection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import junit.framework.TestCase;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yangtools.yang.common.Uint32;

public class OutboundQueueProviderImplTest extends TestCase {

    private static final Long DUMMY_ENTRY_NUMBER = 44L;
    private static final Uint32 DUMMY_XID = Uint32.valueOf(55L);

    private final OutboundQueueProviderImpl outboundQueueProvider =
            new OutboundQueueProviderImpl(OFConstants.OFP_VERSION_1_3);

    @Test
    public void testReserveEntry() {

        outboundQueueProvider.onConnectionQueueChanged(null);
        Long returnValue = outboundQueueProvider.reserveEntry();
        assertEquals(null, returnValue);

        OutboundQueue mockedQueue = mock(OutboundQueue.class);
        when(mockedQueue.reserveEntry()).thenReturn(DUMMY_ENTRY_NUMBER);
        outboundQueueProvider.onConnectionQueueChanged(mockedQueue);
        returnValue = outboundQueueProvider.reserveEntry();
        assertEquals(DUMMY_ENTRY_NUMBER, returnValue);
    }

    @Test
    public void testCreateBarrierRequest() {
        final BarrierInput barrierRequest = outboundQueueProvider.createBarrierRequest(DUMMY_XID.toJava());
        assertNotNull(barrierRequest);
        assertEquals(OFConstants.OFP_VERSION_1_3, barrierRequest.getVersion().toJava());
        assertEquals(DUMMY_XID, barrierRequest.getXid());
    }
}
