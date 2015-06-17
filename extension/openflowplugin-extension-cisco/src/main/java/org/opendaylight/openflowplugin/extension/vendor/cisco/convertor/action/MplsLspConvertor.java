/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionMplsLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLsp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.mpls.lsp.grouping.ActionMplsLspBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionMplsLspNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionMplsLspNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionMplsLspNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionMplsLspNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionMplsLspGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.MplsLspName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.mpls.lsp.grouping.ActionMplsLspHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.mpls.lsp.grouping.ActionMplsLspHiBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 10/22/14.
 */
public class MplsLspConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        ActionMplsLsp actionMplsLsp = ((OfjCofActionMplsLsp) input.getActionChoice()).getActionMplsLsp();
        ActionMplsLspHiBuilder actionMplsLspHiBuilder = new ActionMplsLspHiBuilder();
        MplsLspName mplsLspName = new MplsLspName(new String(actionMplsLsp.getName()));
        actionMplsLspHiBuilder.setMplsLspName(mplsLspName);
        return resolveAction(actionMplsLspHiBuilder.build(), path);
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof CofActionMplsLspGrouping);
        CofActionMplsLspGrouping cofActionFcidGrouping = (CofActionMplsLspGrouping) actionCase;
        ActionMplsLspHi actionMplsLspHi = cofActionFcidGrouping.getActionMplsLspHi();

        ActionMplsLspBuilder actionMplsLspBuilder = new ActionMplsLspBuilder();
        actionMplsLspBuilder.setName(actionMplsLspHi.getMplsLspName().getValue().getBytes());
        OfjCofActionMplsLspBuilder builder = new OfjCofActionMplsLspBuilder().setActionMplsLsp(actionMplsLspBuilder.build());
        return ActionUtil.createCiscoAction(builder.build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            ActionMplsLspHi value, ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new CofActionMplsLspNodesNodeTableFlowWriteActionsCaseBuilder().setActionMplsLspHi(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new CofActionMplsLspNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setActionMplsLspHi(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new CofActionMplsLspNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setActionMplsLspHi(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new CofActionMplsLspNotifGroupDescStatsUpdatedCaseBuilder().setActionMplsLspHi(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
