/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.SendBarrierOutput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.RemoveMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.UpdateMetersBatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.add.meters.batch.input.BatchAddMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.OriginalBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.input.update.grouping.UpdatedBatchedMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.remove.meters.batch.input.BatchRemoveMetersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMeters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.update.meters.batch.input.BatchUpdateMetersBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Test for {@link org.opendaylight.openflowplugin.impl.services.sal.SalMetersBatchServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SalMetersBatchServiceImplTest {
    public static final NodeId NODE_ID = new NodeId("ut-dummy-node");
    public static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    public static final NodeRef NODE_REF =
            new NodeRef(InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY));

    @Mock
    private AddMeter addMeter;
    @Mock
    private RemoveMeter removeMeter;
    @Mock
    private UpdateMeter updateMeter;
    @Mock
    private SendBarrier sendBarrier;
    @Captor
    private ArgumentCaptor<RemoveMeterInput> removeMeterInputCpt;
    @Captor
    private ArgumentCaptor<UpdateMeterInput> updateMeterInputCpt;
    @Captor
    private ArgumentCaptor<AddMeterInput> addMeterInputCpt;

    private AddMetersBatchImpl addMetersBatch;
    private RemoveMetersBatchImpl removeMetersBatch;
    private UpdateMetersBatchImpl updateMetersBatch;

    @Before
    public void setUp() {
        addMetersBatch = new AddMetersBatchImpl(addMeter, sendBarrier);
        removeMetersBatch = new RemoveMetersBatchImpl(removeMeter, sendBarrier);
        updateMetersBatch = new UpdateMetersBatchImpl(updateMeter, sendBarrier);

        when(sendBarrier.invoke(any())).thenReturn(RpcResultBuilder.<SendBarrierOutput>success().buildFuture());
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(addMeter, removeMeter, updateMeter, sendBarrier);
    }

    @Test
    public void testUpdateMetersBatch_success() throws Exception {
        when(updateMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new UpdateMeterOutputBuilder().build()).buildFuture());

        final var input = new UpdateMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateMeters(List.of(
                        createEmptyBatchUpdateMeter(42L),
                        createEmptyBatchUpdateMeter(44L)))
                .build();

        final var resultFuture = updateMetersBatch.invoke(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(updateMeter, sendBarrier);
        inOrder.verify(updateMeter, times(2)).invoke(updateMeterInputCpt.capture());
        final var allValues = updateMeterInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getOriginalMeter().getMeterId().getValue().longValue());
        assertEquals(43, allValues.get(0).getUpdatedMeter().getMeterId().getValue().longValue());
        assertEquals(44, allValues.get(1).getOriginalMeter().getMeterId().getValue().longValue());
        assertEquals(45, allValues.get(1).getUpdatedMeter().getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testUpdateMetersBatch_failure() throws Exception {
        when(updateMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.<UpdateMeterOutput>failed()
                        .withError(ErrorType.APPLICATION, "ur-groupUpdateError")
                        .buildFuture());

        final var input = new UpdateMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchUpdateMeters(List.of(
                        createEmptyBatchUpdateMeter(42L),
                        createEmptyBatchUpdateMeter(44L)))
                .build();

        final var resultFuture = updateMetersBatch.invoke(input);
        final var iterator = resultFuture.get().getResult().nonnullBatchFailedMetersOutput()
                .values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedMetersOutput().size());
        assertEquals(43L, iterator.next().getMeterId().getValue().longValue());
        assertEquals(45L,iterator.next().getMeterId().getValue().longValue());
        assertEquals(2, resultFuture.get().getErrors().size());


        final var inOrder = inOrder(updateMeter, sendBarrier);
        inOrder.verify(updateMeter, times(2)).invoke(updateMeterInputCpt.capture());
        final var allValues = updateMeterInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42, allValues.get(0).getOriginalMeter().getMeterId().getValue().longValue());
        assertEquals(43, allValues.get(0).getUpdatedMeter().getMeterId().getValue().longValue());
        assertEquals(44, allValues.get(1).getOriginalMeter().getMeterId().getValue().longValue());
        assertEquals(45, allValues.get(1).getUpdatedMeter().getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testAddMetersBatch_success() throws Exception {
        when(addMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new AddMeterOutputBuilder().build()).buildFuture());

        final var input = new AddMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddMeters(BindingMap.ordered(
                        createEmptyBatchAddMeter(42L),
                        createEmptyBatchAddMeter(43L)))
                .build();

        final var resultFuture = addMetersBatch.invoke(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(addMeter, sendBarrier);
        inOrder.verify(addMeter, times(2)).invoke(addMeterInputCpt.capture());
        final var allValues = addMeterInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testAddMetersBatch_failure() throws Exception {
        when(addMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.<AddMeterOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupAddError")
                        .buildFuture());

        final var input = new AddMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchAddMeters(BindingMap.ordered(
                        createEmptyBatchAddMeter(42L),
                        createEmptyBatchAddMeter(43L)))
                .build();

        final var resultFuture = addMetersBatch.invoke(input);
        final var iterator = resultFuture.get().getResult().nonnullBatchFailedMetersOutput().values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedMetersOutput().size());
        assertEquals(42L, iterator.next().getMeterId().getValue().longValue());
        assertEquals(43L,iterator.next().getMeterId().getValue().longValue());
        assertEquals(2, resultFuture.get().getErrors().size());


        final var inOrder = inOrder(addMeter, sendBarrier);
        inOrder.verify(addMeter, times(2)).invoke(addMeterInputCpt.capture());
        final var allValues = addMeterInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testRemoveMetersBatch_success() throws Exception {
        when(removeMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.success(new RemoveMeterOutputBuilder().build()).buildFuture());

        final var input = new RemoveMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveMeters(BindingMap.ordered(
                        createEmptyBatchRemoveMeter(42L),
                        createEmptyBatchRemoveMeter(43L)))
                .build();

        final var resultFuture = removeMetersBatch.invoke(input);

        assertTrue(resultFuture.isDone());
        assertTrue(resultFuture.get().isSuccessful());

        final var inOrder = inOrder(removeMeter, sendBarrier);

        inOrder.verify(removeMeter, times(2)).invoke(removeMeterInputCpt.capture());
        final var allValues = removeMeterInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
    }

    @Test
    public void testRemoveMetersBatch_failure() throws Exception {
        when(removeMeter.invoke(any()))
                .thenReturn(RpcResultBuilder.<RemoveMeterOutput>failed()
                        .withError(ErrorType.APPLICATION, "ut-groupRemoveError")
                        .buildFuture());

        final var input = new RemoveMetersBatchInputBuilder()
                .setNode(NODE_REF)
                .setBarrierAfter(true)
                .setBatchRemoveMeters(BindingMap.ordered(
                        createEmptyBatchRemoveMeter(42L),
                        createEmptyBatchRemoveMeter(43L)))
                .build();

        final var resultFuture = removeMetersBatch.invoke(input);
        final var iterator = resultFuture.get().getResult().nonnullBatchFailedMetersOutput().values().iterator();

        assertTrue(resultFuture.isDone());
        assertFalse(resultFuture.get().isSuccessful());
        assertEquals(2, resultFuture.get().getResult().nonnullBatchFailedMetersOutput().size());
        assertEquals(42L, iterator.next().getMeterId().getValue().longValue());
        assertEquals(43L, iterator.next().getMeterId().getValue().longValue());
        assertEquals(2, resultFuture.get().getErrors().size());

        final var inOrder = inOrder(removeMeter, sendBarrier);

        inOrder.verify(removeMeter, times(2)).invoke(removeMeterInputCpt.capture());
        final var allValues = removeMeterInputCpt.getAllValues();
        assertEquals(2, allValues.size());
        assertEquals(42L, allValues.get(0).getMeterId().getValue().longValue());
        assertEquals(43L, allValues.get(1).getMeterId().getValue().longValue());

        inOrder.verify(sendBarrier).invoke(any());
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
