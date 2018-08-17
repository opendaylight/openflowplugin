/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.applications.frm.impl.DeviceMastershipManager;
import org.opendaylight.openflowplugin.applications.frm.impl.ForwardingRulesManagerImpl;
import org.opendaylight.openflowplugin.applications.frm.recovery.OpenflowServiceRecoveryHandler;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.serviceutils.srm.ServiceRecoveryRegistry;
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
import test.mock.util.FRMTest;
import test.mock.util.RpcProviderRegistryMock;
import test.mock.util.SalMeterServiceMock;

@RunWith(MockitoJUnitRunner.class)
public class MeterListenerTest extends FRMTest {
    private ForwardingRulesManagerImpl forwardingRulesManager;
    private static final NodeId NODE_ID = new NodeId("testnode:1");
    private static final NodeKey NODE_KEY = new NodeKey(NODE_ID);
    RpcProviderRegistry rpcProviderRegistryMock = new RpcProviderRegistryMock();
    @Mock
    ClusterSingletonServiceProvider clusterSingletonService;
    @Mock
    DeviceMastershipManager deviceMastershipManager;
    @Mock
    private ReconciliationManager reconciliationManager;
    @Mock
    private OpenflowServiceRecoveryHandler openflowServiceRecoveryHandler;
    @Mock
    private ServiceRecoveryRegistry serviceRecoveryRegistry;
    @Mock
    private MastershipChangeServiceManager mastershipChangeServiceManager;

    @Before
    public void setUp() {
        forwardingRulesManager = new ForwardingRulesManagerImpl(
                getDataBroker(),
                rpcProviderRegistryMock,
                getConfig(),
                mastershipChangeServiceManager,
                clusterSingletonService,
                getConfigurationService(),
                reconciliationManager,
                openflowServiceRecoveryHandler,
                serviceRecoveryRegistry);

        forwardingRulesManager.start();
        // TODO consider tests rewrite (added because of complicated access)
        forwardingRulesManager.setDeviceMastershipManager(deviceMastershipManager);
        Mockito.when(deviceMastershipManager.isDeviceMastered(NODE_ID)).thenReturn(true);
    }

    @Test
    public void addTwoMetersTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        MeterKey meterKey = new MeterKey(new MeterId((long) 2000));
        InstanceIdentifier<Meter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        Meter meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_one").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.submit());
        SalMeterServiceMock salMeterService = (SalMeterServiceMock) forwardingRulesManager.getSalMeterService();
        List<AddMeterInput> addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(1, addMeterCalls.size());
        assertEquals("DOM-0", addMeterCalls.get(0).getTransactionUri().getValue());

        meterKey = new MeterKey(new MeterId((long) 2001));
        meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_two").setBarrier(true).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.submit());
        salMeterService = (SalMeterServiceMock) forwardingRulesManager.getSalMeterService();
        addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(2, addMeterCalls.size());
        assertEquals("DOM-1", addMeterCalls.get(1).getTransactionUri().getValue());
        assertEquals(meterII, addMeterCalls.get(1).getMeterRef().getValue());
    }

    @Test
    public void updateMeterTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        MeterKey meterKey = new MeterKey(new MeterId((long) 2000));
        InstanceIdentifier<Meter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        Meter meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_one").setBarrier(false).build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.submit());
        SalMeterServiceMock salMeterService = (SalMeterServiceMock) forwardingRulesManager.getSalMeterService();
        List<AddMeterInput> addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(1, addMeterCalls.size());
        assertEquals("DOM-0", addMeterCalls.get(0).getTransactionUri().getValue());

        meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_two").setBarrier(true).build();
        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.submit());
        salMeterService = (SalMeterServiceMock) forwardingRulesManager.getSalMeterService();
        List<UpdateMeterInput> updateMeterCalls = salMeterService.getUpdateMeterCalls();
        assertEquals(1, updateMeterCalls.size());
        assertEquals("DOM-1", updateMeterCalls.get(0).getTransactionUri().getValue());
        assertEquals(meterII, updateMeterCalls.get(0).getMeterRef().getValue());
    }

    @Test
    public void removeMeterTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        MeterKey meterKey = new MeterKey(new MeterId((long) 2000));
        InstanceIdentifier<Meter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(Meter.class, meterKey);
        Meter meter = new MeterBuilder().withKey(meterKey).setMeterName("meter_one").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.submit());
        SalMeterServiceMock salMeterService = (SalMeterServiceMock) forwardingRulesManager.getSalMeterService();
        List<AddMeterInput> addMeterCalls = salMeterService.getAddMeterCalls();
        assertEquals(1, addMeterCalls.size());
        assertEquals("DOM-0", addMeterCalls.get(0).getTransactionUri().getValue());

        writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.delete(LogicalDatastoreType.CONFIGURATION, meterII);
        assertCommit(writeTx.submit());
        salMeterService = (SalMeterServiceMock) forwardingRulesManager.getSalMeterService();
        List<RemoveMeterInput> removeMeterCalls = salMeterService.getRemoveMeterCalls();
        assertEquals(1, removeMeterCalls.size());
        assertEquals("DOM-1", removeMeterCalls.get(0).getTransactionUri().getValue());
        assertEquals(meterII, removeMeterCalls.get(0).getMeterRef().getValue());
    }

    @Test
    public void staleMeterCreationTest() throws Exception {
        addFlowCapableNode(NODE_KEY);

        StaleMeterKey meterKey = new StaleMeterKey(new MeterId((long) 2000));
        InstanceIdentifier<StaleMeter> meterII = InstanceIdentifier.create(Nodes.class).child(Node.class, NODE_KEY)
                .augmentation(FlowCapableNode.class).child(StaleMeter.class, meterKey);
        StaleMeter meter = new StaleMeterBuilder().withKey(meterKey).setMeterName("stale_meter_one").build();

        WriteTransaction writeTx = getDataBroker().newWriteOnlyTransaction();
        writeTx.put(LogicalDatastoreType.CONFIGURATION, meterII, meter);
        assertCommit(writeTx.submit());
    }

    @After
    public void tearDown() throws Exception {
        forwardingRulesManager.close();
    }
}
