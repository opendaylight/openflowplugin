/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.StaleMeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.AddMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.RemoveMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.UpdateMeterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import test.mock.util.FRMTest;
import test.mock.util.SalMeterServiceMock;

@RunWith(MockitoJUnitRunner.class)
public class MeterListenerTest extends FRMTest {
    private static final NodeId NODE_ID = new NodeId("testnode:1");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);

    @Before
    public void setUp() {
        setUpForwardingRulesManager();
        setDeviceMastership(NODE_ID);
    }

    @Test
    public void addTwoMetersTest() {
        addFlowCapableNode(NODE_KEY);

        MeterKey meterKey = new MeterKey(new MeterId(Uint32.valueOf(2000)));
        InstanceIdentifier<Meter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        Meter meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_one").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.commit());
        SalMeterServiceMock salMeterService = (SalMeterServiceMock) getForwardingRulesManager().getSalMeterService();
        await().atMost(10, SECONDS).until(() -> salMeterService.getAddMeterCalls().size() == 1);
        List<AddMeterInput> addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(1, addMeterCalls.size());
        assertEquals("DOM-0", addMeterCalls.get(0).getTransactionUri().getValue());

        meterKey = new MeterKey(new MeterId(Uint32.valueOf(2001)));
        meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_two").setBarrier(true).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> salMeterService.getAddMeterCalls().size() == 2);
        addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(2, addMeterCalls.size());
        assertEquals("DOM-1", addMeterCalls.get(1).getTransactionUri().getValue());
        assertEquals(meterII, addMeterCalls.get(1).getMeterRef().getValue());
    }

    @Test
    public void updateMeterTest() {
        addFlowCapableNode(NODE_KEY);

        MeterKey meterKey = new MeterKey(new MeterId(Uint32.valueOf(2000)));
        InstanceIdentifier<Meter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        Meter meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_one").setBarrier(false).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.commit());
        SalMeterServiceMock salMeterService = (SalMeterServiceMock) getForwardingRulesManager().getSalMeterService();
        await().atMost(10, SECONDS).until(() -> salMeterService.getAddMeterCalls().size() == 1);
        List<AddMeterInput> addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(1, addMeterCalls.size());
        assertEquals("DOM-0", addMeterCalls.get(0).getTransactionUri().getValue());

        meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_two").setBarrier(true).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> salMeterService.getUpdateMeterCalls().size() == 1);
        List<UpdateMeterInput> updateMeterCalls = salMeterService.getUpdateMeterCalls();
        assertEquals(1, updateMeterCalls.size());
        assertEquals("DOM-1", updateMeterCalls.get(0).getTransactionUri().getValue());
        assertEquals(meterII, updateMeterCalls.get(0).getMeterRef().getValue());
    }

    @Test
    public void removeMeterTest() {
        addFlowCapableNode(NODE_KEY);

        MeterKey meterKey = new MeterKey(new MeterId(Uint32.valueOf(2000)));
        InstanceIdentifier<Meter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        Meter meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_one").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.commit());
        SalMeterServiceMock salMeterService = (SalMeterServiceMock) getForwardingRulesManager().getSalMeterService();
        await().atMost(10, SECONDS).until(() -> salMeterService.getAddMeterCalls().size() == 1);
        List<AddMeterInput> addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(1, addMeterCalls.size());
        assertEquals("DOM-0", addMeterCalls.get(0).getTransactionUri().getValue());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, meterII);
        assertCommit(writeTx.commit());
        await().atMost(10, SECONDS).until(() -> salMeterService.getRemoveMeterCalls().size() == 1);
        List<RemoveMeterInput> removeMeterCalls = salMeterService.getRemoveMeterCalls();
        assertEquals(1, removeMeterCalls.size());
        assertEquals("DOM-1", removeMeterCalls.get(0).getTransactionUri().getValue());
        assertEquals(meterII, removeMeterCalls.get(0).getMeterRef().getValue());
    }

    @Test
    public void staleMeterCreationTest() {
        addFlowCapableNode(NODE_KEY);

        StaleMeterKey meterKey = new StaleMeterKey(new MeterId(Uint32.valueOf(2000)));
        InstanceIdentifier<StaleMeter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(StaleMeter.class, meterKey);
        StaleMeter meter = new StaleMeterBuilder().withKey(meterKey).setMeterName("stale_meter_one").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.commit());
    }

    @After
    public void tearDown() throws Exception {
        getForwardingRulesManager().close();
    }
}
