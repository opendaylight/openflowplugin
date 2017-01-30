/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.OngoingStubbing;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerMultipartRequestCallback;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.AbstractMultipartRequestCallback}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartRequestCallbackTest {

    @Mock
    private DeviceContext deviceContext;
    @Mock
    private RequestContext<List<MultipartReply>> requestContext;
    @Mock
    private MessageSpy spy;
    @Mock
    private MultiMsgCollector<MultipartReply> multiMsgCollector;
    @Captor
    private ArgumentCaptor<RpcResult<List<MultipartReply>>> rpcResultCapt;

    private AbstractMultipartRequestCallback<MultipartReply> multipartRequestCallback;

    @Before
    public void setUp() throws Exception {
        Mockito.doNothing().when(requestContext).setResult(rpcResultCapt.capture());
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(spy);

        final OngoingStubbing<MultiMsgCollector<MultipartReply>> when =
            Mockito.when(deviceContext.getMultiMsgCollector(Matchers.any()));
        when.thenReturn(multiMsgCollector);
        multipartRequestCallback = new MultiLayerMultipartRequestCallback<>(requestContext, MultipartRequestInput.class, deviceContext, null);
    }

    /**
     * end collecting
     *
     * @throws Exception
     */
    @Test
    public void testOnSuccess1() throws Exception {
        multipartRequestCallback.onSuccess(null);
        Mockito.verify(multiMsgCollector).endCollecting(Matchers.<EventIdentifier>any());
    }

    /**
     * fail adding to collection
     *
     * @throws Exception
     */
    @Test
    public void testOnSuccess2() throws Exception {
        multipartRequestCallback.onSuccess(new EchoOutputBuilder().build());
        final RpcResult<List<MultipartReply>> rpcResult = rpcResultCapt.getValue();
        Assert.assertNotNull(rpcResult);
        Assert.assertFalse(rpcResult.isSuccessful());
    }

    /**
     * successfully added to collection
     *
     * @throws Exception
     */
    @Test
    public void testOnSuccess3() throws Exception {
        final MultipartReplyMessage replyMessage = new MultipartReplyMessageBuilder().build();
        multipartRequestCallback.onSuccess(replyMessage);
        Mockito.verify(multiMsgCollector).addMultipartMsg(Matchers.eq(replyMessage), Matchers.eq(false), Matchers.any());
    }
}
