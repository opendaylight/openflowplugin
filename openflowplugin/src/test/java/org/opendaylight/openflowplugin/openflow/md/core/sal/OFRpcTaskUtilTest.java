/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.NotificationComposer;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerInitialization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OFRpcTaskUtilTest extends ConvertorManagerInitialization {

    @Mock
    private OFRpcTaskContext taskContext;
    @Mock
    private SwitchConnectionDistinguisher connectionDistinguisher;
    @Mock
    private SessionContext sessionContext;
    @Mock
    private IMessageDispatchService messageDispatchService;
    @Mock
    private GetFeaturesOutput featuresOutput;
    @Mock
    private ListenableFuture<RpcResult<BarrierOutput>> resultListenableFuture;
    @Mock
    private ListenableFuture<RpcResult<UpdateFlowOutput>> updateFlowRpcResultListenableFuture;
    @Mock
    private NotificationProviderService notificationProviderService;
    @Mock
    private NotificationComposer<?> notificationComposer;
    @Mock
    ListeningExecutorService executorService;


    @Override
    public void setUp() {
        when(taskContext.getSession()).thenReturn(sessionContext);
        when(taskContext.getMessageService()).thenReturn(messageDispatchService);
        when(sessionContext.getNextXid()).thenReturn(new Long(10));
        when(sessionContext.getFeatures()).thenReturn(featuresOutput);
        when(featuresOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(messageDispatchService.barrier(Mockito.any(BarrierInput.class), Mockito.any(SwitchConnectionDistinguisher.class))).thenReturn(resultListenableFuture);
        when(taskContext.getRpcPool()).thenReturn(executorService);
        when(executorService.submit(Mockito.<Callable<RpcResult<UpdateFlowOutput>>> any())).thenReturn(updateFlowRpcResultListenableFuture);
    }


    @Test
    public void testManageBarrier() throws Exception {
        final Collection<RpcError> rpcErrors = OFRpcTaskUtil.manageBarrier(taskContext, true, connectionDistinguisher);
        assertNotNull(rpcErrors);
    }

    @Test
    public void testHookFutureNotification() throws Exception {
        final AddFlowInputBuilder flowInputBuilder = new AddFlowInputBuilder();
        final OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> addFlowInputRpcResultOFRpcTask = OFRpcTaskFactory.createAddFlowTask(taskContext, flowInputBuilder.build(), connectionDistinguisher, getConvertorManager());
        OFRpcTaskUtil.hookFutureNotification(addFlowInputRpcResultOFRpcTask, updateFlowRpcResultListenableFuture, notificationProviderService, notificationComposer);
    }

    @Test
    public void testChainFutureBarrier() throws Exception {
        final AddFlowInputBuilder flowInputBuilder = new AddFlowInputBuilder();
        flowInputBuilder.setBarrier(true);
        final OFRpcTask<AddFlowInput, RpcResult<UpdateFlowOutput>> addFlowInputRpcResultOFRpcTask = OFRpcTaskFactory.createAddFlowTask(taskContext, flowInputBuilder.build(), connectionDistinguisher, getConvertorManager());
        OFRpcTaskUtil.chainFutureBarrier(addFlowInputRpcResultOFRpcTask, updateFlowRpcResultListenableFuture);
    }
}