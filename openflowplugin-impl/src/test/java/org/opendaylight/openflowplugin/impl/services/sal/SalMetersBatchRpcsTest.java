/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.sal;

import static org.mockito.ArgumentMatchers.any;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMetersBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalMetersBatchRpcs}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalMetersBatchRpcsTest {

    public static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    public static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    public static final NodeRef NODE_REF =
            new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY));

    @Mock
    private SalMeterRpcs salMeterService;
    @Mock
    private FlowCapableTransactionRpc transactionService;
    @Mock
    private UpdateMeter updateMeter;
    @Mock
    private AddMeter addMeter;

    @Mock
    private RemoveMeter removeMeter;
    @Mock
    private SendBarrier sendBarrier;
    @Mock
    private UpdateMetersBatch updateMetersBatch;
    @Captor
    private ArgumentCaptor<RemoveMeterInput> removeMeterInputCpt;
    @Captor
    private ArgumentCaptor<UpdateMeterInput> updateMeterInputCpt;
    @Captor
    private ArgumentCaptor<AddMeterInput> addMeterInputCpt;

    private SalMetersBatchRpcs salMetersBatchService;

    @Before
    public void setUp() {
        Mockito.when(transactionService.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(SendBarrier.class, sendBarrier));
        Mockito.when(sendBarrier.invoke(any()))
            .thenReturn(RpcResultBuilder.success(new SendBarrierOutputBuilder().build()).buildFuture());

        salMetersBatchService = new SalMetersBatchRpcs(salMeterService, transactionService);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(updateMeter, addMeter, removeMeter, sendBarrier);
    }

    @Test
    public void testUpdateMetersBatch_success() throws Exception {
        Mockito.when(salMeterService.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(UpdateMeter.class, updateMeter));
        Mockito.when(updateMeter.invoke(any())).thenReturn(
            RpcResultBuilder.success(new UpdateMeterOutputBuilder().build()).buildFuture());

        final UpdateMetersBatchInput input = new UpdateMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateMeters(Lists.newArrayList(
                        createEmptyBatchUpdateMeter(42L),
                        createEmptyBatchUpdateMeter(44L)))
                .build();

        final Future<RpcResult<UpdateMetersBatchOutput>> resultFuture = salMetersBatchService.updateMetersBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(updateMeter, sendBarrier);
        inOrder.verify(updateMeter, Mockito.times(2)).invoke(updateMeterInputCpt.capture());
        final List<UpdateMeterInput> allValues = updateMeterInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getOriginalMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(43, allValues.get(0).getUpdatedMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(44, allValues.get(1).getOriginalMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(45, allValues.get(1).getUpdatedMeter().getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testUpdateMetersBatch_failure() throws Exception {
        Mockito.when(salMeterService.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(UpdateMeter.class, updateMeter));
        Mockito.when(updateMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.<UpdateMeterOutput>failed()
                        .withError(ErrorType.APPLICATION, "ur-groupUpdateError")
                        .buildFuture());

        final UpdateMetersBatchInput input = new UpdateMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateMeters(Lists.newArrayList(
                        createEmptyBatchUpdateMeter(42L),
                        createEmptyBatchUpdateMeter(44L)))
                .build();

        final Future<RpcResult<UpdateMetersBatchOutput>> resultFuture = salMetersBatchService.updateMetersBatch(input);
        Iterator<BatchFailedMetersOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedMetersOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedMetersOutput().size());
        Assert.assertEquals(43L, iterator.next().getMeterId().getValue().longValue());
        Assert.assertEquals(45L,iterator.next().getMeterId().getValue().longValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());


        final InOrder inOrder = Mockito.inOrder(updateMeter, sendBarrier);
        inOrder.verify(updateMeter, Mockito.times(2)).invoke(updateMeterInputCpt.capture());
        final List<UpdateMeterInput> allValues = updateMeterInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42, allValues.get(0).getOriginalMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(43, allValues.get(0).getUpdatedMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(44, allValues.get(1).getOriginalMeter().getMeterId().getValue().longValue());
        Assert.assertEquals(45, allValues.get(1).getUpdatedMeter().getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }


    @Test
    public void testAddMetersBatch_success() throws Exception {
        Mockito.when(salMeterService.getRpcClassToInstanceMap())
            .thenReturn(ImmutableClassToInstanceMap.of(AddMeter.class, addMeter));
        Mockito.when(addMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        final AddMetersBatchInput input = new AddMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddMeters(BindingMap.ordered(
                        createEmptyBatchAddMeter(42L),
                        createEmptyBatchAddMeter(43L)))
                .build();

        final Future<RpcResult<AddMetersBatchOutput>> resultFuture = salMetersBatchService.addMetersBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(addMeter, sendBarrier);
        inOrder.verify(addMeter, Mockito.times(2)).invoke(addMeterInputCpt.capture());
        final List<AddMeterInput> allValues = addMeterInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testAddMetersBatch_failure() throws Exception {
        Mockito.when(salMeterService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(AddMeter.class, addMeter));
        Mockito.when(addMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.<AddMeterOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupAddError")
                        .buildFuture());

        final AddMetersBatchInput input = new AddMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddMeters(BindingMap.ordered(
                        createEmptyBatchAddMeter(42L),
                        createEmptyBatchAddMeter(43L)))
                .build();

        final Future<RpcResult<AddMetersBatchOutput>> resultFuture = salMetersBatchService.addMetersBatch(input);
        Iterator<BatchFailedMetersOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedMetersOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedMetersOutput().size());
        Assert.assertEquals(42L, iterator.next().getMeterId().getValue().longValue());
        Assert.assertEquals(43L,iterator.next().getMeterId().getValue().longValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());


        final InOrder inOrder = Mockito.inOrder(addMeter, sendBarrier);
        inOrder.verify(addMeter, Mockito.times(2)).invoke(addMeterInputCpt.capture());
        final List<AddMeterInput> allValues = addMeterInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testRemoveMetersBatch_success() throws Exception {
        Mockito.when(salMeterService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(RemoveMeter.class, removeMeter));
        Mockito.when(removeMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new RemoveMeterOutputBuilder().build()).buildFuture());

        final RemoveMetersBatchInput input = new RemoveMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveMeters(BindingMap.ordered(
                        createEmptyBatchRemoveMeter(42L),
                        createEmptyBatchRemoveMeter(43L)))
                .build();

        final Future<RpcResult<RemoveMetersBatchOutput>> resultFuture = salMetersBatchService.removeMetersBatch(input);

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertTrue(resultFuture.get().isSuccessful());

        final InOrder inOrder = Mockito.inOrder(removeMeter, sendBarrier);

        inOrder.verify(removeMeter, Mockito.times(2)).invoke(removeMeterInputCpt.capture());
        final List<RemoveMeterInput> allValues = removeMeterInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    @Test
    public void testRemoveMetersBatch_failure() throws Exception {
        Mockito.when(salMeterService.getRpcClassToInstanceMap())
                .thenReturn(ImmutableClassToInstanceMap.of(RemoveMeter.class, removeMeter));
        Mockito.when(removeMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.<RemoveMeterOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupRemoveError")
                        .buildFuture());

        final RemoveMetersBatchInput input = new RemoveMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveMeters(BindingMap.ordered(
                        createEmptyBatchRemoveMeter(42L),
                        createEmptyBatchRemoveMeter(43L)))
                .build();

        final Future<RpcResult<RemoveMetersBatchOutput>> resultFuture = salMetersBatchService.removeMetersBatch(input);
        Iterator<BatchFailedMetersOutput> iterator = resultFuture.get().getResult().nonnullBatchFailedMetersOutput()
                .values().iterator();

        Assert.assertTrue(resultFuture.isDone());
        Assert.assertFalse(resultFuture.get().isSuccessful());
        Assert.assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedMetersOutput().size());
        Assert.assertEquals(42L, iterator.next().getMeterId().getValue().longValue());
        Assert.assertEquals(43L, iterator.next().getMeterId().getValue().longValue());
        Assert.assertEquals(2, resultFuture.get().getErrors().size());

        final InOrder inOrder = Mockito.inOrder(removeMeter, sendBarrier);

        inOrder.verify(removeMeter, Mockito.times(2)).invoke(removeMeterInputCpt.capture());
        final List<RemoveMeterInput> allValues = removeMeterInputCpt.getAllValues();
        Assert.assertEquals(2, allValues.size());
        Assert.assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        Assert.assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(ArgumentMatchers.any());
    }

    private static BatchAddMeters createEmptyBatchAddMeter(final long groupIdValue) {
        return new BatchAddMetersBuilder()
                .setMeterId(new MeterId(Uint32.valueOf(groupIdValue)))
                .build();
    }

    private static BatchRemoveMeters createEmptyBatchRemoveMeter(final long groupIdValue) {
        return new BatchRemoveMetersBuilder()
                .setMeterId(new MeterId(Uint32.valueOf(groupIdValue)))
                .build();
    }

    private static BatchUpdateMeters createEmptyBatchUpdateMeter(final long groupIdValue) {
        return new BatchUpdateMetersBuilder()
                .setOriginalBatchedMeter(
                        new OriginalBatchedMeterBuilder(createEmptyBatchAddMeter(groupIdValue)).build())
                .setUpdatedBatchedMeter(
                        new UpdatedBatchedMeterBuilder(createEmptyBatchAddMeter(groupIdValue + 1)).build())
                .build();
    }
}
