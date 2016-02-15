/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.util.concurrent.CheckedFuture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;


/**
 * test of {@link DeviceManagerImpl} - lightweight version, using basic ways (TDD)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private DeviceManagerImpl deviceManager;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private FeaturesReply features;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private RpcManager rpcManager;
    @Mock
    private WriteTransaction wTx;
    @Mock
    private MessageIntelligenceAgency messageIntelligenceAgency;
    @Mock
    private CheckedFuture<Void, TransactionCommitFailedException> checkedFuture;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Mockito.when(connectionContext.getConnectionAdapter()).thenReturn(connectionAdapter);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(features.getVersion()).thenReturn((short) 42);
        Mockito.when(dataBroker.newWriteOnlyTransaction()).thenReturn(wTx);
        Mockito.when(wTx.submit()).thenReturn(checkedFuture);
        Mockito.when(checkedFuture.get()).thenReturn((Void) null);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl#deviceConnected(org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext)}.
     * @throws Exception 
     */
    @Test
    @Ignore // FIXME : fix the test ASAP
    public void testDeviceConnected() throws Exception {
        deviceManager.deviceConnected(connectionContext);

        final ArgumentCaptor<MultipartRequestInput> mpInputCaptor = ArgumentCaptor.forClass(MultipartRequestInput.class);
        Mockito.verify(connectionAdapter).multipartRequest(mpInputCaptor.capture());

        Assert.assertTrue(mpInputCaptor.getAllValues().get(0).getMultipartRequestBody() instanceof MultipartRequestDescCase);
        //Assert.assertTrue(mpInputCaptor.getAllValues().get(1).getMultipartRequestBody() instanceof MultipartRequestGroupDescCase);
    }


    @Test
    public void testHookRequest() {

    }

}
