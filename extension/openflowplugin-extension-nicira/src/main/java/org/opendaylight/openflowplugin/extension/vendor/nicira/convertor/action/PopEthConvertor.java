/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopEth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionPopEthBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.pop.eth.grouping.NxActionPopEth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.pop.eth.grouping.NxActionPopEthBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionPopEthGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionPopEthNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPopEthNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionPopEthNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionPopEthNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.pop.eth.grouping.NxPopEth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.pop.eth.grouping.NxPopEthBuilder;

public class PopEthConvertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        NxActionPopEth action = ((ActionPopEth) input.getActionChoice()).getNxActionPopEth();
        NxPopEthBuilder builder = new NxPopEthBuilder();
        return resolveAction(builder.build(), path);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(NxPopEth value, ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new NxActionPopEthNodesNodeTableFlowWriteActionsCaseBuilder().setNxPopEth(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new NxActionPopEthNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxPopEth(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new NxActionPopEthNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxPopEth(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new NxActionPopEthNotifGroupDescStatsUpdatedCaseBuilder().setNxPopEth(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionPopEthGrouping);
        NxActionPopEthGrouping nxAction = (NxActionPopEthGrouping) nxActionArg;
        ActionPopEthBuilder builder = new ActionPopEthBuilder();
        NxActionPopEthBuilder nxActionPopEthBuilder = new NxActionPopEthBuilder();
        builder.setNxActionPopEth(nxActionPopEthBuilder.build());
        return ActionUtil.createAction(builder.build());
    }

}
