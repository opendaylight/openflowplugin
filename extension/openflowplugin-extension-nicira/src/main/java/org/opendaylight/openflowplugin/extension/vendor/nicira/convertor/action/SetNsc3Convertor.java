/*
 * Copyright (C) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Authors : Madhu Venugopal
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action;

import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNsc3Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsc._3.grouping.NxActionSetNsc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nsc._3.grouping.NxActionSetNsc3Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionSetNsc3Grouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.NxActionSetNsc3NotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNsc3NotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.NxActionSetNsc3NotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.NxActionSetNsc3NodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nsc._3.grouping.NxSetNsc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.nx.action.set.nsc._3.grouping.NxSetNsc3Builder;

import com.google.common.base.Preconditions;

public class SetNsc3Convertor implements
        ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        NxActionSetNsc3 action = ((ActionSetNsc3) input.getActionChoice()).getNxActionSetNsc3();
        NxSetNsc3Builder builder = new NxSetNsc3Builder();
        builder.setNsc(action.getNsc());
        return resolveAction(builder.build(), path);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(NxSetNsc3 value, ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new NxActionSetNsc3NodesNodeTableFlowWriteActionsCaseBuilder().setNxSetNsc3(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new NxActionSetNsc3NotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setNxSetNsc3(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new NxActionSetNsc3NotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setNxSetNsc3(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new NxActionSetNsc3NotifGroupDescStatsUpdatedCaseBuilder().setNxSetNsc3(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action nxActionArg) {
        Preconditions.checkArgument(nxActionArg instanceof NxActionSetNsc3Grouping);
        NxActionSetNsc3Grouping nxAction = (NxActionSetNsc3Grouping) nxActionArg;
        ActionSetNsc3Builder builder = new ActionSetNsc3Builder();
        NxActionSetNsc3Builder nxActionSetnsc3Builder = new NxActionSetNsc3Builder();
        nxActionSetnsc3Builder.setNsc(nxAction.getNxSetNsc3().getNsc());
        builder.setNxActionSetNsc3(nxActionSetnsc3Builder.build());
        return ActionUtil.createAction(builder.build());
    }

}