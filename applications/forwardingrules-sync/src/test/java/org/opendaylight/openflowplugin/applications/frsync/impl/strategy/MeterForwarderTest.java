/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.strategy;

import java.math.BigInteger;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link MeterForwarder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterForwarderTest {

    private final NodeKey s1Key = new NodeKey(new NodeId("S1"));
    private final MeterId meterId = new MeterId(42L);
    private final MeterKey meterKey = new MeterKey(meterId);
    private final Meter meter = new MeterBuilder()
            .setMeterId(meterId)
            .setMeterName("test-meter")
            .build();

    private final KeyedInstanceIdentifier<Node, NodeKey> nodePath = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, s1Key);
    private final InstanceIdentifier<FlowCapableNode> flowCapableNodePath = nodePath
            .augmentation(FlowCapableNode.class);
    private final InstanceIdentifier<Meter> meterPath = flowCapableNodePath.child(Meter.class, meterKey);

    @Mock
    private SalMeterService salMeterService;
    @Captor
    private ArgumentCaptor<AddMeterInput> addMeterInputCpt;
    @Captor
    private ArgumentCaptor<RemoveMeterInput> removeMeterInputCpt;
    @Captor
    private ArgumentCaptor<UpdateMeterInput> updateMeterInputCpt;

    private TransactionId txId;

    private MeterForwarder meterForwarder;

    @Before
    public void setUp() throws Exception {
        meterForwarder = new MeterForwarder(salMeterService);
        txId = new TransactionId(BigInteger.ONE);
    }

    @Test
    public void testRemove() throws Exception {
        Mockito.when(salMeterService.removeMeter(removeMeterInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(new RemoveMeterOutputBuilder()
                        .setTransactionId(txId)
                        .build()).buildFuture()
        );

        Meter removeMeter = new MeterBuilder(meter).build();

        final Future<RpcResult<RemoveMeterOutput>> removeResult = meterForwarder.remove(meterPath, removeMeter, flowCapableNodePath);
        Mockito.verify(salMeterService).removeMeter(Matchers.<RemoveMeterInput>any());

        Assert.assertTrue(removeResult.isDone());
        final RpcResult<RemoveMeterOutput> meterResult = removeResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(meterResult.isSuccessful());

        Assert.assertEquals(1, meterResult.getResult().getTransactionId().getValue().intValue());

        final RemoveMeterInput removeMeterInput = removeMeterInputCpt.getValue();
        Assert.assertEquals(meterPath, removeMeterInput.getMeterRef().getValue());
        Assert.assertEquals(nodePath, removeMeterInput.getNode().getValue());
        Assert.assertEquals("test-meter", removeMeterInput.getMeterName());
    }

    @Test
    public void testUpdate() throws Exception {
        Mockito.when(salMeterService.updateMeter(updateMeterInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(new UpdateMeterOutputBuilder()
                        .setTransactionId(txId)
                        .build()).buildFuture()
        );

        Meter meterOriginal = new MeterBuilder(meter).build();
        Meter meterUpdate = new MeterBuilder(meter)
                .setMeterName("another-test")
                .build();

        final Future<RpcResult<UpdateMeterOutput>> updateResult = meterForwarder.update(meterPath, meterOriginal, meterUpdate,
                flowCapableNodePath);
        Mockito.verify(salMeterService).updateMeter(Matchers.<UpdateMeterInput>any());

        Assert.assertTrue(updateResult.isDone());
        final RpcResult<UpdateMeterOutput> meterResult = updateResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(meterResult.isSuccessful());

        Assert.assertEquals(1, meterResult.getResult().getTransactionId().getValue().intValue());

        final UpdateMeterInput updateMeterInput = updateMeterInputCpt.getValue();
        Assert.assertEquals(meterPath, updateMeterInput.getMeterRef().getValue());
        Assert.assertEquals(nodePath, updateMeterInput.getNode().getValue());

        Assert.assertEquals("test-meter", updateMeterInput.getOriginalMeter().getMeterName());
        Assert.assertEquals("another-test", updateMeterInput.getUpdatedMeter().getMeterName());
    }

    @Test
    public void testAdd() throws Exception {
        Mockito.when(salMeterService.addMeter(addMeterInputCpt.capture())).thenReturn(
                RpcResultBuilder.success(new AddMeterOutputBuilder()
                        .setTransactionId(txId)
                        .build()).buildFuture()
        );

        final Future<RpcResult<AddMeterOutput>> addResult = meterForwarder.add(meterPath, meter, flowCapableNodePath);
        Mockito.verify(salMeterService).addMeter(Matchers.<AddMeterInput>any());

        Assert.assertTrue(addResult.isDone());
        final RpcResult<AddMeterOutput> meterResult = addResult.get(2, TimeUnit.SECONDS);
        Assert.assertTrue(meterResult.isSuccessful());

        Assert.assertEquals(1, meterResult.getResult().getTransactionId().getValue().intValue());

        final AddMeterInput addMeterInput = addMeterInputCpt.getValue();
        Assert.assertEquals(meterPath, addMeterInput.getMeterRef().getValue());
        Assert.assertEquals(nodePath, addMeterInput.getNode().getValue());
        Assert.assertEquals("test-meter", addMeterInput.getMeterName());
    }
}