/*
 * Copyright (c) 2014, 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WildcardsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.match.MatchSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.TablesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.TablesMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.wildcards.WildcardSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = CommandProvider.class, immediate = true)
public final class OpenflowpluginTableFeaturesTestCommandProvider implements CommandProvider {
    private final DataBroker dataBroker;
    private Node testNode;

    @Inject
    @Activate
    public OpenflowpluginTableFeaturesTestCommandProvider(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
        // createTestNode();
        // createTestTableFeatures();
    }

    private void createUserNode(final String nodeRef) {
        testNode = new NodeBuilder().setId(new NodeId(nodeRef)).build();
    }

    private void createTestNode() {
        testNode = new NodeBuilder().setId(new NodeId(OpenflowpluginTestActivator.NODE_ID)).build();
    }

    private static @NonNull InstanceIdentifier<Node> nodeToInstanceId(final Node node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.key());
    }

    private static TableFeaturesBuilder createTestTableFeatures(final String tableFeatureTypeArg) {

        String tableFeatureType = tableFeatureTypeArg;
        if (tableFeatureType == null) {
            tableFeatureType = "t1";
        }

        final TableFeaturesBuilder tableFeature = new TableFeaturesBuilder();
        // Sample data , committing to DataStore
        if (!tableFeatureType.equals("t1")) {
            final BindingMap.Builder<TableFeaturePropertiesKey, TableFeatureProperties> properties =
                BindingMap.orderedBuilder();
            switch (tableFeatureType) {
                case "t2":
                    //To set the ApplyActionsMiss
                    properties.add(createApplyActionsMissTblFeatureProp().build());
                    break;
                case "t3":
                    // To set the Next Table
                    properties.add(createNextTblFeatureProp().build());
                    break;
                case "t4":
                    // To set the Next Table Miss
                    properties.add(createNextTableMissTblFeatureProp().build());
                    break;
                case "t5":
                    //To set the ApplyActions
                    properties.add(createApplyActionsTblFeatureProp().build());
                    break;
                case "t6":
                    // To set the instructions
                    properties.add(createInstructionsTblFeatureProp().build());
                    break;
                case "t7":
                    // To set the instructions miss
                    properties.add(createInstructionsMissTblFeatureProp().build());
                    break;
                case "t8":
                    // To set the write actions
                    properties.add(createWriteActionsTblFeatureProp().build());
                    break;
                case "t9":
                    // To set the write actions miss
                    properties.add(createWriteActionsMissTblFeatureProp().build());
                    break;
                case "t10":
                    // To set the match field
                    properties.add(createMatchFieldTblFeatureProp().build());
                    break;
                case "t11":
                    // To set the write set-field
                    properties.add(createWriteSetFieldTblFeatureProp().build());
                    break;
                case "t12":
                    // To set the write set-field miss
                    properties.add(createWriteSetFieldMissTblFeatureProp().build());
                    break;
                case "t13":
                    // To set the apply set field
                    properties.add(createApplySetFieldTblFeatureProp().build());
                    break;
                case "t14":
                    // To set the apply set field miss
                    properties.add(createApplySetFieldMissTblFeatureProp().build());
                    break;
                case "t15":
                    // To set the wildcards set field match
                    properties.add(createWildCardsTblFeatureProp().build());
                    break;
                default:
                    break;
            }

            tableFeature
                .setTableId(Uint8.ZERO)
                .setName("Table 0")
                .setMetadataMatch(Uint64.TEN)
                .setMetadataWrite(Uint64.TEN)
                .setMaxEntries(Uint32.valueOf(10000))
                .setConfig(new TableConfig(false))
                .setTableProperties(new TablePropertiesBuilder().setTableFeatureProperties(properties.build()).build());
        }
        return tableFeature;
    }

    private static TableFeaturePropertiesBuilder createApplyActionsMissTblFeatureProp() {
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder()
                    .setApplyActionsMiss(new ApplyActionsMissBuilder()
                        .setAction(BindingMap.of(new ActionBuilder()
                            .setAction(new PopMplsActionCaseBuilder().build())
                            .build()))
                        .build())
                    .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createApplyActionsTblFeatureProp() {
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .feature.prop.type.table.feature.prop.type.ApplyActionsBuilder()
                .setApplyActions(new ApplyActionsBuilder()
                    .setAction(BindingMap.of(new ActionBuilder()
                        .setAction(new PopMplsActionCaseBuilder().build())
                        .build()))
                    .build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createNextTblFeatureProp() {
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new NextTableBuilder()
                .setTables(new TablesBuilder().setTableIds(List.of(Uint8.TWO, Uint8.valueOf(3))).build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createNextTableMissTblFeatureProp() {
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new NextTableMissBuilder()
                .setTablesMiss(new TablesMissBuilder()
                    .setTableIds(List.of(Uint8.valueOf(23), Uint8.valueOf(24), Uint8.valueOf(25),
                        Uint8.valueOf(27), Uint8.valueOf(28), Uint8.valueOf(29), Uint8.valueOf(30)))
                    .build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createInstructionsTblFeatureProp() {
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .feature.prop.type.table.feature.prop.type.InstructionsBuilder()
                    .setInstructions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                        .feature.prop.type.table.feature.prop.type.instructions.InstructionsBuilder().build())
                    .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createInstructionsMissTblFeatureProp() {
        // To set the instructions miss -- "t7"

        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .feature.prop.type.table.feature.prop.type.InstructionsMissBuilder()
                    .setInstructionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                        .feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder().build())
                    .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createWriteActionsTblFeatureProp() {
        // t8

        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .feature.prop.type.table.feature.prop.type.WriteActionsBuilder()
                    .setWriteActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                        .feature.prop.type.table.feature.prop.type.write.actions.WriteActionsBuilder()
                            .setAction(BindingMap.ordered(
                                new ActionBuilder().setAction(new CopyTtlOutCaseBuilder().build()).build(),
                                new ActionBuilder().setAction(new PopVlanActionCaseBuilder().build()).build()))
                        .build())
                    .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createWriteActionsMissTblFeatureProp() {
        // t9
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder()
                    .setWriteActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                        .feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMissBuilder()
                            .setAction(BindingMap.ordered(
                                new ActionBuilder().setAction(new CopyTtlInCaseBuilder().build()).build(),
                                new ActionBuilder().setAction(new PushPbbActionCaseBuilder().build()).build()))
                            .build())
                    .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createMatchFieldTblFeatureProp() {
        //t10
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new MatchBuilder()
                .setMatchSetfield(new MatchSetfieldBuilder()
                    .setSetFieldMatch(BindingMap.of(new SetFieldMatchBuilder()
                        .setHasMask(false)
                        .setMatchType(MplsLabel.VALUE)
                        .build()))
                    .build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createWriteSetFieldTblFeatureProp() {
        //t11
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new WriteSetfieldBuilder()
                .setWriteSetfield(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                    .prop.type.table.feature.prop.type.write.setfield.WriteSetfieldBuilder()
                        .setSetFieldMatch(BindingMap.ordered(
                            new SetFieldMatchBuilder().setHasMask(false).setMatchType(MplsLabel.VALUE).build(),
                            new SetFieldMatchBuilder().setHasMask(true).setMatchType(MplsBos.VALUE).build(),
                            new SetFieldMatchBuilder().setHasMask(true).setMatchType(EthDst.VALUE).build()))
                        .build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createWriteSetFieldMissTblFeatureProp() {
        // t12
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new WriteSetfieldMissBuilder()
            .setWriteSetfieldMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                .prop.type.table.feature.prop.type.write.setfield.miss.WriteSetfieldMissBuilder()
                    .setSetFieldMatch(BindingMap.ordered(
                        new SetFieldMatchBuilder().setHasMask(false).setMatchType(EthSrc.VALUE).build(),
                        new SetFieldMatchBuilder().setHasMask(true).setMatchType(InPort.VALUE).build(),
                        new SetFieldMatchBuilder().setHasMask(true).setMatchType(Ipv4Dst.VALUE).build()))
                    .build())
            .build())
            .withKey(new TableFeaturePropertiesKey(0)).setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createApplySetFieldTblFeatureProp() {
        //t13
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new ApplySetfieldBuilder()
                .setApplySetfield(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                    .prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder()
                        .setSetFieldMatch(BindingMap.ordered(
                            new SetFieldMatchBuilder().setHasMask(false).setMatchType(ArpOp.VALUE).build(),
                            new SetFieldMatchBuilder().setHasMask(true).setMatchType(InPort.VALUE).build(),
                            new SetFieldMatchBuilder().setHasMask(true).setMatchType(Ipv4Dst.VALUE).build()))
                        .build())
            .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createApplySetFieldMissTblFeatureProp() {
        //t14
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new ApplySetfieldMissBuilder()
                .setApplySetfieldMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                    .feature.prop.type.table.feature.prop.type.apply.setfield.miss.ApplySetfieldMissBuilder()
                        .setSetFieldMatch(BindingMap.of(
                            new SetFieldMatchBuilder().setHasMask(false).setMatchType(ArpOp.VALUE).build(),
                            new SetFieldMatchBuilder().setHasMask(true).setMatchType(InPort.VALUE).build(),
                            new SetFieldMatchBuilder().setHasMask(true).setMatchType(Ipv4Dst.VALUE).build()))
                        .build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private static TableFeaturePropertiesBuilder createWildCardsTblFeatureProp() {
        //t15
        return new TableFeaturePropertiesBuilder()
            .setTableFeaturePropType(new WildcardsBuilder()
                .setWildcardSetfield(new WildcardSetfieldBuilder()
                    .setSetFieldMatch(BindingMap.ordered(
                        new SetFieldMatchBuilder().setHasMask(false).setMatchType(ArpOp.VALUE).build(),
                        new SetFieldMatchBuilder().setHasMask(true).setMatchType(InPort.VALUE).build()))
                    .build())
                .build())
            .withKey(new TableFeaturePropertiesKey(0))
            .setOrder(1);
    }

    private void writeTableFeatures(final CommandInterpreter ci, final TableFeatures tableFeatures) {
        ReadWriteTransaction modification = requireNonNull(dataBroker).newReadWriteTransaction();

        KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                        .child(TableFeatures.class, new TableFeaturesKey(tableFeatures.getTableId()));

        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, nodeToInstanceId(testNode), testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, path1, tableFeatures);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, tableFeatures);
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
    public void _modifyTable(final CommandInterpreter ci) {
        String nref = ci.nextArgument();
        ci.println(" Table Command Provider modify");

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        String tableFeatureType = ci.nextArgument();
        TableFeaturesBuilder tableFeaturesBld = createTestTableFeatures(tableFeatureType);

        writeTableFeatures(ci, tableFeaturesBld.build());
    }

    @Override
    public String getHelp() {
        return new StringBuilder()
            .append("---FRM MD-SAL Table test module---\n")
            .append("\t modifyTable <node id>        - node ref\n")
            .toString();
    }
}

