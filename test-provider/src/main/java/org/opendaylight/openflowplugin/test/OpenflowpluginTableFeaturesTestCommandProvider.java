/**
 * Copyright (c) 2014, 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.osgi.framework.BundleContext;


public class OpenflowpluginTableFeaturesTestCommandProvider implements CommandProvider {

    private DataBroker dataBroker;
    private ProviderContext pc;
    private final BundleContext ctx;
    private TableFeatures testTable;
    private Node testNode;
    private final String originalTableName = "Foo";
    private final String updatedTableName = "Bar";

    public OpenflowpluginTableFeaturesTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBroker = session.getSALService(DataBroker.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        // createTestNode();
        // createTestTableFeatures();
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

    private TableFeaturesBuilder createTestTableFeatures(String tableFeatureTypeArg) {

        String tableFeatureType = tableFeatureTypeArg;
        if (tableFeatureType == null) {
            tableFeatureType = "t1";
        }

        final TableFeaturesBuilder tableFeature = new TableFeaturesBuilder();
        // Sample data , committing to DataStore
        if (!tableFeatureType.equals("t1")) {


            tableFeature.setTableId((short) 0);
            tableFeature.setName("Table 0");


            tableFeature.setMetadataMatch(BigInteger.valueOf(10));
            tableFeature.setMetadataWrite(BigInteger.valueOf(10));
            tableFeature.setMaxEntries(10000L);

            tableFeature.setConfig(new TableConfig(false));

            List<TableFeatureProperties> properties = new ArrayList<TableFeatureProperties>();


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
            }


            TablePropertiesBuilder propertyBld = new TablePropertiesBuilder();
            propertyBld.setTableFeatureProperties(properties);
            tableFeature.setTableProperties(propertyBld.build());
        }
        testTable = tableFeature.build();
        return tableFeature;
    }

    private TableFeaturePropertiesBuilder createApplyActionsMissTblFeatureProp() {
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        //To set the ApplyActionsMiss
        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        ab.setAction(new PopMplsActionCaseBuilder().build());
        actionList.add(ab.build());

        tableFeatureProperty.setTableFeaturePropType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder()
                        .setApplyActionsMiss(new ApplyActionsMissBuilder().setAction(actionList).build()).build());

        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;

    }

    private TableFeaturePropertiesBuilder createApplyActionsTblFeatureProp() {
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();
        List<Action> actionListt5 = new ArrayList<Action>();
        ActionBuilder abt5 = new ActionBuilder();

        abt5.setAction(new PopMplsActionCaseBuilder().build());
        actionListt5.add(abt5.build());

        tableFeatureProperty.setTableFeaturePropType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder()
                        .setApplyActions(new ApplyActionsBuilder().setAction(actionListt5).build()).build());

        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createNextTblFeatureProp() {
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();
        List<Short> nextTblIds = Arrays.asList(new Short[]{2, 3});
        NextTableBuilder nextTblBld = new NextTableBuilder();

        nextTblBld.setTables(new TablesBuilder().setTableIds(nextTblIds).build());
        tableFeatureProperty.setTableFeaturePropType(nextTblBld.build());

        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createNextTableMissTblFeatureProp() {
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();
        List<Short> nextTblMissIds = Arrays.asList(new Short[]{23, 24, 25, 27, 28, 29, 30});
        NextTableMissBuilder nextTblMissBld = new NextTableMissBuilder();

        nextTblMissBld.setTablesMiss(new TablesMissBuilder().setTableIds(nextTblMissIds).build());
        tableFeatureProperty.setTableFeaturePropType(nextTblMissBld.build());

        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }


    private TableFeaturePropertiesBuilder createInstructionsTblFeatureProp() {
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<Instruction> instLst = new ArrayList<Instruction>();
        InstructionBuilder ib = new InstructionBuilder();
        ib.setOrder(1);
        ib.setInstruction(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder().build());
        ib.setKey(new InstructionKey(1));

        tableFeatureProperty.setTableFeaturePropType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder().setInstructions(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.InstructionsBuilder().setInstruction(instLst).build()).build());


        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createInstructionsMissTblFeatureProp() {
        // To set the instructions miss -- "t7"
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<Instruction> instLst = new ArrayList<Instruction>();
        InstructionBuilder ib1 = new InstructionBuilder();
        ib1.setOrder(1);
        ib1.setInstruction(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder().build());
        ib1.setKey(new InstructionKey(1));

        InstructionBuilder ib2 = new InstructionBuilder();
        ib2.setOrder(2);
        ib2.setInstruction(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder().build());
        ib2.setKey(new InstructionKey(2));

        tableFeatureProperty.setTableFeaturePropType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder().setInstructionsMiss(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder().setInstruction(instLst).build()).build());


        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createWriteActionsTblFeatureProp() {
        // t8
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<Action> actionList = new ArrayList<Action>();

        ActionBuilder abt1 = new ActionBuilder();
        abt1.setAction(new CopyTtlOutCaseBuilder().build());
        actionList.add(abt1.build());

        ActionBuilder abt2 = new ActionBuilder();
        abt2.setAction(new PopVlanActionCaseBuilder().build());
        actionList.add(abt2.build());

        tableFeatureProperty.setTableFeaturePropType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder()
                        .setWriteActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.WriteActionsBuilder().setAction(actionList).build()).build());

        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createWriteActionsMissTblFeatureProp() {
        // t9
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<Action> actionList = new ArrayList<Action>();

        ActionBuilder abt1 = new ActionBuilder();
        abt1.setAction(new CopyTtlInCaseBuilder().build());
        actionList.add(abt1.build());

        ActionBuilder abt2 = new ActionBuilder();
        abt2.setAction(new PushPbbActionCaseBuilder().build());
        actionList.add(abt2.build());

        tableFeatureProperty.setTableFeaturePropType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder()
                        .setWriteActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMissBuilder().setAction(actionList).build()).build());

        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createMatchFieldTblFeatureProp() {
        //t10
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();


        List<SetFieldMatch> setFieldMatch = new ArrayList<SetFieldMatch>();
        SetFieldMatchBuilder setFieldMatchBld = new SetFieldMatchBuilder();
        setFieldMatchBld.setHasMask(false);
        setFieldMatchBld.setMatchType(MplsLabel.class);


        setFieldMatch.add(setFieldMatchBld.build());
        MatchBuilder matchBld = new MatchBuilder();
        MatchSetfieldBuilder matchSetfieldBld = new MatchSetfieldBuilder();
        matchSetfieldBld.setSetFieldMatch(setFieldMatch);
        matchBld.setMatchSetfield(matchSetfieldBld.build());

        tableFeatureProperty.setTableFeaturePropType(matchBld.build());
        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }


    private TableFeaturePropertiesBuilder createWriteSetFieldTblFeatureProp() {
        //t11
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<SetFieldMatch> setFieldMatch = new ArrayList<SetFieldMatch>();
        SetFieldMatchBuilder setFieldMatchBld1 = new SetFieldMatchBuilder();
        setFieldMatchBld1.setHasMask(false);
        setFieldMatchBld1.setMatchType(MplsLabel.class);

        SetFieldMatchBuilder setFieldMatchBld2 = new SetFieldMatchBuilder();
        setFieldMatchBld2.setHasMask(true);
        setFieldMatchBld2.setMatchType(MplsBos.class);

        SetFieldMatchBuilder setFieldMatchBld3 = new SetFieldMatchBuilder();
        setFieldMatchBld3.setHasMask(true);
        setFieldMatchBld3.setMatchType(EthDst.class);

        setFieldMatch.add(setFieldMatchBld1.build());
        setFieldMatch.add(setFieldMatchBld2.build());
        setFieldMatch.add(setFieldMatchBld3.build());

        WriteSetfieldBuilder writeSetfieldBld = new WriteSetfieldBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.WriteSetfieldBuilder writeSetfieldBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.WriteSetfieldBuilder();
        writeSetfieldBuilder.setSetFieldMatch(setFieldMatch);
        writeSetfieldBld.setWriteSetfield(writeSetfieldBuilder.build());


        tableFeatureProperty.setTableFeaturePropType(writeSetfieldBld.build());
        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createWriteSetFieldMissTblFeatureProp() {
        // t12
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<SetFieldMatch> setFieldMatch = new ArrayList<SetFieldMatch>();
        SetFieldMatchBuilder setFieldMatchBld1 = new SetFieldMatchBuilder();
        setFieldMatchBld1.setHasMask(false);
        setFieldMatchBld1.setMatchType(EthSrc.class);

        SetFieldMatchBuilder setFieldMatchBld2 = new SetFieldMatchBuilder();
        setFieldMatchBld2.setHasMask(true);
        setFieldMatchBld2.setMatchType(InPort.class);

        SetFieldMatchBuilder setFieldMatchBld3 = new SetFieldMatchBuilder();
        setFieldMatchBld3.setHasMask(true);
        setFieldMatchBld3.setMatchType(Ipv4Dst.class);

        setFieldMatch.add(setFieldMatchBld1.build());
        setFieldMatch.add(setFieldMatchBld2.build());
        setFieldMatch.add(setFieldMatchBld3.build());

        WriteSetfieldMissBuilder writeSetfieldBld = new WriteSetfieldMissBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.miss.WriteSetfieldMissBuilder writeSetfieldMissBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.miss.WriteSetfieldMissBuilder();
        writeSetfieldMissBuilder.setSetFieldMatch(setFieldMatch);
        writeSetfieldBld.setWriteSetfieldMiss(writeSetfieldMissBuilder.build());

        tableFeatureProperty.setTableFeaturePropType(writeSetfieldBld.build());
        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createApplySetFieldTblFeatureProp() {
        //t13
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<SetFieldMatch> setFieldMatch = new ArrayList<SetFieldMatch>();
        SetFieldMatchBuilder setFieldMatchBld1 = new SetFieldMatchBuilder();
        setFieldMatchBld1.setHasMask(false);
        setFieldMatchBld1.setMatchType(ArpOp.class);

        SetFieldMatchBuilder setFieldMatchBld2 = new SetFieldMatchBuilder();
        setFieldMatchBld2.setHasMask(true);
        setFieldMatchBld2.setMatchType(InPort.class);

        SetFieldMatchBuilder setFieldMatchBld3 = new SetFieldMatchBuilder();
        setFieldMatchBld3.setHasMask(true);
        setFieldMatchBld3.setMatchType(Ipv4Dst.class);

        setFieldMatch.add(setFieldMatchBld1.build());
        setFieldMatch.add(setFieldMatchBld2.build());
        setFieldMatch.add(setFieldMatchBld3.build());

        ApplySetfieldBuilder applySetfieldBld = new ApplySetfieldBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder applySetfieldBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder();
        applySetfieldBuilder.setSetFieldMatch(setFieldMatch);
        applySetfieldBld.setApplySetfield(applySetfieldBuilder.build());

        tableFeatureProperty.setTableFeaturePropType(applySetfieldBld.build());
        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createApplySetFieldMissTblFeatureProp() {
        //t14
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<SetFieldMatch> setFieldMatch = new ArrayList<SetFieldMatch>();
        SetFieldMatchBuilder setFieldMatchBld1 = new SetFieldMatchBuilder();
        setFieldMatchBld1.setHasMask(false);
        setFieldMatchBld1.setMatchType(ArpOp.class);

        SetFieldMatchBuilder setFieldMatchBld2 = new SetFieldMatchBuilder();
        setFieldMatchBld2.setHasMask(true);
        setFieldMatchBld2.setMatchType(InPort.class);

        SetFieldMatchBuilder setFieldMatchBld3 = new SetFieldMatchBuilder();
        setFieldMatchBld3.setHasMask(true);
        setFieldMatchBld3.setMatchType(Ipv4Dst.class);

        setFieldMatch.add(setFieldMatchBld1.build());
        setFieldMatch.add(setFieldMatchBld2.build());
        setFieldMatch.add(setFieldMatchBld3.build());

        ApplySetfieldMissBuilder applySetfieldMissBld = new ApplySetfieldMissBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.miss.ApplySetfieldMissBuilder applySetfieldMissBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.miss.ApplySetfieldMissBuilder();
        applySetfieldMissBuilder.setSetFieldMatch(setFieldMatch);
        applySetfieldMissBld.setApplySetfieldMiss(applySetfieldMissBuilder.build());

        tableFeatureProperty.setTableFeaturePropType(applySetfieldMissBld.build());
        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }

    private TableFeaturePropertiesBuilder createWildCardsTblFeatureProp() {
        //t15
        TableFeaturePropertiesBuilder tableFeatureProperty = new TableFeaturePropertiesBuilder();

        List<SetFieldMatch> setFieldMatch = new ArrayList<SetFieldMatch>();
        SetFieldMatchBuilder setFieldMatchBld1 = new SetFieldMatchBuilder();
        setFieldMatchBld1.setHasMask(false);
        setFieldMatchBld1.setMatchType(ArpOp.class);

        SetFieldMatchBuilder setFieldMatchBld2 = new SetFieldMatchBuilder();
        setFieldMatchBld2.setHasMask(true);
        setFieldMatchBld2.setMatchType(InPort.class);


        setFieldMatch.add(setFieldMatchBld1.build());
        setFieldMatch.add(setFieldMatchBld2.build());

        WildcardsBuilder wildCardsBld = new WildcardsBuilder();
        WildcardSetfieldBuilder wildcardsBuilder =
                new WildcardSetfieldBuilder();
        wildcardsBuilder.setSetFieldMatch(setFieldMatch);
        wildCardsBld.setWildcardSetfield(wildcardsBuilder.build());

        tableFeatureProperty.setTableFeaturePropType(wildCardsBld.build());
        TableFeaturePropertiesKey keyValue = new TableFeaturePropertiesKey(0);
        tableFeatureProperty.setKey(keyValue);
        tableFeatureProperty.setOrder(1);

        return tableFeatureProperty;
    }


    private void writeTableFeatures(final CommandInterpreter ci, TableFeatures tableFeatures) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();

        KeyedInstanceIdentifier<TableFeatures, TableFeaturesKey> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class).
                        child(TableFeatures.class, new TableFeaturesKey(tableFeatures.getTableId()));


        modification.merge(LogicalDatastoreType.OPERATIONAL, nodeToInstanceId(testNode), testNode, true);
        modification.merge(LogicalDatastoreType.OPERATIONAL, path1, tableFeatures, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode), testNode, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, tableFeatures, true);
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

    public void _modifyTable(CommandInterpreter ci) {
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
        StringBuilder help = new StringBuilder();
        help.append("---FRM MD-SAL Table test module---\n");
        help.append("\t modifyTable <node id>        - node ref\n");

        return help.toString();
    }

}

