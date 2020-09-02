/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:MethodName")
public class OpenflowpluginMeterTestCommandProvider implements CommandProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginMeterTestCommandProvider.class);
    private final DataBroker dataBroker;
    private final BundleContext ctx;
    private Meter testMeter;
    private Meter testMeter1;
    private Meter testMeter2;
    private Node testNode;
    private final String originalMeterName = "Foo";
    private final String updatedMeterName = "Bar";
    private final MeterEventListener meterEventListener = new MeterEventListener();
    private final NotificationService notificationService;
    private Registration listener1Reg;

    public OpenflowpluginMeterTestCommandProvider(DataBroker dataBroker, NotificationService notificationService,
            BundleContext ctx) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
        this.ctx = ctx;
    }

    public void init() {
        ctx.registerService(CommandProvider.class.getName(), this, null);
        // For switch events
        listener1Reg = notificationService.registerNotificationListener(meterEventListener);

        createTestNode();
        createTestMeter();
    }

    private void createUserNode(String nodeRef) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.withKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private void createTestNode() {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.withKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private static InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.key());
    }

    private static final class MeterEventListener implements SalMeterListener {

        @Override
        public void onMeterAdded(MeterAdded notification) {
            LOG.info("Meter to be added {}", notification.toString());
            LOG.info("Meter  Xid {}", notification.getTransactionId().getValue());
        }

        @Override
        public void onMeterRemoved(MeterRemoved notification) {
            LOG.info("Meter to be removed {}", notification.toString());
            LOG.info("Meter  Xid {}", notification.getTransactionId().getValue());
        }

        @Override
        public void onMeterUpdated(MeterUpdated notification) {
            LOG.info("Meter to be updated {}", notification.toString());
            LOG.info("Meter  Xid {}", notification.getTransactionId().getValue());
        }

    }

    private MeterBuilder createTestMeter() {
        // Sample data , committing to DataStore

        MeterKey key = new MeterKey(new MeterId(Uint32.valueOf(12)));
        MeterBuilder meter = new MeterBuilder();
        meter.setContainerName("abcd");
        meter.withKey(key);
        meter.setMeterId(new MeterId(Uint32.valueOf(9)));
        meter.setMeterName(originalMeterName);
        meter.setFlags(new MeterFlags(true, false, false, false));
        MeterBandHeaderBuilder bandHeader = new MeterBandHeaderBuilder();
        bandHeader.setBandRate(Uint32.valueOf(234));
        bandHeader.setBandBurstSize(Uint32.valueOf(444));
        DscpRemarkBuilder dscpRemark = new DscpRemarkBuilder();
        dscpRemark.setDscpRemarkBurstSize(Uint32.valueOf(5));
        dscpRemark.setPrecLevel(Uint8.ONE);
        dscpRemark.setDscpRemarkRate(Uint32.valueOf(12));
        bandHeader.setBandType(dscpRemark.build());
        MeterBandTypesBuilder bandTypes = new MeterBandTypesBuilder();
        MeterBandType bandType = new MeterBandType(false, true, false);
        bandTypes.setFlags(bandType);
        bandHeader.setMeterBandTypes(bandTypes.build());
        bandHeader.setBandId(new BandId(Uint32.ZERO));

        List<MeterBandHeader> bandHdr = new ArrayList<>();
        bandHdr.add(bandHeader.build());

        MeterBandHeadersBuilder bandHeaders = new MeterBandHeadersBuilder();
        bandHeaders.setMeterBandHeader(bandHdr);
        meter.setMeterBandHeaders(bandHeaders.build());

        testMeter = meter.build();
        return meter;
    }

    private MeterBuilder createTestMeters(String s1, String s2) {
        // Sample data , committing to DataStore
        MeterKey key = new MeterKey(new MeterId(Uint32.valueOf(s1)));
        MeterBuilder meter = new MeterBuilder();
        meter.setContainerName("abcd");
        meter.withKey(key);
        meter.setMeterId(new MeterId(Uint32.valueOf(9)));
        MeterBandHeaderBuilder bandHeader = new MeterBandHeaderBuilder();
        if (s2.equalsIgnoreCase("modify")) {
            meter.setMeterName(updatedMeterName);
            bandHeader.setBandRate(Uint32.valueOf(234));
        } else {
            meter.setMeterName(originalMeterName);
            bandHeader.setBandRate(Uint32.valueOf(123));
        }
        meter.setFlags(new MeterFlags(true, false, false, false));

        bandHeader.setBandBurstSize(Uint32.valueOf(444));
        DscpRemarkBuilder dscpRemark = new DscpRemarkBuilder();
        dscpRemark.setDscpRemarkBurstSize(Uint32.valueOf(5));
        dscpRemark.setPrecLevel(Uint8.ONE);
        dscpRemark.setDscpRemarkRate(Uint32.valueOf(12));
        bandHeader.setBandType(dscpRemark.build());
        MeterBandTypesBuilder bandTypes = new MeterBandTypesBuilder();
        MeterBandType bandType = new MeterBandType(false, true, false);
        bandTypes.setFlags(bandType);
        bandHeader.setMeterBandTypes(bandTypes.build());
        bandHeader.setBandId(new BandId(Uint32.ZERO));

        List<MeterBandHeader> bandHdr = new ArrayList<>();
        bandHdr.add(bandHeader.build());

        MeterBandHeadersBuilder bandHeaders = new MeterBandHeadersBuilder();
        bandHeaders.setMeterBandHeader(bandHdr);
        meter.setMeterBandHeaders(bandHeaders.build());

        int firstInt = Integer.parseInt(s1);
        switch (firstInt) {
            case 1:
                testMeter1 = meter.build();
                break;
            case 2:
                testMeter2 = meter.build();
                break;
            case 3:
                testMeter1 = meter.build();
                break;
            case 4:
                testMeter2 = meter.build();
                break;
            case 5:
                testMeter1 = meter.build();
                break;
            case 6:
                testMeter2 = meter.build();
                break;
            case 7:
                testMeter1 = meter.build();
                break;
            case 8:
                testMeter2 = meter.build();
                break;
            default:
                // No-op?
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
        createTestMeter();
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(testMeter.getMeterId()));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
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

        int count = Integer.parseInt(ci.nextArgument());

        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        switch (count) {
            case 1:
                createTestMeters("1", "remove");
                InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
                createTestMeters("2", "remove");
                InstanceIdentifier<Meter> path2 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path2);

                break;
            case 2:
                createTestMeters("3", "remove");
                InstanceIdentifier<Meter> path3 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path3);
                createTestMeters("4", "remove");
                InstanceIdentifier<Meter> path4 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path4);
                break;
            case 3:
                createTestMeters("5", "remove");
                InstanceIdentifier<Meter> path5 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path5);
                createTestMeters("6", "remove");
                InstanceIdentifier<Meter> path6 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path6);
                break;
            case 4:
                createTestMeters("7", "remove");
                InstanceIdentifier<Meter> path7 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path7);
                createTestMeters("8", "remove");
                InstanceIdentifier<Meter> path8 = InstanceIdentifier.create(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()));
                modification.delete(LogicalDatastoreType.CONFIGURATION, path8);
                break;
            default:
                break;
        }

        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
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
        int count = Integer.parseInt(ci.nextArgument());
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
            default:
                break;
        }
        // createTestMeters();
        // writeMeter(ci, testMeter);
    }

    private void writeMeter(final CommandInterpreter ci, Meter meter) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, meter);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    private void writeMeter(final CommandInterpreter ci, Meter meter, Meter meter1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, meter);
        InstanceIdentifier<Meter> path2 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter1.getMeterId()));
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path2, meter1);

        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
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

        int count = Integer.parseInt(ci.nextArgument());
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
            default:
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
