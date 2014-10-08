/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;

import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.vrf.grouping.ActionVrfBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofActionVrfGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofAtVrfType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.VrfExtra;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.VrfName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.VrfVpnId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.vrf.grouping.ActionVrfHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.vrf.grouping.ActionVrfHiBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.CofActionVrfNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionVrfNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionVrfNodesNodeTableFlowWriteActionsCaseBuilder;

import com.google.common.base.Preconditions;

/**
 * Convert to/from openflowplugin model to openflowjava model for 
 * OutputReg action
 * @author readams
 */
public class VrfConvertor implements 
ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
ConvertorActionFromOFJava<Action, ActionPath> {
    
    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        ActionVrf actionLo = input.getAugmentation(OfjAugCofAction.class).getActionVrf();
        ActionVrfHiBuilder actionHiBld = new ActionVrfHiBuilder();
        actionHiBld.setVpnType(CofAtVrfType.forValue(actionLo.getVpnType()));
        VrfExtra vrfExtra = null; 
        switch (actionHiBld.getVpnType()) {
        case VPNID:
            vrfExtra = new VrfExtra(new VrfVpnId(actionLo.getVrfExtra()));
            break;
        case NAME:
            vrfExtra = new VrfExtra(new VrfName(new String(actionLo.getVrfExtra())));
            break;
        default:
            // NOOP, device sent invalid vrfExtra value
        }
        actionHiBld.setVrfExtra(vrfExtra);
        
        return resolveAction(actionHiBld.build(), path);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(ActionVrfHi value, ActionPath path) {
        switch (path) {
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new CofActionVrfNodesNodeTableFlowWriteActionsCaseBuilder().setActionVrfHi(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            return new CofActionVrfNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setActionVrfHi(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            return new CofActionVrfNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setActionVrfHi(value).build();
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            return new CofActionVrfNotifGroupDescStatsUpdatedCaseBuilder().setActionVrfHi(value).build();
        default:
            throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action cofActionArg) {
        Preconditions.checkArgument(cofActionArg instanceof CofActionVrfGrouping);
        CofActionVrfGrouping cofActionHi = (CofActionVrfGrouping) cofActionArg;
        ActionVrfHi actionVrfHi = cofActionHi.getActionVrfHi();
        
        ActionVrfBuilder actionVrfBld = new ActionVrfBuilder();
        actionVrfBld.setVpnType(actionVrfHi.getVpnType().getIntValue());
        switch (actionVrfHi.getVpnType()) {
        case VPNID:
            actionVrfBld.setVrfExtra(actionVrfHi.getVrfExtra().getVrfVpnId().getValue());
            break;
        case NAME:
            actionVrfBld.setVrfExtra(actionVrfHi.getVrfExtra().getVrfName().getValue().getBytes());
            break;
        default:
            throw new IllegalArgumentException("invalif vrf type: "+actionVrfHi.getVpnType());
        }
        
        OfjAugCofActionBuilder builder = new OfjAugCofActionBuilder().setActionVrf(actionVrfBld.build());
        return ActionUtil.createCiscoAction(builder.build(), CofAtVrf.class);
    }
}