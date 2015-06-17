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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.action.container.action.choice.OfjCofActionNetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.netflow.grouping.ActionNetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNetflowNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNetflowNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNetflowNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNetflowNodesNodeTableFlowWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.CofActionNetflowGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.netflow.grouping.ActionNetflowHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.netflow.grouping.ActionNetflowHiBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 10/23/14.
 */
public class NetflowConvertor implements ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
        ConvertorActionFromOFJava<Action, ActionPath> {


    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        ActionNetflowHiBuilder actionNetflowHiBuilder = new ActionNetflowHiBuilder();
        actionNetflowHiBuilder.setNetflow(true);
        return resolveAction(actionNetflowHiBuilder.build(), path);
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase) {
        Preconditions.checkArgument(actionCase instanceof CofActionNetflowGrouping);

        ActionNetflowBuilder actionNetflowBuilder = new ActionNetflowBuilder();
        actionNetflowBuilder.setNetflow(true);
        OfjCofActionNetflowBuilder builder = new OfjCofActionNetflowBuilder().setActionNetflow(actionNetflowBuilder.build());
        return ActionUtil.createCiscoAction(builder.build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            ActionNetflowHi value, ActionPath path) {
        switch (path) {
            case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
                return new CofActionNetflowNodesNodeTableFlowWriteActionsCaseBuilder().setActionNetflowHi(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
                return new CofActionNetflowNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setActionNetflowHi(value).build();
            case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
                return new CofActionNetflowNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setActionNetflowHi(value).build();
            case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
                return new CofActionNetflowNotifGroupDescStatsUpdatedCaseBuilder().setActionNetflowHi(value).build();
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
