/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: hema.gopalkrishnan@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.Ordering;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ActionExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionSetNwDstReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionSetNwSrcReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.CommonPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.DlAddressAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.DlAddressActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.EthertypeActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.GroupIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.MaxLengthActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.MplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.MplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NwTosAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmFieldsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmFieldsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.PortActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.QueueIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.QueueIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.VlanPcpAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.VlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.VlanVidAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.VlanVidActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.dst._case.TcpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.src._case.TcpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.src._case.UdpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 * @author avishnoi@in.ibm.com Added convertor for OF bucket actions to SAL
 *         actions
 */
public final class ActionConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(ActionConvertor.class);
    private static final String UNKNOWN_ACTION_TYPE_VERSION = "Unknown Action Type for the Version";

    private ActionConvertor() {
        // NOOP
    }

    /**
     * Translates SAL actions into OF Library actions
     *
     * @param actions    SAL actions
     * @param version    Openflow protocol version used
     * @param datapathid
     * @param flow       TODO
     * @return OF Library actions
     */
    public static List<Action> getActions(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions,
            short version, BigInteger datapathid, Flow flow) {
        List<Action> actionsList = new ArrayList<>();
        Action ofAction;

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> sortedActions =
                Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>build())
                        .sortedCopy(actions);

        for (int actionItem = 0; actionItem < sortedActions.size(); actionItem++) {
            ofAction = null;
            ActionBuilder actionBuilder = new ActionBuilder();

            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = sortedActions.get(
                    actionItem).getAction();

            if (action instanceof OutputActionCase) {
                ofAction = salToOFAction((OutputActionCase) action, actionBuilder, version);
            } else if (action instanceof GroupActionCase) {
                ofAction = salToOFGroupAction(action, actionBuilder);
            } else if (action instanceof CopyTtlOutCase) {
                ofAction = salToOFCopyTTLIOut(actionBuilder);
            } else if (action instanceof CopyTtlInCase) {
                ofAction = salToOFCopyTTLIIn(actionBuilder);
            } else if (action instanceof SetMplsTtlActionCase) {
                ofAction = salToOFSetMplsTtl(action, actionBuilder);
            } else if (action instanceof DecMplsTtlCase) {
                ofAction = salToOFDecMplsTtl(actionBuilder);
            } else if (action instanceof PushVlanActionCase) {
                ofAction = salToOFPushVlanAction(action, actionBuilder, version);
            } else if (action instanceof PopVlanActionCase) {
                ofAction = (version == OFConstants.OFP_VERSION_1_0) ?
                        salToOFStripVlan(actionBuilder, version)
                        : salToOFPopVlan(actionBuilder);
            } else if (action instanceof PushMplsActionCase) {
                ofAction = salToOFPushMplsAction(action, actionBuilder);
            } else if (action instanceof PopMplsActionCase) {
                ofAction = salToOFPopMpls(action, actionBuilder);
            } else if (action instanceof SetQueueActionCase) {
                ofAction = salToOFSetQueue(action, actionBuilder);
            } else if (action instanceof SetNwTtlActionCase) {
                ofAction = salToOFSetNwTtl(action, actionBuilder);
            } else if (action instanceof DecNwTtlCase) {
                ofAction = salToOFDecNwTtl(actionBuilder);
            } else if (action instanceof SetFieldCase) {
                ofAction = salToOFSetField(action, actionBuilder, version, datapathid);
            } else if (action instanceof PushPbbActionCase) {
                ofAction = salToOFPushPbbAction(action, actionBuilder);
            } else if (action instanceof PopPbbActionCase) {
                ofAction = salToOFPopPBB(actionBuilder);

                // 1.0 Actions
            } else if (action instanceof SetVlanIdActionCase) {
                /*if (version == OFConstants.OFP_VERSION_1_0) {

                } else {
                    List<Action> setVlanIdActionsList = convertToOF13(action, actionBuilder);
                    actionsList.addAll(setVlanIdActionsList);
                }*/
                ofAction = salToOFSetVlanId(action, actionBuilder, version);
            } else if (action instanceof SetVlanPcpActionCase) {
                ofAction = salToOFSetVlanpcp(action, actionBuilder, version);
            } else if (action instanceof StripVlanActionCase) {
                ofAction = salToOFStripVlan(actionBuilder, version);
            } else if (action instanceof SetDlSrcActionCase) {
                ofAction = salToOFSetDlSrc(action, actionBuilder, version);
            } else if (action instanceof SetDlDstActionCase) {
                ofAction = salToOFSetDlDst(action, actionBuilder, version);
            } else if (action instanceof SetNwSrcActionCase) {
                ofAction = salToOFSetNwSrc(action, actionBuilder, version);
            } else if (action instanceof SetNwDstActionCase) {
                ofAction = salToOFSetNwDst(action, actionBuilder, version);
            } else if (action instanceof SetTpSrcActionCase) {
                ofAction = salToOFSetTpSrc(action, actionBuilder, version, IPProtocols.fromProtocolNum(flow.getMatch().
                        getIpMatch().getIpProtocol()));
            } else if (action instanceof SetTpDstActionCase) {
                ofAction = salToOFSetTpDst(action, actionBuilder, version, IPProtocols.fromProtocolNum(flow.getMatch().
                        getIpMatch().getIpProtocol()));
            } else if (action instanceof SetNwTosActionCase) {
                ofAction = salToOFSetNwTos(action, actionBuilder, version);
            } else if (action instanceof GeneralExtensionGrouping) {

                /**
                 * TODO: EXTENSION PROPOSAL (action, MD-SAL to OFJava)
                 * - we might need sessionContext as converter input
                 *
                 */

                GeneralExtensionGrouping extensionCaseGrouping = (GeneralExtensionGrouping) action;
                Extension extAction = extensionCaseGrouping.getExtension();
                ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(extensionCaseGrouping.getExtensionKey(), version);
                ConvertorToOFJava<Action> convertor =
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                if (convertor != null) {
                    ofAction = convertor.convert(extAction);
                }
            } else {
                // try vendor codecs
                TypeVersionKey<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> key =
                        new TypeVersionKey<>(
                                (Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>) action.getImplementedInterface(),
                                version);
                ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action> convertor =
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                if (convertor != null) {
                    ofAction = convertor.convert(action);
                }
            }

            if (ofAction != null) {
                actionsList.add(ofAction);
            }
        }
        return actionsList;
    }

    private static Action salToOFSetField(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version, BigInteger datapathid) {

        SetFieldCase setFieldCase = (SetFieldCase) action;
        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match =
                setFieldCase.getSetField();

        if (version == OFConstants.OFP_VERSION_1_0) {
            // pushvlan +setField can be called to configure 1.0 switches via MDSAL app
            if (match.getVlanMatch() != null) {
                VlanVidActionBuilder vlanidActionBuilder = new VlanVidActionBuilder();
                vlanidActionBuilder.setVlanVid(match.getVlanMatch().getVlanId().getVlanId().getValue());
                actionBuilder.setType(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetVlanVid.class);
                actionBuilder.addAugmentation(VlanVidAction.class, vlanidActionBuilder.build());
                return actionBuilder.build();
            } else {
                return emtpyAction(actionBuilder);
            }

        } else {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            MatchReactor.getInstance().convert(match, version, oxmFieldsActionBuilder, datapathid);

            actionBuilder.setType(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);

            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        }

    }

    private static Action salToOFDecNwTtl(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class);
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFPushMplsAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PushMplsActionCase pushMplsActionCase = (PushMplsActionCase) action;
        actionBuilder.setType(PushMpls.class);

        return salToOFPushAction(pushMplsActionCase.getPushMplsAction().getEthernetType(), actionBuilder);
    }

    private static Action salToOFPushPbbAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PushPbbActionCase pushPbbActionCase = (PushPbbActionCase) action;
        actionBuilder.setType(PushPbb.class);

        return salToOFPushAction(pushPbbActionCase.getPushPbbAction().getEthernetType(), actionBuilder);
    }

    private static Action salToOFPushVlanAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            // if client configure openflow 1.0 switch as a openflow 1.3 switch using openflow 1.3 instructions
            // then we can ignore PUSH_VLAN as set-vlan-id will push a vlan header if not present
            return null;
        }

        PushVlanActionCase pushVlanActionCase = (PushVlanActionCase) action;
        PushVlanAction pushVlanAction = pushVlanActionCase.getPushVlanAction();
        actionBuilder.setType(PushVlan.class);

        return salToOFPushAction(pushVlanAction.getEthernetType(), actionBuilder);
    }

    private static Action salToOFSetNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        SetNwTtlActionCase nwTtlActionCase = (SetNwTtlActionCase) action;
        NwTtlActionBuilder nwTtlActionBuilder = new NwTtlActionBuilder();
        nwTtlActionBuilder.setNwTtl(nwTtlActionCase.getSetNwTtlAction().getNwTtl());
        actionBuilder.setType(SetNwTtl.class);
        actionBuilder.addAugmentation(NwTtlAction.class, nwTtlActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFSetQueue(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        SetQueueActionCase setQueueActionCase = (SetQueueActionCase) action;
        SetQueueAction setQueueAction = setQueueActionCase.getSetQueueAction();

        QueueIdActionBuilder queueIdActionBuilder = new QueueIdActionBuilder();
        queueIdActionBuilder.setQueueId(setQueueAction.getQueueId());
        actionBuilder.setType(SetQueue.class);
        actionBuilder.addAugmentation(QueueIdAction.class, queueIdActionBuilder.build());

        return actionBuilder.build();
    }

    private static Action salToOFPopMpls(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        PopMplsActionCase popMplsActionCase = (PopMplsActionCase) action;
        actionBuilder.setType(PopMpls.class);

        return salToOFPushAction(popMplsActionCase.getPopMplsAction().getEthernetType(), actionBuilder);
    }

    private static Action salToOFPopVlan(ActionBuilder actionBuilder) {
        actionBuilder.setType(PopVlan.class);
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFPopPBB(ActionBuilder actionBuilder) {
        actionBuilder.setType(PopPbb.class);
        return emtpyAction(actionBuilder);
    }

    // set-vlan-id (1.0 feature) can be called on  1.3 switches as well using ADSAL apis
    private static Action salToOFSetVlanId(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetVlanIdActionCase setvlanidcase = (SetVlanIdActionCase) action;
        SetVlanIdAction setvlanidaction = setvlanidcase.getSetVlanIdAction();

        if (version == OFConstants.OFP_VERSION_1_0) {

            VlanVidActionBuilder vlanidActionBuilder = new VlanVidActionBuilder();
            vlanidActionBuilder.setVlanVid(setvlanidaction.getVlanId().getValue());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetVlanVid.class);
            actionBuilder.addAugmentation(VlanVidAction.class, vlanidActionBuilder.build());
            return actionBuilder.build();

        } else {
            if (version >= OFConstants.OFP_VERSION_1_3) {
                OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
                List<MatchEntry> matchEntriesList = new ArrayList<>();
                MatchEntryBuilder matchEntriesBuilder = new MatchEntryBuilder();
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setOxmMatchField(VlanVid.class);
                VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
                VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
                vlanVidBuilder.setCfiBit(true);
                vlanVidBuilder.setVlanVid(setvlanidaction.getVlanId().getValue());
                vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
                matchEntriesBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
                matchEntriesBuilder.setHasMask(false);
                matchEntriesList.add(matchEntriesBuilder.build());
                oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
                actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
                return actionBuilder.build();
            } else {
                LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
                return null;
            }
        }
    }

    private static Action salToOFSetVlanpcp(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetVlanPcpActionCase setvlanpcpcase = (SetVlanPcpActionCase) action;
        SetVlanPcpAction setvlanpcpaction = setvlanpcpcase.getSetVlanPcpAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            VlanPcpActionBuilder vlanpcpActionBuilder = new VlanPcpActionBuilder();
            vlanpcpActionBuilder.setVlanPcp(setvlanpcpaction.getVlanPcp().getValue());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetVlanPcp.class);
            actionBuilder.addAugmentation(VlanPcpAction.class, vlanpcpActionBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfVlanPcp(setvlanpcpaction.getVlanPcp()));
            oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    private static Action salToOFStripVlan(ActionBuilder actionBuilder, short version) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.StripVlan.class);

            return emtpyAction(actionBuilder);
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            MatchEntryBuilder matchEntriesBuilder = new MatchEntryBuilder();
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setOxmMatchField(VlanVid.class);
            VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
            VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
            vlanVidBuilder.setCfiBit(true);
            vlanVidBuilder.setVlanVid(0x0000);
            vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
            matchEntriesBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
            matchEntriesBuilder.setHasMask(false);
            matchEntriesList.add(matchEntriesBuilder.build());
            oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    private static Action salToOFSetDlSrc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetDlSrcActionCase setdlsrccase = (SetDlSrcActionCase) action;
        SetDlSrcAction setdlsrcaction = setdlsrccase.getSetDlSrcAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            DlAddressActionBuilder dladdressactionbuilder = new DlAddressActionBuilder();
            dladdressactionbuilder.setDlAddress(setdlsrcaction.getAddress());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetDlSrc.class);
            actionBuilder.addAugmentation(DlAddressAction.class, dladdressactionbuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
            EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
            ethSrcBuilder.setMacAddress(setdlsrcaction.getAddress());
            ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc.class);
            matchEntryBuilder.setHasMask(false);
            matchEntriesList.add(matchEntryBuilder.build());
            oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);

            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    private static Action salToOFSetDlDst(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetDlDstActionCase setdldstcase = (SetDlDstActionCase) action;
        SetDlDstAction setdldstaction = setdldstcase.getSetDlDstAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            DlAddressActionBuilder dladdressactionbuilder = new DlAddressActionBuilder();
            dladdressactionbuilder.setDlAddress(setdldstaction.getAddress());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetDlDst.class);
            actionBuilder.addAugmentation(DlAddressAction.class, dladdressactionbuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(EthDst.class);
            EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
            EthDstBuilder ethDstBuilder = new EthDstBuilder();
            ethDstBuilder.setMacAddress(setdldstaction.getAddress());
            matchEntryBuilder.setHasMask(false);

            ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ethDstCaseBuilder.build());

            matchEntriesList.add(matchEntryBuilder.build());
            oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    protected static Action salToOFSetNwSrc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        try {
            ActionSetNwSrcReactor.getInstance().convert((SetNwSrcActionCase) action, version, actionBuilder, null);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        return actionBuilder.build();
    }

    protected static Action salToOFSetNwDst(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        try {
            ActionSetNwDstReactor.getInstance().convert((SetNwDstActionCase) action, version, actionBuilder, null);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        return actionBuilder.build();
    }

    private static Action salToOFSetNwTos(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version) {

        SetNwTosActionCase setnwtoscase = (SetNwTosActionCase) action;
        SetNwTosAction setnwtosaction = setnwtoscase.getSetNwTosAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            NwTosActionBuilder tosBuilder = new NwTosActionBuilder();
            tosBuilder.setNwTos(setnwtosaction.getTos().shortValue());
            actionBuilder.addAugmentation(NwTosAction.class, tosBuilder.build());
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTos.class);

            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfIpDscp(new Dscp(
                    ActionUtil.tosToDscp(setnwtosaction.getTos().shortValue())
            )));
            oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }

    }

    private static Action salToOFSetTpSrc(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version, IPProtocols protocol) {

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetTpSrcActionCase settpsrccase = (SetTpSrcActionCase) action;
            SetTpSrcAction settpsrcaction = settpsrccase.getSetTpSrcAction();

            PortActionBuilder settpsrc = new PortActionBuilder();
            PortNumber port = new PortNumber(settpsrcaction.getPort().getValue().longValue());
            settpsrc.setPort(port);

            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetTpSrc.class);
            actionBuilder.addAugmentation(PortAction.class, settpsrc.build());
            return actionBuilder.build();
        } else if (version == OFConstants.OFP_VERSION_1_3) {
            SetTpSrcActionCase settpsrccase = (SetTpSrcActionCase) action;
            SetTpSrcAction settpsrcaction = settpsrccase.getSetTpSrcAction();

            MatchEntryBuilder matchEntriesBuilder = new MatchEntryBuilder();
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setHasMask(false);

            InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
            int port = settpsrcaction.getPort().getValue().intValue();
            int type = 0x0f & port;

            switch (protocol) {
                case ICMP:
                    matchEntriesBuilder.setOxmMatchField(Icmpv4Type.class);
                    Icmpv4TypeCaseBuilder icmpv4TypeCaseBuilder = new Icmpv4TypeCaseBuilder();
                    Icmpv4TypeBuilder icmpv4TypeBuilder = new Icmpv4TypeBuilder();
                    icmpv4TypeBuilder.setIcmpv4Type((short) type);
                    icmpv4TypeCaseBuilder.setIcmpv4Type(icmpv4TypeBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(icmpv4TypeCaseBuilder.build());
                    break;
                case ICMPV6:
                    matchEntriesBuilder.setOxmMatchField(Icmpv6Type.class);
                    Icmpv6TypeCaseBuilder icmpv6TypeCaseBuilder = new Icmpv6TypeCaseBuilder();
                    Icmpv6TypeBuilder icmpv6TypeBuilder = new Icmpv6TypeBuilder();
                    icmpv6TypeBuilder.setIcmpv6Type((short) type);
                    icmpv6TypeCaseBuilder.setIcmpv6Type(icmpv6TypeBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(icmpv6TypeCaseBuilder.build());
                    break;
                case TCP:
                    matchEntriesBuilder.setOxmMatchField(TcpSrc.class);
                    TcpSrcCaseBuilder tcpSrcCaseBuilder = new TcpSrcCaseBuilder();
                    TcpSrcBuilder tcpSrcBuilder = new TcpSrcBuilder();
                    tcpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    tcpSrcCaseBuilder.setTcpSrc(tcpSrcBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(tcpSrcCaseBuilder.build());
                    break;
                case UDP:
                    matchEntriesBuilder.setOxmMatchField(UdpSrc.class);
                    UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
                    UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
                    udpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
                    break;
                default:
                    LOG.warn("Unknown protocol with combination of SetSourcePort: {}", protocol);
                    break;
            }

            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);

            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            List<MatchEntry> matchEntries = new ArrayList<MatchEntry>();
            matchEntries.add(matchEntriesBuilder.build());
            oxmFieldsActionBuilder.setMatchEntry(matchEntries);

            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        }
        LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
        return null;
    }

    private static Action salToOFSetTpDst(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder, short version, IPProtocols protocol) {

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetTpDstActionCase settpdstcase = (SetTpDstActionCase) action;
            SetTpDstAction settpdstaction = settpdstcase.getSetTpDstAction();
            PortActionBuilder settpdst = new PortActionBuilder();
            PortNumber port = new PortNumber(settpdstaction.getPort().getValue().longValue());
            settpdst.setPort(port);

            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetTpDst.class);
            actionBuilder.addAugmentation(PortAction.class, settpdst.build());
            return actionBuilder.build();
        } else if (version == OFConstants.OFP_VERSION_1_3) {
            SetTpDstActionCase settpdstcase = (SetTpDstActionCase) action;
            SetTpDstAction settpdstaction = settpdstcase.getSetTpDstAction();

            MatchEntryBuilder matchEntriesBuilder = new MatchEntryBuilder();
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setHasMask(false);
            int port = settpdstaction.getPort().getValue().intValue();
            int code = 0x0f & port;

            switch (protocol) {
                case ICMP:
                    matchEntriesBuilder.setOxmMatchField(Icmpv4Type.class);
                    Icmpv4CodeCaseBuilder icmpv4CodeCaseBuilder = new Icmpv4CodeCaseBuilder();
                    Icmpv4CodeBuilder icmpv4CodeBuilder = new Icmpv4CodeBuilder();
                    icmpv4CodeBuilder.setIcmpv4Code((short) code);
                    icmpv4CodeCaseBuilder.setIcmpv4Code(icmpv4CodeBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(icmpv4CodeCaseBuilder.build());
                    break;
                case ICMPV6:
                    matchEntriesBuilder.setOxmMatchField(Icmpv6Code.class);
                    Icmpv6CodeCaseBuilder icmpv6CodeCaseBuilder = new Icmpv6CodeCaseBuilder();
                    Icmpv6CodeBuilder icmpv6CodeBuilder = new Icmpv6CodeBuilder();
                    icmpv6CodeBuilder.setIcmpv6Code((short) code);
                    icmpv6CodeCaseBuilder.setIcmpv6Code(icmpv6CodeBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(icmpv6CodeCaseBuilder.build());
                    break;
                case TCP:
                    matchEntriesBuilder.setOxmMatchField(TcpDst.class);
                    TcpDstCaseBuilder tcpDstCaseBuilder = new TcpDstCaseBuilder();
                    TcpDstBuilder tcpDstBuilder = new TcpDstBuilder();
                    tcpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
                    break;
                case UDP:
                    matchEntriesBuilder.setOxmMatchField(UdpSrc.class);
                    UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
                    UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
                    udpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
                    matchEntriesBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
                    break;
                default:
                    LOG.warn("Unknown protocol with combination of SetSourcePort: {}", protocol);
                    break;
            }

            actionBuilder
                    .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);

            OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
            List<MatchEntry> matchEntries = new ArrayList<MatchEntry>();
            matchEntries.add(matchEntriesBuilder.build());
            oxmFieldsActionBuilder.setMatchEntry(matchEntries);

            actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
            return actionBuilder.build();
        }
        LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
        return null;
    }

    private static Action salToOFGroupAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {

        GroupActionCase groupActionCase = (GroupActionCase) action;
        GroupAction groupAction = groupActionCase.getGroupAction();

        GroupIdActionBuilder groupIdBuilder = new GroupIdActionBuilder();
        groupIdBuilder.setGroupId(groupAction.getGroupId());
        actionBuilder.setType(Group.class);
        actionBuilder.addAugmentation(GroupIdAction.class, groupIdBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFPushAction(Integer ethernetType, ActionBuilder actionBuilder) {
        EthertypeActionBuilder ethertypeActionBuilder = new EthertypeActionBuilder();
        if (ethernetType != null) {
            ethertypeActionBuilder.setEthertype(new EtherType(ethernetType));
        }

        /* OF */
        actionBuilder.addAugmentation(EthertypeAction.class, ethertypeActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFDecMplsTtl(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFSetMplsTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder) {
        SetMplsTtlActionCase mplsTtlActionCase = (SetMplsTtlActionCase) action;
        SetMplsTtlAction mplsTtlAction = mplsTtlActionCase.getSetMplsTtlAction();

        MplsTtlActionBuilder mplsTtlActionBuilder = new MplsTtlActionBuilder();
        mplsTtlActionBuilder.setMplsTtl(mplsTtlAction.getMplsTtl()/* SAL */);
        /* OF */
        actionBuilder.setType(SetMplsTtl.class);
        actionBuilder.addAugmentation(MplsTtlAction.class, mplsTtlActionBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFCopyTTLIIn(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFCopyTTLIOut(ActionBuilder actionBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);
        return emtpyAction(actionBuilder);

    }

    private static Action emtpyAction(ActionBuilder actionBuilder) {
        return actionBuilder.build();
    }

    private static Action salToOFAction(
            OutputActionCase outputActionCase,
            ActionBuilder actionBuilder, short version) {

        OutputAction outputAction = outputActionCase.getOutputAction();
        PortActionBuilder portAction = new PortActionBuilder();
        MaxLengthActionBuilder maxLenActionBuilder = new MaxLengthActionBuilder();
        if (outputAction.getMaxLength() != null) {
            maxLenActionBuilder.setMaxLength(outputAction.getMaxLength());
        } else {
            maxLenActionBuilder.setMaxLength(0);
        }
        actionBuilder.addAugmentation(MaxLengthAction.class, maxLenActionBuilder.build());

        Uri uri = outputAction.getOutputNodeConnector();

        OpenflowVersion ofVersion = OpenflowVersion.get(version);
        Long portNumber = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, uri.getValue());
        if (OpenflowPortsUtil.checkPortValidity(ofVersion, portNumber)) {
            portAction.setPort(new PortNumber(portNumber));
        } else {
            LOG.error("Invalid Port specified " + portNumber + " for Output Action for OF version:" + ofVersion);
        }

        actionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
        actionBuilder.addAugmentation(PortAction.class, portAction.build());
        return actionBuilder.build();

    }

    /**
     * Method to convert OF actions associated with bucket to SAL Actions.
     *
     * @param actionList
     * @param ofVersion  current ofp version
     * @param actionPath TODO
     * @return List of converted SAL Actions.
     */
    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> toMDSalActions(
            List<Action> actionList, OpenflowVersion ofVersion, ActionPath actionPath) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> bucketActions = new ArrayList<>();
        for (Action action : actionList) {
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class)) {
                bucketActions.add(ofToSALOutputAction(ofVersion, action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class)) {
                bucketActions.add(ofToSALGroupAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class)) {
                CopyTtlOutBuilder copyTtlOutaction = new CopyTtlOutBuilder();
                bucketActions.add(new CopyTtlOutCaseBuilder().setCopyTtlOut(copyTtlOutaction.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class)) {
                CopyTtlInBuilder copyTtlInaction = new CopyTtlInBuilder();
                bucketActions.add(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtlInaction.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class)) {
                bucketActions.add(ofToSALSetMplsTtl(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class)) {
                DecMplsTtlBuilder decMplsTtl = new DecMplsTtlBuilder();
                bucketActions.add(new DecMplsTtlCaseBuilder().setDecMplsTtl(decMplsTtl.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class)) {
                bucketActions.add(ofToSALPushVlanAction(action));

            } else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class)
                    || action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.StripVlan.class)) {
                // OF1.0 nodes will emit StripVlan and OF1.3+ will emit StripVlan/PopVlan, convert both to PopVlan for SAL
                PopVlanActionBuilder popVlan = new PopVlanActionBuilder();
                bucketActions.add(new PopVlanActionCaseBuilder().setPopVlanAction(popVlan.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class)) {
                bucketActions.add(ofToSALPushMplsAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class)) {
                bucketActions.add(ofToSALPopMplsAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class)) {
                bucketActions.add(ofToSALSetQueue(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class)) {
                bucketActions.add(ofToSALSetNwTtl(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class)) {
                DecNwTtlBuilder decNwTtl = new DecNwTtlBuilder();
                bucketActions.add(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class)) {
                bucketActions.add(new SetFieldCaseBuilder().setSetField(MatchConvertorImpl.fromOFSetFieldToSALSetFieldAction(action, ofVersion))
                        .build());
            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class)) {
                bucketActions.add(ofToSALPushPbbAction(action));

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class)) {
                PopPbbActionBuilder popPbb = new PopPbbActionBuilder();
                bucketActions.add(new PopPbbActionCaseBuilder().setPopPbbAction(popPbb.build()).build());

            } else if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter.class)) {
                /**
                 * TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL)
                 * - we might also need a way on how to identify exact type of augmentation to be
                 *   used as match can be bound to multiple models
                 */
                org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action processedAction =
                        ActionExtensionHelper.processAlienAction(action, ofVersion, actionPath);
                if (processedAction != null) {
                    bucketActions.add(processedAction);
                }
            }
        }
        return bucketActions;
    }

    /**
     * Method converts OF Output action object to SAL Output action object.
     *
     * @param ofVersion
     * @param ofVersion
     * @param action    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.
     *                  action.rev130731.actions.actions.list.Action
     * @return OutputAction
     */
    public static OutputActionCase ofToSALOutputAction(OpenflowVersion ofVersion, Action action) {
        OutputActionBuilder outputAction = new OutputActionBuilder();
        PortAction port = action.getAugmentation(PortAction.class);
        if (port != null) {
            PortNumberUni protocolAgnosticPort = 
                    OpenflowPortsUtil.getProtocolAgnosticPort(
                    ofVersion, port.getPort().getValue());
            String portNumberAsString = OpenflowPortsUtil.portNumberToString(protocolAgnosticPort);
            outputAction.setOutputNodeConnector(new Uri(portNumberAsString));
        } else {
            LOG.error("Provided action is not OF Output action, no associated port found!");
        }

        MaxLengthAction length = action.getAugmentation(MaxLengthAction.class);
        if (length != null) {
            outputAction.setMaxLength(length.getMaxLength());
        } else {
            LOG.error("Provided action is not OF Output action, no associated length found!");
        }

        return new OutputActionCaseBuilder().setOutputAction(outputAction.build()).build();
    }

    /**
     * Method converts OF GroupAction object to SAL GroupAction object
     *
     * @param action
     * @return GroupAction
     */
    public static GroupActionCase ofToSALGroupAction(Action action) {

        GroupActionBuilder groupAction = new GroupActionBuilder();

        GroupIdAction groupId = action.getAugmentation(GroupIdAction.class);
        groupAction.setGroupId(groupId.getGroupId());

        return new GroupActionCaseBuilder().setGroupAction(groupAction.build()).build();
    }

    /**
     * Method converts OF SetMplsTTL action object to SAL SetMplsTTL action
     * object.
     *
     * @param action
     * @return
     */
    public static SetMplsTtlActionCase ofToSALSetMplsTtl(Action action) {

        SetMplsTtlActionBuilder mplsTtlAction = new SetMplsTtlActionBuilder();
        MplsTtlAction mplsTtl = action.getAugmentation(MplsTtlAction.class);
        mplsTtlAction.setMplsTtl(mplsTtl.getMplsTtl());
        return new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(mplsTtlAction.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action
     * @return PushVlanAction
     */
    public static PushVlanActionCase ofToSALPushVlanAction(Action action) {

        PushVlanActionBuilder pushVlanAction = new PushVlanActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushVlanAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushVlanActionCaseBuilder().setPushVlanAction(pushVlanAction.build()).build();
    }

    /**
     * Method converts OF PushMpls action to SAL PushMpls action.
     *
     * @param action
     * @return PushMplsAction
     */
    public static PushMplsActionCase ofToSALPushMplsAction(Action action) {

        PushMplsActionBuilder pushMplsAction = new PushMplsActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushMplsAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushMplsActionCaseBuilder().setPushMplsAction(pushMplsAction.build()).build();
    }

    /**
     * Method converts OF PopMpls action to SAL PopMpls action.
     *
     * @param action
     * @return PopMplsActionCase
     */
    public static PopMplsActionCase ofToSALPopMplsAction(Action action) {

        PopMplsActionBuilder popMplsAction = new PopMplsActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        popMplsAction.setEthernetType(etherType.getEthertype().getValue());

        return new PopMplsActionCaseBuilder().setPopMplsAction(popMplsAction.build()).build();
    }

    /**
     * Method converts OF SetQueue action to SAL SetQueue action.
     *
     * @param action
     * @return SetQueueAction
     */
    public static SetQueueActionCase ofToSALSetQueue(Action action) {

        SetQueueActionBuilder setQueueAction = new SetQueueActionBuilder();

        QueueIdAction queueId = action.getAugmentation(QueueIdAction.class);
        setQueueAction.setQueueId(queueId.getQueueId());
        return new SetQueueActionCaseBuilder().setSetQueueAction(setQueueAction.build()).build();
    }

    /**
     * Method converts OF SetNwTtl action to SAL SetNwTtl action.
     *
     * @param action
     * @return SetNwTtlAction
     */
    public static SetNwTtlActionCase ofToSALSetNwTtl(Action action) {

        SetNwTtlActionBuilder setNwTtl = new SetNwTtlActionBuilder();
        NwTtlAction nwTtl = action.getAugmentation(NwTtlAction.class);
        setNwTtl.setNwTtl(nwTtl.getNwTtl());

        return new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtl.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action
     * @return PushVlanAction
     */
    public static PushPbbActionCase ofToSALPushPbbAction(Action action) {

        PushPbbActionBuilder pushPbbAction = new PushPbbActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushPbbAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushPbbActionCaseBuilder().setPushPbbAction(pushPbbAction.build()).build();
    }

    //TODO make a model in YANG for protocols 
    /*private enum IPProtocols {
        ICMP(1), 
        TCP(6), 
        UDP(17), 
        ICMPV6(58);

        private int protocol;
        
        private static Map<Integer, IPProtocols> valueMap;
        static {
            valueMap = new HashMap<>();
            for(IPProtocols protocols : IPProtocols.values()) {
                valueMap.put(protocols.protocol, protocols);
            }
        }
        
        private IPProtocols(int value) {
            this.protocol = value;
        }

        private byte getValue() {
            return (byte) this.protocol;
        }
        
        private Short getShortValue() {
            return (short) protocol;
        }
        
        private IPProtocols fromProtocolNum(Short protocolNum) {
            return valueMap.get(protocolNum);
        }
    }    */

}
