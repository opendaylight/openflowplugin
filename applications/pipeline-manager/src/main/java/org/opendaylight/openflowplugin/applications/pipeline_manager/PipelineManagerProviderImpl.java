/**
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.pipeline_manager;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PipelineManagerProviderImpl implements DataChangeListener, PipelineManager {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineManagerProviderImpl.class);
    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> nodesListener;


    public PipelineManagerProviderImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        final InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.create(Nodes.class);
        nodesListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                nodesIdentifier, this, AsyncDataBroker.DataChangeScope.SUBTREE); // FIXME: Try to listen to the Node changes only instead of subtree
        LOG.info("new Pipeline Manager created: {}", this);
    }

    @Override
    public void close() throws Exception {
        nodesListener.close();
        LOG.info("Pipeline Manager destroyed: {}", this);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Map<InstanceIdentifier<?>, DataObject> createdData = change.getCreatedData();
        for (Map.Entry<InstanceIdentifier<?>, DataObject> entry : createdData.entrySet()) {
            if (entry.getValue() instanceof Node) {
                Node node = (Node) entry.getValue();
                createPipeline(node);
            }
        }
    }

    private void createPipeline(Node node) {
        List<Table> tableList = getTableList(node);
        for (Table table : tableList) {
            List<Short> nextIds = getNextTablesMiss(table);
            if (nextIds.isEmpty())
                break;
            Short nextId = Collections.min(nextIds);
            Short currentId = table.getId();
            addFlowGoto(node, currentId, nextId);
        }
    }

    private void addFlowGoto(Node node, Short currentId, Short nextId) {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setTableId(currentId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setPriority(0);
        flowBuilder.setMatch(new MatchBuilder().build());
        flowBuilder.setInstructions(
                new InstructionsBuilder().setInstruction(Collections.singletonList(
                        new InstructionBuilder().setInstruction(
                                new GoToTableCaseBuilder().setGoToTable(
                                        new GoToTableBuilder().setTableId(nextId).build()
                                ).build()
                        ).setOrder(0).build()
                )).build());
        String flowIdStr = "PipelineManager";
        final FlowId flowId = new FlowId(flowIdStr);
        final FlowKey key = new FlowKey(flowId);
        flowBuilder.setKey(key);

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(node.getId())).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
                .build();

        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.merge(LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);
        transaction.submit();
    }

    private List<TableFeatureProperties> getTableFeatureProperties(Table table) {
        if (table.getTableFeatures().isEmpty())
            return Collections.emptyList();
        TableFeatures tableFeatures = table.getTableFeatures().get(0);
        return tableFeatures.getTableProperties().getTableFeatureProperties();
    }

    private List<Short> getNextTablesMiss(Table table) {
        for (TableFeatureProperties tableFeatureProperties : getTableFeatureProperties(table)) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof NextTableMiss) {
                NextTableMiss nextTableMiss = (NextTableMiss) tableFeatureProperties.getTableFeaturePropType();
                return nextTableMiss.getTablesMiss().getTableIds();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean setTableId(NodeId nodeId, FlowBuilder flowBuilder) {
        List<Table> tableList = getTableList(nodeId);
        for (Table table : tableList) {
            List<TableFeatureProperties> tableFeaturePropertiesList = getTableFeatureProperties(table);
            if (isFlowSupported(tableFeaturePropertiesList, flowBuilder)) {
                flowBuilder.setTableId(table.getId());
                return true;
            }
        }
        return false;
    }

    private List<Table> getTableList(NodeId nodeId) {
        Node node = InventoryDataServiceUtil.getDataObject(dataBroker.newReadOnlyTransaction(),
                InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)));
        return getTableList(node);
    }

    private List<Table> getTableList(Node node) {
        FlowCapableNode flowCapableNode = node.getAugmentation(FlowCapableNode.class);
        List<Table> tableList = flowCapableNode.getTable();
        Collections.sort(tableList, new Comparator<Table>() {
            @Override
            public int compare(Table o1, Table o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return tableList;
    }

    private boolean isFlowSupported(List<TableFeatureProperties> tableFeaturePropertiesList, FlowBuilder flowBuilder) {
        List<SetFieldMatch> matchList = getMatchList(tableFeaturePropertiesList);
        return isMatchSupported(matchList, flowBuilder.getMatch())
                && isInstructionsSupported(tableFeaturePropertiesList, flowBuilder.getInstructions().getInstruction());
    }

    private boolean isInstructionsSupported(List<TableFeatureProperties> tableFeaturePropertiesList, List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            if (!isInstructionSupported(tableFeaturePropertiesList, instruction))
                return false;
        }
        return true;
    }

    private boolean isInstructionSupported(List<TableFeatureProperties> tableFeaturePropertiesList, Instruction instruction) {
        List<Instruction> supportedInstructions = getInstructionList(tableFeaturePropertiesList);
        for (Instruction supportedInstructionProxy : supportedInstructions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction supportedInstruction = supportedInstructionProxy.getInstruction();
            if (instruction.getInstruction().getImplementedInterface().equals(supportedInstruction.getImplementedInterface())) {
                if (instruction.getInstruction() instanceof ApplyActionsCase) {
                    ApplyActionsCase applyActionsCase = (ApplyActionsCase) instruction.getInstruction();
                    List<Action> supportedApplyActions = getApplyActionList(tableFeaturePropertiesList);
                    for (Action action : applyActionsCase.getApplyActions().getAction()) {
                        if (!isActionSupported(supportedApplyActions, action)) {
                            return false;
                        }
                    }
                    if (instruction.getInstruction() instanceof WriteActionsCase) {
                        WriteActionsCase writeActionsCase = (WriteActionsCase) instruction.getInstruction();
                        List<Action> supportedWriteActions = getWriteActionList(tableFeaturePropertiesList);
                        for (Action action : writeActionsCase.getWriteActions().getAction()) {
                            if (!isActionSupported(supportedWriteActions, action)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isActionSupported(List<Action> supportedApplyActions, Action action) {
        for (Action supportedApplyAction : supportedApplyActions) {
            if (supportedApplyAction.getAction().getImplementedInterface().equals(action.getAction().getImplementedInterface()))
                return true;
        }

        return false;
    }

    private boolean isFieldSupported(Class<? extends MatchField> field, List<SetFieldMatch> supportedFields) {
        for (SetFieldMatch supportedField : supportedFields) {
            if (isFieldMatch(field, supportedField.getMatchType()))
                return true;
        }

        return false;
    }

    private boolean isFieldMatch(Class<? extends MatchField> field, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> matchType) {
        return field.getSimpleName().equals(matchType.getSimpleName());
    }

    private boolean isMatchSupported(List<SetFieldMatch> supportedMatchList, Match match) {
        MatchConvertorImpl matchConvertor = new MatchConvertorImpl();
        List<MatchEntry> matchEntryList = matchConvertor.convert(match, null);
        for (MatchEntry matchEntry : matchEntryList) {
            if (!isFieldSupported(matchEntry.getOxmMatchField(), supportedMatchList))
                return false;
        }
        return true;
    }

    private List<SetFieldMatch> getMatchList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match match = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match) tableFeatureProperties.getTableFeaturePropType();
                return match.getMatchSetfield().getSetFieldMatch();
            }
        }
        return Collections.emptyList();
    }

    private List<Instruction> getInstructionList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions instructions = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions) tableFeatureProperties.getTableFeaturePropType();
                return instructions.getInstructions().getInstruction();
            }
        }
        return Collections.emptyList();
    }

    private List<Action> getApplyActionList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions actions = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions) tableFeatureProperties.getTableFeaturePropType();
                return actions.getApplyActions().getAction();
            }
        }
        return Collections.emptyList();
    }

    private List<Action> getWriteActionList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions actions = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions) tableFeatureProperties.getTableFeaturePropType();
                return actions.getWriteActions().getAction();
            }
        }
        return Collections.emptyList();
    }

}
