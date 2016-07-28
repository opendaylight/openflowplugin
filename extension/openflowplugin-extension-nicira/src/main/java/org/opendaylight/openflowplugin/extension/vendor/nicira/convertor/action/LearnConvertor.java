/*
 * Copyright (c) 2016 Hewlett-Packard Enterprise and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.NxActionLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionLearnGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.FlowModSpec;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionLearnNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionLearnNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionLearnNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionLearnNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.NxLearn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.NxLearnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.nx.learn.FlowMods;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.learn.grouping.nx.learn.FlowModsBuilder;

import com.google.common.base.Preconditions;

/**
 * @author Slava Radune
 */

public class LearnConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {
    
    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(final Action input, final ActionPath path) {
        NxActionLearn action = ((ActionLearn) input.getActionChoice()).getNxActionLearn();
        NxLearnBuilder builder = new NxLearnBuilder();
        builder.setFlags(action.getFlags());
        builder.setCookie(action.getCookie());
        builder.setFinHardTimeout(action.getFinHardTimeout());
        builder.setFinIdleTimeout(action.getFinIdleTimeout());
        builder.setHardTimeout(action.getHardTimeout());
        builder.setIdleTimeout(action.getIdleTimeout());   
        builder.setPriority(action.getPriority());
        builder.setTableId(action.getTableId());
        builder.setFlowMods(getFlowMods(action));
        return resolveAction(builder.build(), path);
    }


    private List<FlowMods> getFlowMods(NxActionLearn action) {
        if(action.getFlowMods() == null){
            return null;
        }
        
        List<FlowMods> flowMods = new ArrayList<FlowMods>();
        for(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods flowMod : action.getFlowMods()){
            FlowModsBuilder flowModBuilder = new FlowModsBuilder();
            FlowModSpec flowModSpec = buildFlowModSpec(flowMod.getFlowModSpec());
            flowModBuilder.setFlowModSpec(flowModSpec);
            flowMods.add(flowModBuilder.build());
        }
        return flowMods;
        
    }


    private FlowModSpec buildFlowModSpec(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec flowModSpec) {
        if(flowModSpec instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromField flowModAdd2 = 
                    ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCase) flowModSpec).getFlowModAddMatchFromField(); 
            FlowModAddMatchFromFieldBuilder flowModAdd = new FlowModAddMatchFromFieldBuilder();
            flowModAdd.setDstField(flowModAdd2.getDstField());
            flowModAdd.setSrcField(flowModAdd2.getSrcField());
            flowModAdd.setDstOfs(flowModAdd2.getDstOfs());
            flowModAdd.setSrcOfs(flowModAdd2.getSrcOfs());
            flowModAdd.setFlowModHeaderLen(flowModAdd2.getFlowModHeaderLen());
            FlowModAddMatchFromFieldCaseBuilder caseBuilder = new FlowModAddMatchFromFieldCaseBuilder();
            caseBuilder.setFlowModAddMatchFromField(flowModAdd.build());
            return caseBuilder.build();
        }
        return null;
    }


    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(final NxLearn value, final ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new NxActionLearnNodesNodeTableFlowWriteActionsCaseBuilder().setNxLearn(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new NxActionLearnNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxLearn(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new NxActionLearnNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxLearn(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new NxActionLearnNotifGroupDescStatsUpdatedCaseBuilder().setNxLearn(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionLearnGrouping);
        NxActionLearnGrouping nxAction = (NxActionLearnGrouping) nxActionArg;

        NxActionLearnBuilder nxActionLearnBuilder = new NxActionLearnBuilder();
        nxActionLearnBuilder.setFlags(nxAction.getNxLearn().getFlags());
        nxActionLearnBuilder.setCookie(nxAction.getNxLearn().getCookie());
        nxActionLearnBuilder.setFinHardTimeout(nxAction.getNxLearn().getFinHardTimeout());
        nxActionLearnBuilder.setFinIdleTimeout(nxAction.getNxLearn().getFinIdleTimeout());
        nxActionLearnBuilder.setHardTimeout(nxAction.getNxLearn().getHardTimeout());
        nxActionLearnBuilder.setIdleTimeout(nxAction.getNxLearn().getIdleTimeout());   
        nxActionLearnBuilder.setPriority(nxAction.getNxLearn().getPriority());
        nxActionLearnBuilder.setTableId(nxAction.getNxLearn().getTableId());
        nxActionLearnBuilder.setFlowMods(getFlowMods(nxAction.getNxLearn()));
        ActionLearnBuilder actionLearnBuilder = new ActionLearnBuilder();
        actionLearnBuilder.setNxActionLearn(nxActionLearnBuilder.build());
        return ActionUtil.createAction(actionLearnBuilder.build());
    }
    
    private List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods> getFlowMods(
            NxLearn nxLearn) {
        if(nxLearn.getFlowMods() == null){
            return null;
        }
        
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowMods> flowMods = new ArrayList<>();
        for(FlowMods flowMod : nxLearn.getFlowMods()){
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder flowModBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.learn.grouping.nx.action.learn.FlowModsBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec flowModSpec = buildFlowModSpec(flowMod.getFlowModSpec());
            flowModBuilder.setFlowModSpec(flowModSpec);
            flowMods.add(flowModBuilder.build());
        }
        return flowMods;
    }


    private org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.FlowModSpec buildFlowModSpec(FlowModSpec flowModSpec) {
        if(flowModSpec instanceof FlowModAddMatchFromFieldCase){
            FlowModAddMatchFromField flowModAdd2 = ((FlowModAddMatchFromFieldCase) flowModSpec).getFlowModAddMatchFromField(); 
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder 
                flowModAdd = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.flow.mod.add.match.from.field._case.FlowModAddMatchFromFieldBuilder();
            flowModAdd.setDstField(flowModAdd2.getDstField());
            flowModAdd.setSrcField(flowModAdd2.getSrcField());
            flowModAdd.setDstOfs(flowModAdd2.getDstOfs());
            flowModAdd.setSrcOfs(flowModAdd2.getSrcOfs());
            flowModAdd.setFlowModHeaderLen(flowModAdd2.getFlowModHeaderLen());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder 
                caseBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.flow.mod.spec.flow.mod.spec.FlowModAddMatchFromFieldCaseBuilder();
            caseBuilder.setFlowModAddMatchFromField(flowModAdd.build());
            return caseBuilder.build();
        }
        return null;
    }

}
