/**
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.config.rev131024.Meters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginMeterTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginMeterTestCommandProvider.class);
    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private Meter testMeter;
    private Meter testMeter1;
    private Meter testMeter2;
    private Node testNode;
    private final String originalMeterName = "Foo";
    private final String updatedMeterName = "Bar";
    private final MeterEventListener meterEventListener = new MeterEventListener();
    private static NotificationService notificationService;
    private Registration<org.opendaylight.yangtools.yang.binding.NotificationListener> listener1Reg;

    public OpenflowpluginMeterTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        notificationService = session.getSALService(NotificationService.class);
        // For switch events
        listener1Reg = notificationService.registerNotificationListener(meterEventListener);

        createTestNode();
        createTestMeter();
    }

    private void createUserNode(String nodeRef) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private void createTestNode() {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    final class MeterEventListener implements SalMeterListener {

        @Override
        public void onMeterAdded(MeterAdded notification) {
            LOG.info("Meter to be added.........................." + notification.toString());
            LOG.info("Meter  Xid........................." + notification.getTransactionId().getValue());
            LOG.info("-----------------------------------------------------------------------------------");
        }

        @Override
        public void onMeterRemoved(MeterRemoved notification) {
            LOG.info("Meter to be removed.........................." + notification.toString());
            LOG.info("Meter  Xid........................." + notification.getTransactionId().getValue());
            LOG.info("-----------------------------------------------------------------------------------");
        }

        @Override
        public void onMeterUpdated(MeterUpdated notification) {
            LOG.info("Meter to be updated.........................." + notification.toString());
            LOG.info("Meter  Xid........................." + notification.getTransactionId().getValue());
            LOG.info("-----------------------------------------------------------------------------------");
        }

    }

    private MeterBuilder createTestMeter() {
        // Sample data , committing to DataStore
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();

        long id = 12;
        MeterKey key = new MeterKey(new MeterId(id));
        MeterBuilder meter = new MeterBuilder();
        meter.setContainerName("abcd");
        meter.setKey(key);
        meter.setMeterId(new MeterId(9L));
        meter.setMeterName(originalMeterName);
        meter.setFlags(new MeterFlags(true, false, false, false));
        MeterBandHeadersBuilder bandHeaders = new MeterBandHeadersBuilder();
        List<MeterBandHeader> bandHdr = new ArrayList<MeterBandHeader>();
        MeterBandHeaderBuilder bandHeader = new MeterBandHeaderBuilder();
        bandHeader.setBandRate((long) 234);
        bandHeader.setBandBurstSize((long) 444);
        DscpRemarkBuilder dscpRemark = new DscpRemarkBuilder();
        dscpRemark.setDscpRemarkBurstSize((long) 5);
        dscpRemark.setPercLevel((short) 1);
        dscpRemark.setDscpRemarkRate((long) 12);
        bandHeader.setBandType(dscpRemark.build());
        MeterBandTypesBuilder bandTypes = new MeterBandTypesBuilder();
        MeterBandType bandType = new MeterBandType(false, true, false);
        bandTypes.setFlags(bandType);
        bandHeader.setMeterBandTypes(bandTypes.build());
        bandHeader.setBandId(new BandId(0L));
        bandHdr.add(bandHeader.build());
        bandHeaders.setMeterBandHeader(bandHdr);
        meter.setMeterBandHeaders(bandHeaders.build());

        testMeter = meter.build();
        return meter;
    }

    private MeterBuilder createTestMeters(String s1, String s2) {
        // Sample data , committing to DataStore
        long id = Integer.parseInt(s1);
        MeterKey key = new MeterKey(new MeterId(id));
        MeterBuilder meter = new MeterBuilder();
        meter.setContainerName("abcd");
        meter.setKey(key);
        meter.setMeterId(new MeterId(9L));
        MeterBandHeaderBuilder bandHeader = new MeterBandHeaderBuilder();
        if (s2.equalsIgnoreCase("modify")) {
            meter.setMeterName(updatedMeterName);
            bandHeader.setBandRate((long) 234);
        } else {
            meter.setMeterName(originalMeterName);
            bandHeader.setBandRate((long) 123);
        }
        meter.setFlags(new MeterFlags(true, false, false, false));
        MeterBandHeadersBuilder bandHeaders = new MeterBandHeadersBuilder();
        List<MeterBandHeader> bandHdr = new ArrayList<MeterBandHeader>();

        bandHeader.setBandBurstSize((long) 444);
        DscpRemarkBuilder dscpRemark = new DscpRemarkBuilder();
        dscpRemark.setDscpRemarkBurstSize((long) 5);
        dscpRemark.setPercLevel((short) 1);
        dscpRemark.setDscpRemarkRate((long) 12);
        bandHeader.setBandType(dscpRemark.build());
        MeterBandTypesBuilder bandTypes = new MeterBandTypesBuilder();
        MeterBandType bandType = new MeterBandType(false, true, false);
        bandTypes.setFlags(bandType);
        bandHeader.setMeterBandTypes(bandTypes.build());
        bandHeader.setBandId(new BandId(0L));
        bandHdr.add(bandHeader.build());
        bandHeaders.setMeterBandHeader(bandHdr);
        meter.setMeterBandHeaders(bandHeaders.build());

        if (Integer.parseInt(s1) == 1) {
            testMeter1 = meter.build();
        } else if (Integer.parseInt(s1) == 2) {
            testMeter2 = meter.build();
        } else if (Integer.parseInt(s1) == 3) {
            testMeter1 = meter.build();
        } else if (Integer.parseInt(s1) == 4) {
            testMeter2 = meter.build();
        } else if (Integer.parseInt(s1) == 5) {
            testMeter1 = meter.build();
        } else if (Integer.parseInt(s1) == 6) {
            testMeter2 = meter.build();
        } else if (Integer.parseInt(s1) == 7) {
            testMeter1 = meter.build();
        } else if (Integer.parseInt(s1) == 8) {
            testMeter2 = meter.build();
        }

        return meter;
    }

    public void _removeMeter(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        MeterBuilder mBuilder = createTestMeter();
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(testMeter.getMeterId())).build();
        modification.removeConfigurationData(path1);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Meter Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _removeMeters(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }

        Integer count = Integer.parseInt(ci.nextArgument());

        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        switch (count) {
        case 1:
            MeterBuilder mBuilder = createTestMeters("1", "remove");
            InstanceIdentifier<Meter> path1 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter1.getMeterId())).build();
            modification.removeConfigurationData(path1);
            MeterBuilder mBuilder1 = createTestMeters("2", "remove");
            InstanceIdentifier<Meter> path2 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter2.getMeterId())).build();
            modification.removeConfigurationData(path2);

            break;
        case 2:
            MeterBuilder mBuilder2 = createTestMeters("3", "remove");
            InstanceIdentifier<Meter> path3 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter1.getMeterId())).build();
            modification.removeConfigurationData(path3);
            MeterBuilder mBuilder22 = createTestMeters("4", "remove");
            InstanceIdentifier<Meter> path4 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter2.getMeterId())).build();
            modification.removeConfigurationData(path4);
            break;
        case 3:
            MeterBuilder mBuilder3 = createTestMeters("5", "remove");
            InstanceIdentifier<Meter> path5 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter1.getMeterId())).build();
            modification.removeConfigurationData(path5);
            MeterBuilder mBuilder4 = createTestMeters("6", "remove");
            InstanceIdentifier<Meter> path6 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter2.getMeterId())).build();
            modification.removeConfigurationData(path6);
            break;
        case 4:
            MeterBuilder mBuilder5 = createTestMeters("7", "remove");
            InstanceIdentifier<Meter> path7 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter1.getMeterId())).build();
            modification.removeConfigurationData(path7);
            MeterBuilder mBuilder6 = createTestMeters("8", "remove");
            InstanceIdentifier<Meter> path8 = InstanceIdentifier.builder(Nodes.class)
                    .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                    .child(Meter.class, new MeterKey(testMeter2.getMeterId())).build();
            modification.removeConfigurationData(path8);
            break;

        }

        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Meter Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _addMeter(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        createTestMeter();
        writeMeter(ci, testMeter);
    }

    public void _addMeters(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        Integer count = Integer.parseInt(ci.nextArgument());
        switch (count) {
        case 1:
            createTestMeters("1", "add");
            createTestMeters("2", "add");
            writeMeter(ci, testMeter1, testMeter2);
            break;
        case 2:
            createTestMeters("3", "add");
            createTestMeters("4", "add");
            writeMeter(ci, testMeter1, testMeter2);
            break;
        case 3:
            createTestMeters("5", "add");
            createTestMeters("6", "add");
            writeMeter(ci, testMeter1, testMeter2);
            break;
        case 4:
            createTestMeters("7", "add");
            createTestMeters("8", "add");
            writeMeter(ci, testMeter1, testMeter2);
            break;

        }
        // createTestMeters();
        // writeMeter(ci, testMeter);
    }

    private void writeMeter(CommandInterpreter ci, Meter meter) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId())).build();
        DataObject cls = modification.readConfigurationData(path1);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, meter);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Meter Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeMeter(CommandInterpreter ci, Meter meter, Meter meter1) {
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId())).build();
        DataObject cls = modification.readConfigurationData(path1);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, meter);
        InstanceIdentifier<Meter> path2 = InstanceIdentifier.builder(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter1.getMeterId())).build();
        DataObject cls1 = modification.readConfigurationData(path2);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path2, meter1);

        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Meter Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _modifyMeter(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        MeterBuilder meter = createTestMeter();
        meter.setMeterName(updatedMeterName);
        writeMeter(ci, meter.build());
        meter.setMeterName(originalMeterName);
        writeMeter(ci, meter.build());
    }

    public void _modifyMeters(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }

        Integer count = Integer.parseInt(ci.nextArgument());
        switch (count) {
        case 1:
            createTestMeters("1", "modify");
            createTestMeters("2", "modify");
            writeMeter(ci, testMeter1, testMeter2);
            break;
        case 2:
            createTestMeters("3", "modify");
            createTestMeters("4", "modify");
            writeMeter(ci, testMeter1, testMeter2);
            break;
        }
    }

    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---FRM MD-SAL Group test module---\n");
        help.append("\t addMeter <node id>        - node ref\n");
        help.append("\t modifyMeter <node id>        - node ref\n");
        help.append("\t removeMeter <node id>        - node ref\n");

        return help.toString();
    }

}
