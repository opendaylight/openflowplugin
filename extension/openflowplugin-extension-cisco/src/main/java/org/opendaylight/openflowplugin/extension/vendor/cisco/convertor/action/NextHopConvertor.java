/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.action;

import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.vendor.cisco.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.CofAtOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.OfjAugCofActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNh;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141008.ofj.cof.action.output.nh.grouping.ActionOutputNhBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofActionNextHopGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofAtOutputNhAddressExtraType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.CofAtOutputNhAddressType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.NhPortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHi.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.cof.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHiBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.apply.actions._case.apply.actions.action.action.CofActionNextHopNotifFlowsStatisticsUpdateApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.flows.statistics.update.flow.and.statistics.map.list.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNextHopNotifFlowsStatisticsUpdateWriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.group.desc.stats.updated.group.desc.stats.buckets.bucket.action.action.CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cisco.action.rev141010.nodes.node.table.flow.instructions.instruction.instruction.write.actions._case.write.actions.action.action.CofActionNextHopNodesNodeTableFlowWriteActionsCaseBuilder;

import com.google.common.base.Preconditions;

/**
 * @author msunal
 *
 */
public class NextHopConvertor implements 
ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>,
ConvertorActionFromOFJava<Action, ActionPath> {

    @Override
    public org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action convert(Action input, ActionPath path) {
        ActionOutputNh actionOutNh = input.getAugmentation(OfjAugCofAction.class).getActionOutputNh();
        
        ActionOutputNhHiBuilder actionOutNhHiBuilder = new ActionOutputNhHiBuilder();
        actionOutNhHiBuilder.setAddressType(CofAtOutputNhAddressType.forValue(actionOutNh.getAddressType()));
        actionOutNhHiBuilder.setAddressExtraType(CofAtOutputNhAddressExtraType.forValue(actionOutNh.getAddressExtraType()));
        
        switch (actionOutNhHiBuilder.getAddressExtraType()) {
        case PORT:
            actionOutNhHiBuilder.setAddressExtra(new NhPortNumber(actionOutNh.getAddressExtra().longValue()));
            break;
        case NONE:
            // TODO; skip bytes if padding used
            break;
        default:
            // NOOP
        }
        
        Address nhAddress = null;
        switch (actionOutNhHiBuilder.getAddressType()) {
        case IPV4:
            nhAddress = new Address(new Ipv4Address(ActionUtil.bytesToIpv4Address(actionOutNh.getAddress())));
            break;
        case IPV6:
            nhAddress = new Address(new Ipv6Address(ActionUtil.bytesToIpv6Address(actionOutNh.getAddress())));
            break;
        case MAC48:
            nhAddress = new Address(new MacAddress(ByteBufUtils.macAddressToString(actionOutNh.getAddress())));
            break;
        case P2P:
            // TODO; skip bytes if padding used
            break;
        case NONE:
            // TODO; skip bytes if padding used
            break;
        default:
            // NOOP
        }
        actionOutNhHiBuilder.setAddress(nhAddress);
        
        return resolveAction(actionOutNhHiBuilder.build(), path);
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action resolveAction(
            ActionOutputNhHi value, ActionPath path) {
        switch (path) {
        case NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION:
            return new CofActionNextHopNodesNodeTableFlowWriteActionsCaseBuilder().setActionOutputNhHi(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION:
            return new CofActionNextHopNotifFlowsStatisticsUpdateWriteActionsCaseBuilder().setActionOutputNhHi(value).build();
        case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION:
            return new CofActionNextHopNotifFlowsStatisticsUpdateApplyActionsCaseBuilder().setActionOutputNhHi(value).build();
        case GROUPDESCSTATSUPDATED_GROUPDESCSTATS_BUCKETS_BUCKET_ACTION:
            return new CofActionNextHopNotifGroupDescStatsUpdatedCaseBuilder().setActionOutputNhHi(value).build();
        default:
            throw new CodecPreconditionException(path);
        }
    }

    @Override
    public Action convert(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action cofActionHi) {
        Preconditions.checkArgument(cofActionHi instanceof CofActionNextHopGrouping);
        CofActionNextHopGrouping cofActionHiGrouping = (CofActionNextHopGrouping) cofActionHi;
        ActionOutputNhHi actionOutputNhHi = cofActionHiGrouping.getActionOutputNhHi();
        
        ActionOutputNhBuilder actionOutputNhBld = new ActionOutputNhBuilder();
        actionOutputNhBld.setAddressType(actionOutputNhHi.getAddressType().getIntValue());
        actionOutputNhBld.setAddressExtraType(actionOutputNhHi.getAddressExtraType().getIntValue());
        
        switch (actionOutputNhHi.getAddressExtraType()) {
        case PORT:
            actionOutputNhBld.setAddressExtra(actionOutputNhHi.getAddressExtra().getValue());
            break;
        case NONE:
            // TODO: write padding? 
            break;
        default:
            throw new IllegalArgumentException("not implemented: "+actionOutputNhHi.getAddressExtraType());
        }
        
        switch (actionOutputNhHi.getAddressType()) {
        case IPV4:
            String ipv4 = actionOutputNhHi.getAddress().getIpv4Address().getValue();
            byte[] ipv4Raw = new byte[EncodeConstants.GROUPS_IN_IPV4_ADDRESS];
            Iterable<String> address4Groups = ByteBufUtils.DOT_SPLITTER.split(ipv4);
            int i1 = 0;
            for (String group : address4Groups) {
                ipv4Raw[i1] = (byte) Short.parseShort(group);
                i1++;
            }
            actionOutputNhBld.setAddress(ipv4Raw);
            break;
        case IPV6:
            String ipv6 = actionOutputNhHi.getAddress().getIpv6Address().getValue();
            byte[] ipv6Raw = new byte[EncodeConstants.SIZE_OF_IPV6_ADDRESS_IN_BYTES];
            Iterable<String> address6Groups = ByteBufUtils.COLON_SPLITTER.split(ipv6);
            int i2 = 0;
            for (String group : address6Groups) {
                int ipv6Part = Integer.parseInt(group, 16);
                ipv6Raw[i2] = (byte) (ipv6Part >> 8);
                i2 ++;
                ipv6Raw[i2] = (byte) ipv6Part;
                i2++;
            }
            actionOutputNhBld.setAddress(ipv6Raw);
            break;
        case MAC48:
            String mac48 = actionOutputNhHi.getAddress().getMacAddress().getValue();
            actionOutputNhBld.setAddress(ByteBufUtils.macAddressToBytes(mac48));
            break;
        case P2P:
            // NOOP, TODO: write padding?
            break;
        case NONE:
            // TODO: write padding? 
            break;
        default:
            throw new IllegalArgumentException("not implemented: "+actionOutputNhHi.getAddressExtraType());
        }
        
        OfjAugCofActionBuilder builder = new OfjAugCofActionBuilder().setActionOutputNh(actionOutputNhBld.build());
        return ActionUtil.createCiscoAction(builder.build(), CofAtOutputNh.class);
    }
}
