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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionFcidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.fcid.grouping.ActionFcidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionFcidNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionFcidNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionFcidNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionFcidNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionFcidGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.FcidId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.fcid.grouping.ActionFcidHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.fcid.grouping.ActionFcidHiBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 10/23/14.
 */
public class FcidConvertor implements ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        ActionFcid actionFcid = ((OfjCofActionFcid) input.getActionChoice()).getActionFcid();
        ActionFcidHiBuilder actionFcidHiBuilder = new ActionFcidHiBuilder();
        FcidId fcidId = new FcidId(new Integer(actionFcid.getFcid().shortValue()));
        actionFcidHiBuilder.setFcid(fcidId);

        return resolveAction(actionFcidHiBuilder.build(), path);
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof CofActionFcidGrouping);
        CofActionFcidGrouping cofActionFcidGrouping = (CofActionFcidGrouping) actionCase;
        ActionFcidHi actionFcidHi = cofActionFcidGrouping.getActionFcidHi();

        ActionFcidBuilder actionFcidBuilder = new ActionFcidBuilder();
        if (actionFcidHi.getFcid().getValue() != null) {
            Short fcid = (short) actionFcidHi.getFcid().getValue().shortValue();
            actionFcidBuilder.setFcid(fcid);
        } else {
            throw new IllegalStateException("Attribute fcid must not be empty.");
        }
        OfjCofActionFcidBuilder builder = new OfjCofActionFcidBuilder().setActionFcid(actionFcidBuilder.build());
        return ActionUtil.createCiscoAction(builder.build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            ActionFcidHi value, ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new CofActionFcidNodesNodeTableFlowWriteActionsCaseBuilder().setActionFcidHi(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new CofActionFcidNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setActionFcidHi(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new CofActionFcidNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setActionFcidHi(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new CofActionFcidNotifGroupDescStatsUpdatedCaseBuilder().setActionFcidHi(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }

}
