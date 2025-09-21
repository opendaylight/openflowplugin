/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = CommandProvider.class, immediate = true)
public final class OpenflowpluginMeterTestCommandProvider implements CommandProvider {
    private static final String ORIGINAL_METER_NAME = "Foo";
    private static final String UPDATED_METER_NAME = "Bar";

    private final DataBroker dataBroker;
    private Meter testMeter;
    private Meter testMeter1;
    private Meter testMeter2;
    private Node testNode;

    @Inject
    @Activate
    public OpenflowpluginMeterTestCommandProvider(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
        createTestNode();
        createTestMeter();
    }

    private void createUserNode(final String nodeRef) {
        testNode = new NodeBuilder().setId(new NodeId(nodeRef)).build();
    }

    private void createTestNode() {
        testNode = new NodeBuilder().setId(new NodeId(OpenflowpluginTestActivator.NODE_ID)).build();
    }

    private static @NonNull DataObjectIdentifier<Node> nodeToInstanceId(final Node node) {
        return DataObjectIdentifier.builder(Nodes.class).child(Node.class, node.key()).build();
    }

    private MeterBuilder createTestMeter() {
        // Sample data , committing to DataStore
        final MeterBuilder meter = new MeterBuilder()
            .setContainerName("abcd")
            .withKey(new MeterKey(new MeterId(Uint32.valueOf(12))))
            .setMeterId(new MeterId(Uint32.valueOf(9)))
            .setMeterName(ORIGINAL_METER_NAME)
            .setFlags(new MeterFlags(true, false, false, false))
            .setMeterBandHeaders(new MeterBandHeadersBuilder()
                .setMeterBandHeader(BindingMap.of(new MeterBandHeaderBuilder()
                    .setBandRate(Uint32.valueOf(234))
                    .setBandBurstSize(Uint32.valueOf(444))
                    .setBandType(new DscpRemarkBuilder()
                        .setDscpRemarkBurstSize(Uint32.valueOf(5))
                        .setPrecLevel(Uint8.ONE)
                        .setDscpRemarkRate(Uint32.valueOf(12))
                        .build())
                    .setMeterBandTypes(new MeterBandTypesBuilder()
                        .setFlags(new MeterBandType(false, true, false))
                        .build())
                    .setBandId(new BandId(Uint32.ZERO))
                    .build()))
                .build());

        testMeter = meter.build();
        return meter;
    }

    private MeterBuilder createTestMeters(final String s1, final String s2) {
        // Sample data , committing to DataStore
        final MeterBuilder meter = new MeterBuilder()
            .setContainerName("abcd")
            .withKey(new MeterKey(new MeterId(Uint32.valueOf(s1))))
            .setMeterId(new MeterId(Uint32.valueOf(9)))
            .setFlags(new MeterFlags(true, false, false, false));
        final MeterBandHeaderBuilder bandHeader = new MeterBandHeaderBuilder()
            .setBandBurstSize(Uint32.valueOf(444))
            .setBandType(new DscpRemarkBuilder()
                .setDscpRemarkBurstSize(Uint32.valueOf(5)).setPrecLevel(Uint8.ONE).setDscpRemarkRate(Uint32.valueOf(12))
                .build())
            .setMeterBandTypes(new MeterBandTypesBuilder().setFlags(new MeterBandType(false, true, false)).build())
            .setBandId(new BandId(Uint32.ZERO));

        if (s2.equalsIgnoreCase("modify")) {
            meter.setMeterName(UPDATED_METER_NAME);
            bandHeader.setBandRate(Uint32.valueOf(234));
        } else {
            meter.setMeterName(ORIGINAL_METER_NAME);
            bandHeader.setBandRate(Uint32.valueOf(123));
        }

        meter.setMeterBandHeaders(new MeterBandHeadersBuilder()
            .setMeterBandHeader(BindingMap.of(bandHeader.build()))
            .build());

        int firstInt = Integer.parseInt(s1);
        switch (firstInt) {
            case 1:
            case 3:
            case 5:
            case 7:
                testMeter1 = meter.build();
                break;
            case 2:
            case 4:
            case 6:
            case 8:
                testMeter2 = meter.build();
                break;
            default:
                // No-op?
        }

        return meter;
    }

    @SuppressWarnings("checkstyle:MethodName")
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
        final var path1 = DataObjectIdentifier.builder(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(testMeter.getMeterId()))
                .build();
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    @SuppressWarnings("checkstyle:MethodName")
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
            case 1 -> {
                createTestMeters("1", "remove");
                final var path1 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
                createTestMeters("2", "remove");
                final var path2 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path2);
            }
            case 2 -> {
                createTestMeters("3", "remove");
                final var path3 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path3);
                createTestMeters("4", "remove");
                final var path4 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path4);
            }
            case 3 -> {
                createTestMeters("5", "remove");
                final var path5 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path5);
                createTestMeters("6", "remove");
                final var path6 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path6);
            }
            case 4 -> {
                createTestMeters("7", "remove");
                final var path7 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter1.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path7);
                createTestMeters("8", "remove");
                final var path8 = DataObjectIdentifier.builder(Nodes.class)
                        .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(Meter.class, new MeterKey(testMeter2.getMeterId()))
                        .build();
                modification.delete(LogicalDatastoreType.CONFIGURATION, path8);
            }
            default -> {
                // No-op
            }
        }

        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _addMeter(final CommandInterpreter ci) {
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

    @SuppressWarnings("checkstyle:MethodName")
    public void _addMeters(final CommandInterpreter ci) {
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

    private void writeMeter(final CommandInterpreter ci, final Meter meter) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        final var path1 = DataObjectIdentifier.builder(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()))
                .build();
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, meter);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    private void writeMeter(final CommandInterpreter ci, final Meter meter, final Meter meter1) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        final var path1 = DataObjectIdentifier.builder(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()))
                .build();
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, meter);
        final var path2 = DataObjectIdentifier.builder(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter1.getMeterId()))
                .build();
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path2, meter1);

        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(final CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _modifyMeter(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        MeterBuilder meter = createTestMeter();
        meter.setMeterName(UPDATED_METER_NAME);
        writeMeter(ci, meter.build());
        meter.setMeterName(ORIGINAL_METER_NAME);
        writeMeter(ci, meter.build());
    }

    @SuppressWarnings("checkstyle:MethodName")
    public void _modifyMeters(final CommandInterpreter ci) {
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
