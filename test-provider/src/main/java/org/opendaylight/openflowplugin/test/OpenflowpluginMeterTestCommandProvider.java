/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginMeterTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginMeterTestCommandProvider.class);
    private DataBroker dataBroker;
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
    private Registration listener1Reg;

    public OpenflowpluginMeterTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBroker = session.getSALService(DataBroker.class);
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
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.getKey());
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
        dscpRemark.setPrecLevel((short) 1);
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
        dscpRemark.setPrecLevel((short) 1);
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

    public void _removeMeter(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        MeterBuilder mBuilder = createTestMeter();
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(testMeter.getMeterId()));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    public void _removeMeters(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }

        Integer count = Integer.parseInt(ci.nextArgument());

        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        switch (count) {
            case 1:
                MeterBuilder mBuilder = createTestMeters("1", "remove");
                InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
                MeterBuilder mBuilder1 = createTestMeters("2", "remove");
                InstanceIdentifier<Meter> path2 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path2);

                break;
            case 2:
                MeterBuilder mBuilder2 = createTestMeters("3", "remove");
                InstanceIdentifier<Meter> path3 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path3);
                MeterBuilder mBuilder22 = createTestMeters("4", "remove");
                InstanceIdentifier<Meter> path4 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path4);
                break;
            case 3:
                MeterBuilder mBuilder3 = createTestMeters("5", "remove");
                InstanceIdentifier<Meter> path5 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path5);
                MeterBuilder mBuilder4 = createTestMeters("6", "remove");
                InstanceIdentifier<Meter> path6 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path6);
                break;
            case 4:
                MeterBuilder mBuilder5 = createTestMeters("7", "remove");
                InstanceIdentifier<Meter> path7 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path7);
                MeterBuilder mBuilder6 = createTestMeters("8", "remove");
                InstanceIdentifier<Meter> path8 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path8);
                break;

        }

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
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

    private void writeMeter(final CommandInterpreter ci, Meter meter) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode), testNode, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, meter, true);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    private void writeMeter(final CommandInterpreter ci, Meter meter, Meter meter1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode), testNode, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, meter, true);
        InstanceIdentifier<Meter> path2 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter1.getMeterId()));
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode), testNode, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path2, meter1, true);

        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
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
        StringBuilder help = new StringBuilder();
        help.append("---FRM MD-SAL Meter test module---\n");
        help.append("\t addMeter <node id>           - node ref\n");
        help.append("\t modifyMeter <node id>        - node ref\n");
        help.append("\t removeMeter <node id>        - node ref\n");

        return help.toString();
    }

}
