/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.sal.common.util.Rpcs;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev131103.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * simple NPE smoke test
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelDrivenSwitchImplTest {

    private ModelDrivenSwitchImpl mdSwitchOF10;
    private ModelDrivenSwitchImpl mdSwitchOF13;

    @Mock
    private SessionContext context;
    @Mock
    private ConnectionConductor conductor;
    @Mock
    private IMessageDispatchService messageDispatchService;
    @Mock
    private GetFeaturesOutput features;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        Mockito.when(context.getPrimaryConductor()).thenReturn(conductor);
        Mockito.when(context.getMessageDispatchService()).thenReturn(messageDispatchService);
        Mockito.when(conductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_0)
                .thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(context.getFeatures()).thenReturn(features);
        Mockito.when(features.getDatapathId()).thenReturn(BigInteger.valueOf(1));
        
        OFSessionUtil.getSessionManager().setRpcPool(MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10)));

        mdSwitchOF10 = new ModelDrivenSwitchImpl(null, null, context);
        mdSwitchOF13 = new ModelDrivenSwitchImpl(null, null, context);
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#addFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput)}
     * .
     */
    @Test
    public void testAddFlow() {
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        updateFlowOutput.setTransactionId(new TransactionId(new BigInteger("42")));
        Set<RpcError> errorSet = Collections.emptySet();
        RpcResult<UpdateFlowOutput> result = Rpcs.getRpcResult(true, updateFlowOutput.build(), errorSet);
        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        AddFlowInputBuilder input = new AddFlowInputBuilder();
//        input.setMatch(new MatchBuilder().build());

        mdSwitchOF10.addFlow(input.build());
        mdSwitchOF13.addFlow(input.build());
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#removeFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput)}
     * .
     */
    @Test
    public void testRemoveFlow() {
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        updateFlowOutput.setTransactionId(new TransactionId(new BigInteger("42")));
        Set<RpcError> errorSet = Collections.emptySet();
        RpcResult<UpdateFlowOutput> result = Rpcs.getRpcResult(true, updateFlowOutput.build(), errorSet);
        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        RemoveFlowInputBuilder input = new RemoveFlowInputBuilder();
        input.setMatch(new MatchBuilder().build());

        mdSwitchOF10.removeFlow(input.build());
        mdSwitchOF13.removeFlow(input.build());
    }

    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#updateFlow(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput)}
     * .
     */
    @Test
    public void testUpdateFlow() {
        UpdateFlowOutputBuilder updateFlowOutput = new UpdateFlowOutputBuilder();
        updateFlowOutput.setTransactionId(new TransactionId(new BigInteger("42")));
        Set<RpcError> errorSet = Collections.emptySet();
        RpcResult<UpdateFlowOutput> result = Rpcs.getRpcResult(true, updateFlowOutput.build(), errorSet);
        Mockito.when(
                messageDispatchService.flowMod(Matchers.any(FlowModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdateFlowInputBuilder input = new UpdateFlowInputBuilder();
        UpdatedFlowBuilder updatedFlow = new UpdatedFlowBuilder();
        updatedFlow.setMatch(new MatchBuilder().build());
        input.setUpdatedFlow(updatedFlow.build());

        mdSwitchOF10.updateFlow(input.build());
        mdSwitchOF13.updateFlow(input.build());
    }
    
    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * addGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.
     * AddGroupInput)}
     * .
     */
    @Test
    public void testAddGroup() {
        UpdateGroupOutputBuilder updateGroupOutput = new UpdateGroupOutputBuilder();
        updateGroupOutput.setTransactionId(new TransactionId(new BigInteger("42")));
        Set<RpcError> errorSet = Collections.emptySet();
        RpcResult<UpdateGroupOutput> result = Rpcs.getRpcResult(true, updateGroupOutput.build(), errorSet);
        Mockito.when(
                messageDispatchService.groupMod(Matchers.any(GroupModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        AddGroupInputBuilder input = new AddGroupInputBuilder();
//        input.setMatch(new MatchBuilder().build());

        mdSwitchOF10.addGroup(input.build());
        mdSwitchOF13.addGroup(input.build());
    }
    
    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.sal.ModelDrivenSwitchImpl#
     * updateGroup(org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.
     * UpdateGroupInput)}
     * .
     */
    @Test
    public void testUpdateGroup() {
        UpdateGroupOutputBuilder updateGroupOutput = new UpdateGroupOutputBuilder();
        updateGroupOutput.setTransactionId(new TransactionId(new BigInteger("42")));
        Set<RpcError> errorSet = Collections.emptySet();
        RpcResult<UpdateGroupOutput> result = Rpcs.getRpcResult(true, updateGroupOutput.build(), errorSet);
        Mockito.when(
                messageDispatchService.groupMod(Matchers.any(GroupModInput.class),
                        Matchers.any(SwitchConnectionDistinguisher.class))).thenReturn(Futures.immediateFuture(result));

        UpdateGroupInputBuilder input = new UpdateGroupInputBuilder();
//        input.setMatch(new MatchBuilder().build());

        mdSwitchOF10.updateGroup(input.build());
        mdSwitchOF13.updateGroup(input.build());
    }
}
