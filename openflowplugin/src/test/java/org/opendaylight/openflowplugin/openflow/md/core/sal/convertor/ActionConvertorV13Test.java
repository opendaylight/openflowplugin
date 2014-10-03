/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.strip.vlan.action._case.StripVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.QueueIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.QueueIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

/**
 * @author michal.polkorab
 *
 */
public class ActionConvertorV13Test {

    /**
     * Test {@link ActionConvertor#toMDSalActions(List, OpenflowVersion, ActionPath)}
     */
    @Test
    public void testToMDSalActions() {
        List<Action> actions = new ArrayList<>();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
        .Action> mdSalActions = ActionConvertor.toMDSalActions(actions, OpenflowVersion.OF13,
                ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

        Assert.assertEquals("Wrong number of output actions", 0, mdSalActions.size());
    }

    /**
     * Test {@link ActionConvertor#toMDSalActions(List, OpenflowVersion, ActionPath)}
     */
    @Test
    public void testToMDSalActions2() {
        OpenflowPortsUtil.init();
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(Output.class);
        PortActionBuilder port = new PortActionBuilder();
        port.setPort(new PortNumber(42L));
        actionBuilder.addAugmentation(PortAction.class, port.build());
        MaxLengthActionBuilder maxLen = new MaxLengthActionBuilder();
        maxLen.setMaxLength(52);
        actionBuilder.addAugmentation(MaxLengthAction.class, maxLen.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(CopyTtlOut.class);
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(CopyTtlIn.class);
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(SetMplsTtl.class);
        MplsTtlActionBuilder mplsTtl = new MplsTtlActionBuilder();
        mplsTtl.setMplsTtl((short) 4);
        actionBuilder.addAugmentation(MplsTtlAction.class, mplsTtl.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(DecMplsTtl.class);
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(PushVlan.class);
        EthertypeActionBuilder etherType = new EthertypeActionBuilder();
        etherType.setEthertype(new EtherType(16));
        actionBuilder.addAugmentation(EthertypeAction.class, etherType.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(PopVlan.class);
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(PushMpls.class);
        etherType = new EthertypeActionBuilder();
        etherType.setEthertype(new EtherType(17));
        actionBuilder.addAugmentation(EthertypeAction.class, etherType.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(PopMpls.class);
        etherType = new EthertypeActionBuilder();
        etherType.setEthertype(new EtherType(18));
        actionBuilder.addAugmentation(EthertypeAction.class, etherType.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(SetQueue.class);
        QueueIdActionBuilder queueId = new QueueIdActionBuilder();
        queueId.setQueueId(1234L);
        actionBuilder.addAugmentation(QueueIdAction.class, queueId.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(Group.class);
        GroupIdActionBuilder group = new GroupIdActionBuilder();
        group.setGroupId(555L);
        actionBuilder.addAugmentation(GroupIdAction.class, group.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(SetNwTtl.class);
        NwTtlActionBuilder nwTtl = new NwTtlActionBuilder();
        nwTtl.setNwTtl((short) 8);
        actionBuilder.addAugmentation(NwTtlAction.class, nwTtl.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(DecNwTtl.class);
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(SetField.class);
        OxmFieldsActionBuilder matchEntries = new OxmFieldsActionBuilder();
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder matchBuilder = new MatchEntriesBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setOxmMatchField(InPort.class);
        matchBuilder.setHasMask(false);
        PortNumberMatchEntryBuilder portBuilder = new PortNumberMatchEntryBuilder();
        portBuilder.setPortNumber(new PortNumber(1L));
        matchBuilder.addAugmentation(PortNumberMatchEntry.class, portBuilder.build());
        entries.add(matchBuilder.build());
        matchEntries.setMatchEntries(entries);
        actionBuilder.addAugmentation(OxmFieldsAction.class, matchEntries.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(PushPbb.class);
        etherType = new EthertypeActionBuilder();
        etherType.setEthertype(new EtherType(19));
        actionBuilder.addAugmentation(EthertypeAction.class, etherType.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setType(PopPbb.class);
        actions.add(actionBuilder.build());

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
        .Action> mdSalActions = ActionConvertor.toMDSalActions(actions, OpenflowVersion.OF13,
                ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

        Assert.assertEquals("Wrong number of output actions", 16, mdSalActions.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = mdSalActions.get(0);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.OutputActionCase", action.getImplementedInterface().getName());
        OutputActionCase output = (OutputActionCase) action;
        Assert.assertEquals("Wrong output port", "42", output.getOutputAction().getOutputNodeConnector().getValue());
        Assert.assertEquals("Wrong max length", 52, output.getOutputAction().getMaxLength().intValue());
        action = mdSalActions.get(1);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.CopyTtlOutCase", action.getImplementedInterface().getName());
        action = mdSalActions.get(2);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.CopyTtlInCase", action.getImplementedInterface().getName());
        action = mdSalActions.get(3);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.SetMplsTtlActionCase", action.getImplementedInterface().getName());
        SetMplsTtlActionCase setMplsTtl = (SetMplsTtlActionCase) action;
        Assert.assertEquals("Wrong mpls ttl", 4, setMplsTtl.getSetMplsTtlAction().getMplsTtl().intValue());
        action = mdSalActions.get(4);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.DecMplsTtlCase", action.getImplementedInterface().getName());
        action = mdSalActions.get(5);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PushVlanActionCase", action.getImplementedInterface().getName());
        PushVlanActionCase pushVlan = (PushVlanActionCase) action;
        Assert.assertEquals("Wrong ethertype", 16, pushVlan.getPushVlanAction().getEthernetType().intValue());
        action = mdSalActions.get(6);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PopVlanActionCase", action.getImplementedInterface().getName());
        action = mdSalActions.get(7);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PushMplsActionCase", action.getImplementedInterface().getName());
        PushMplsActionCase pushMpls = (PushMplsActionCase) action;
        Assert.assertEquals("Wrong ethertype", 17, pushMpls.getPushMplsAction().getEthernetType().intValue());
        action = mdSalActions.get(8);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PopMplsActionCase", action.getImplementedInterface().getName());
        PopMplsActionCase popMpls = (PopMplsActionCase) action;
        Assert.assertEquals("Wrong ethertype", 18, popMpls.getPopMplsAction().getEthernetType().intValue());
        action = mdSalActions.get(9);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.SetQueueActionCase", action.getImplementedInterface().getName());
        SetQueueActionCase setQueue = (SetQueueActionCase) action;
        Assert.assertEquals("Wrong queue-id", 1234, setQueue.getSetQueueAction().getQueueId().intValue());
        action = mdSalActions.get(10);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.GroupActionCase", action.getImplementedInterface().getName());
        GroupActionCase groupAction = (GroupActionCase) action;
        Assert.assertEquals("Wrong group-id", 555, groupAction.getGroupAction().getGroupId().intValue());
        action = mdSalActions.get(11);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.SetNwTtlActionCase", action.getImplementedInterface().getName());
        SetNwTtlActionCase setNwTtl = (SetNwTtlActionCase) action;
        Assert.assertEquals("Wrong nw ttl", 8, setNwTtl.getSetNwTtlAction().getNwTtl().intValue());
        action = mdSalActions.get(12);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.DecNwTtlCase", action.getImplementedInterface().getName());
        action = mdSalActions.get(13);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.SetFieldCase", action.getImplementedInterface().getName());
        SetFieldCase setField = (SetFieldCase) action;
        Assert.assertEquals("Wrong in port", "openflow:null:1", setField.getSetField().getInPort().getValue());
        action = mdSalActions.get(14);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PushPbbActionCase", action.getImplementedInterface().getName());
        PushPbbActionCase pushPbb = (PushPbbActionCase) action;
        Assert.assertEquals("Wrong ethertype", 19, pushPbb.getPushPbbAction().getEthernetType().intValue());
        action = mdSalActions.get(15);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PopPbbActionCase", action.getImplementedInterface().getName());
    }

    /**
     * Test {@link ActionConvertor#getActions(List, short, BigInteger)}
     */
    @Test
    public void testGetActions() {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
        .Action> salActions = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
        .ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
        .action.list.ActionBuilder();
        SetVlanPcpActionCaseBuilder vlanPcpCaseBuilder = new SetVlanPcpActionCaseBuilder();
        SetVlanPcpActionBuilder pcpBuilder = new SetVlanPcpActionBuilder();
        pcpBuilder.setVlanPcp(new VlanPcp((short) 7));
        vlanPcpCaseBuilder.setSetVlanPcpAction(pcpBuilder.build());
        actionBuilder.setAction(vlanPcpCaseBuilder.build());
        actionBuilder.setOrder(0);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        StripVlanActionCaseBuilder stripCaseBuilder = new StripVlanActionCaseBuilder();
        StripVlanActionBuilder stripBuilder = new StripVlanActionBuilder();
        stripCaseBuilder.setStripVlanAction(stripBuilder.build());
        actionBuilder.setAction(stripCaseBuilder.build());
        actionBuilder.setOrder(1);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetDlDstActionCaseBuilder dlDstCaseBuilder = new SetDlDstActionCaseBuilder();
        SetDlDstActionBuilder dlDstBuilder = new SetDlDstActionBuilder();
        dlDstBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        dlDstCaseBuilder.setSetDlDstAction(dlDstBuilder.build());
        actionBuilder.setAction(dlDstCaseBuilder.build());
        actionBuilder.setOrder(2);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetDlSrcActionCaseBuilder dlSrcCaseBuilder = new SetDlSrcActionCaseBuilder();
        SetDlSrcActionBuilder dlSrcBuilder = new SetDlSrcActionBuilder();
        dlSrcBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        dlSrcCaseBuilder.setSetDlSrcAction(dlSrcBuilder.build());
        actionBuilder.setAction(dlSrcCaseBuilder.build());
        actionBuilder.setOrder(3);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetNwSrcActionCaseBuilder nwSrcCaseBuilder = new SetNwSrcActionCaseBuilder();
        SetNwSrcActionBuilder nwSrcBuilder = new SetNwSrcActionBuilder();
        Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setIpv4Address(new Ipv4Prefix("10.0.0.1/24"));
        nwSrcBuilder.setAddress(ipv4Builder.build());
        nwSrcCaseBuilder.setSetNwSrcAction(nwSrcBuilder.build());
        actionBuilder.setAction(nwSrcCaseBuilder.build());
        actionBuilder.setOrder(4);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetNwDstActionCaseBuilder nwDstCaseBuilder = new SetNwDstActionCaseBuilder();
        SetNwDstActionBuilder nwDstBuilder = new SetNwDstActionBuilder();
        ipv4Builder = new Ipv4Builder();
        ipv4Builder.setIpv4Address(new Ipv4Prefix("10.0.0.2/32"));
        nwDstBuilder.setAddress(ipv4Builder.build());
        nwDstCaseBuilder.setSetNwDstAction(nwDstBuilder.build());
        actionBuilder.setAction(nwDstCaseBuilder.build());
        actionBuilder.setOrder(5);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetTpSrcActionCaseBuilder tpSrcCaseBuilder = new SetTpSrcActionCaseBuilder();
        SetTpSrcActionBuilder tpSrcBuilder = new SetTpSrcActionBuilder();
        tpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types
                .rev100924.PortNumber(54));
        tpSrcCaseBuilder.setSetTpSrcAction(tpSrcBuilder.build());
        actionBuilder.setAction(tpSrcCaseBuilder.build());
        actionBuilder.setOrder(6);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetTpDstActionCaseBuilder tpDstCaseBuilder = new SetTpDstActionCaseBuilder();
        SetTpDstActionBuilder tpDstBuilder = new SetTpDstActionBuilder();
        tpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types
                .rev100924.PortNumber(45));
        tpDstCaseBuilder.setSetTpDstAction(tpDstBuilder.build());
        actionBuilder.setAction(tpDstCaseBuilder.build());
        actionBuilder.setOrder(7);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetNwTosActionCaseBuilder tosCaseBuilder = new SetNwTosActionCaseBuilder();
        SetNwTosActionBuilder tosBuilder = new SetNwTosActionBuilder();
        tosBuilder.setTos(16);
        tosCaseBuilder.setSetNwTosAction(tosBuilder.build());
        actionBuilder.setAction(tosCaseBuilder.build());
        actionBuilder.setOrder(8);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        SetVlanIdActionCaseBuilder vlanIdCaseBuilder = new SetVlanIdActionCaseBuilder();
        SetVlanIdActionBuilder vlanIdBuilder = new SetVlanIdActionBuilder();
        vlanIdBuilder.setVlanId(new VlanId(22));
        vlanIdCaseBuilder.setSetVlanIdAction(vlanIdBuilder.build());
        actionBuilder.setAction(vlanIdCaseBuilder.build());
        actionBuilder.setOrder(9);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        nwSrcCaseBuilder = new SetNwSrcActionCaseBuilder();
        nwSrcBuilder = new SetNwSrcActionBuilder();
        Ipv6Builder ipv6Builder = new Ipv6Builder();
        ipv6Builder.setIpv6Address(new Ipv6Prefix("0000:0000:0000:0000:0000:0000:0000:0005/128"));
        nwSrcBuilder.setAddress(ipv6Builder.build());
        nwSrcCaseBuilder.setSetNwSrcAction(nwSrcBuilder.build());
        actionBuilder.setAction(nwSrcCaseBuilder.build());
        actionBuilder.setOrder(10);
        salActions.add(actionBuilder.build());
        actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                .action.list.ActionBuilder();
        nwDstCaseBuilder = new SetNwDstActionCaseBuilder();
        nwDstBuilder = new SetNwDstActionBuilder();
        ipv6Builder = new Ipv6Builder();
        ipv6Builder.setIpv6Address(new Ipv6Prefix("0000:0000:0000:0000:0000:0000:0000:0008/64"));
        nwDstBuilder.setAddress(ipv6Builder.build());
        nwDstCaseBuilder.setSetNwDstAction(nwDstBuilder.build());
        actionBuilder.setAction(nwDstCaseBuilder.build());
        actionBuilder.setOrder(11);
        salActions.add(actionBuilder.build());
        
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping
        .Action> actions = ActionConvertor.getActions(salActions, EncodeConstants.OF13_VERSION_ID, new BigInteger("42"));
        
        Assert.assertEquals("Wrong number of actions", 12, actions.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping
        .Action action = actions.get(0);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        MatchEntries entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp.class, false);
        Assert.assertEquals("Wrong vlan pcp", 7, entry.getAugmentation(VlanPcpMatchEntry.class).getVlanPcp().intValue());
        action = actions.get(1);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid.class, false);
        Assert.assertEquals("Wrong vlan vid", 0, entry.getAugmentation(VlanVidMatchEntry.class).getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, entry.getAugmentation(VlanVidMatchEntry.class).isCfiBit());
        action = actions.get(2);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst.class, false);
        Assert.assertEquals("Wrong dl dst", "00:00:00:00:00:06", entry.getAugmentation(MacAddressMatchEntry.class)
                .getMacAddress().getValue());
        action = actions.get(3);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc.class, false);
        Assert.assertEquals("Wrong dl src", "00:00:00:00:00:05", entry.getAugmentation(MacAddressMatchEntry.class)
                .getMacAddress().getValue());
        action = actions.get(4);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Src.class, false);
        Assert.assertEquals("Wrong ipv4 src", "10.0.0.1", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        action = actions.get(5);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Dst.class, false);
        Assert.assertEquals("Wrong ipv4 dst", "10.0.0.2", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        action = actions.get(6);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc.class, false);
        Assert.assertEquals("Wrong tcp src", 54, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
        action = actions.get(7);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst.class, false);
        Assert.assertEquals("Wrong tcp dst", 45, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
        action = actions.get(8);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpDscp.class, false);
        Assert.assertEquals("Wrong ip dscp", 4, entry.getAugmentation(DscpMatchEntry.class)
                .getDscp().getValue().intValue());
        action = actions.get(9);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid.class, false);
        Assert.assertEquals("Wrong vlan id", 22, entry.getAugmentation(VlanVidMatchEntry.class)
                .getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, entry.getAugmentation(VlanVidMatchEntry.class)
                .isCfiBit());
        action = actions.get(10);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Src.class, false);
        Assert.assertEquals("Wrong ipv4 src", "0000:0000:0000:0000:0000:0000:0000:0005",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
        action = actions.get(11);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev130731.SetField", action.getType().getName());
        entry = action.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Dst.class, false);
        Assert.assertEquals("Wrong ipv4 dst", "0000:0000:0000:0000:0000:0000:0000:0008",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
    }

    private static void checkEntryHeader(MatchEntries entry, Class<? extends MatchField> field, boolean hasMask) {
        Assert.assertEquals("Wrong oxm class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong oxm field", field, entry.getOxmMatchField());
        Assert.assertEquals("Wrong hasMask", hasMask, entry.isHasMask());
    }
}