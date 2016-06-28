/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.queue._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;

/**
 * @author michal.polkorab
 */
public class ActionConvertorV13Test {
    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor#convert(java.util.List, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData)}}
     */
    @Test
    public void testToMDSalActions() {
        List<Action> actions = new ArrayList<>();
        ActionResponseConvertorData data = new ActionResponseConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setActionPath(ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
                .Action>> mdSalActions = ConvertorManager.getInstance().convert(actions, data);

        Assert.assertEquals("Wrong number of output actions", 0, mdSalActions.orElse(Collections.emptyList()).size());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor#convert(java.util.List, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData)}}
     */
    @Test
    public void testToMDSalActions2() {
        OpenflowPortsUtil.init();
        List<Action> actions = new ArrayList<>();

        ActionBuilder actionBuilder = new ActionBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(42L));
        outputBuilder.setMaxLength(52);
        caseBuilder.setOutputAction(outputBuilder.build());
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new CopyTtlOutCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new CopyTtlInCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetMplsTtlCaseBuilder setMplsTtlCaseBuilder = new SetMplsTtlCaseBuilder();
        SetMplsTtlActionBuilder setMplsTtlBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlBuilder.setMplsTtl((short) 4);
        setMplsTtlCaseBuilder.setSetMplsTtlAction(setMplsTtlBuilder.build());
        actionBuilder.setActionChoice(setMplsTtlCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new DecMplsTtlCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanBuilder = new PushVlanActionBuilder();
        pushVlanBuilder.setEthertype(new EtherType(new EtherType(16)));
        pushVlanCaseBuilder.setPushVlanAction(pushVlanBuilder.build());
        actionBuilder.setActionChoice(pushVlanCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        PushMplsCaseBuilder pushMplsCaseBuilder = new PushMplsCaseBuilder();
        PushMplsActionBuilder pushMplsBuilder = new PushMplsActionBuilder();
        pushMplsBuilder.setEthertype(new EtherType(new EtherType(17)));
        pushMplsCaseBuilder.setPushMplsAction(pushMplsBuilder.build());
        actionBuilder.setActionChoice(pushMplsCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        PopMplsCaseBuilder popMplsCaseBuilder = new PopMplsCaseBuilder();
        PopMplsActionBuilder popMplsBuilder = new PopMplsActionBuilder();
        popMplsBuilder.setEthertype(new EtherType(new EtherType(18)));
        popMplsCaseBuilder.setPopMplsAction(popMplsBuilder.build());
        actionBuilder.setActionChoice(popMplsCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetQueueCaseBuilder setQueueCaseBuilder = new SetQueueCaseBuilder();
        SetQueueActionBuilder setQueueBuilder = new SetQueueActionBuilder();
        setQueueBuilder.setQueueId(1234L);
        setQueueCaseBuilder.setSetQueueAction(setQueueBuilder.build());
        actionBuilder.setActionChoice(setQueueCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        GroupCaseBuilder groupCaseBuilder = new GroupCaseBuilder();
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroupId(555L);
        groupCaseBuilder.setGroupAction(groupActionBuilder.build());
        actionBuilder.setActionChoice(groupCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetNwTtlCaseBuilder nwTtlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder nwTtlBuilder = new SetNwTtlActionBuilder();
        nwTtlBuilder.setNwTtl((short) 8);
        nwTtlCaseBuilder.setSetNwTtlAction(nwTtlBuilder.build());
        actionBuilder.setActionChoice(nwTtlCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new DecNwTtlCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setOxmMatchField(InPort.class);
        matchBuilder.setHasMask(false);
        InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
        InPortBuilder inPortBuilder = new InPortBuilder();
        inPortBuilder.setPortNumber(new PortNumber(1L));
        inPortCaseBuilder.setInPort(inPortBuilder.build());
        matchBuilder.setMatchEntryValue(inPortCaseBuilder.build());
        entries.add(matchBuilder.build());
        setFieldBuilder.setMatchEntry(entries);
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
        actionBuilder.setActionChoice(setFieldCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        PushPbbCaseBuilder pushPbbCaseBuilder = new PushPbbCaseBuilder();
        PushPbbActionBuilder pushPbbBuilder = new PushPbbActionBuilder();
        pushPbbBuilder.setEthertype(new EtherType(new EtherType(19)));
        pushPbbCaseBuilder.setPushPbbAction(pushPbbBuilder.build());
        actionBuilder.setActionChoice(pushPbbCaseBuilder.build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopPbbCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new SetNwDstCaseBuilder().build());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new StripVlanCaseBuilder().build());
        actions.add(actionBuilder.build());

        ActionResponseConvertorData data = new ActionResponseConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setActionPath(ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
                .Action>> mdSalActionsOptional = ConvertorManager.getInstance().convert(actions, data);

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
                .Action> mdSalActions = mdSalActionsOptional.orElse(Collections.emptyList());

        Assert.assertEquals("Wrong number of output actions", 18, mdSalActions.size());
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
        action = mdSalActions.get(16);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.SetNwDstActionCase", action.getImplementedInterface().getName());
        action = mdSalActions.get(17);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.PopVlanActionCase", action.getImplementedInterface().getName());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor#convert(java.util.List, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData)} }
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
        /* Correct canonical form for v4 prefix!!! */
        ipv4Builder.setIpv4Address(new Ipv4Prefix("10.0.0.0/24"));
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
        /* Use canonical form, 00:00:0000 is not a valid v6 notation */
        ipv6Builder.setIpv6Address(new Ipv6Prefix("::5/128"));
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
        /* Use canonical form, 00:00:0006/64 is not a valid v6 notation - this equates to ::/64 */
        ipv6Builder.setIpv6Address(new Ipv6Prefix("::/64"));
        nwDstBuilder.setAddress(ipv6Builder.build());
        nwDstCaseBuilder.setSetNwDstAction(nwDstBuilder.build());
        actionBuilder.setAction(nwDstCaseBuilder.build());
        actionBuilder.setOrder(11);
        salActions.add(actionBuilder.build());

        IpMatchBuilder ipMatchBld = new IpMatchBuilder().setIpProtocol((short) 6);
        MatchBuilder matchBld = new MatchBuilder().setIpMatch(ipMatchBld.build());
        FlowBuilder flowBld = new FlowBuilder().setMatch(matchBld.build());
        Flow flow = flowBld.build();

        ActionConvertorData data = new ActionConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setDatapathId(new BigInteger("42"));

        if (flow.getMatch() != null && flow.getMatch().getIpMatch() != null) {
            data.setIpProtocol(flow.getMatch().getIpMatch().getIpProtocol());
        }

        Optional<List<Action>> actionsOptional = ConvertorManager.getInstance().convert(salActions, data);
        List<Action> actions = actionsOptional.orElse(Collections.emptyList());

        Assert.assertEquals("Wrong number of actions", 12, actions.size());
        Action action = actions.get(0);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        MatchEntry entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);;
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp.class, false);
        Assert.assertEquals("Wrong vlan pcp", 7, ((VlanPcpCase) entry.getMatchEntryValue()).getVlanPcp().getVlanPcp()
                .intValue());

        action = actions.get(1);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        setFieldCase =
          (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid.class, false);
        Assert.assertEquals("Wrong vlan vid", 0, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getVlanVid()
                .intValue());
        Assert.assertEquals("Wrong cfi bit", true, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().isCfiBit());

        action = actions.get(2);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst.class, false);
        Assert.assertEquals("Wrong dl dst", "00:00:00:00:00:06", ((EthDstCase) entry.getMatchEntryValue()).getEthDst()
                .getMacAddress().getValue());

        action = actions.get(3);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc.class, false);
        Assert.assertEquals("Wrong dl src", "00:00:00:00:00:05", ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc()
                .getMacAddress().getValue());

        action = actions.get(4);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src.class, false);
        Assert.assertEquals("Wrong ipv4 src", "10.0.0.0", ((Ipv4SrcCase) entry.getMatchEntryValue()).getIpv4Src()
                .getIpv4Address().getValue());

        action = actions.get(5);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst.class, false);
        Assert.assertEquals("Wrong ipv4 dst", "10.0.0.2", ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst()
                .getIpv4Address().getValue());

        action = actions.get(6);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc.class, false);
        Assert.assertEquals("Wrong tcp src", 54, ((TcpSrcCase) entry.getMatchEntryValue()).getTcpSrc()
                .getPort().getValue().intValue());

        action = actions.get(7);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);;
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst.class, false);
        Assert.assertEquals("Wrong tcp dst", 45, ((TcpDstCase) entry.getMatchEntryValue()).getTcpDst()
                .getPort().getValue().intValue());

        action = actions.get(8);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp.class, false);
        Assert.assertEquals("Wrong ip dscp", 4, ((IpDscpCase) entry.getMatchEntryValue()).getIpDscp()
                .getDscp().getValue().intValue());

        action = actions.get(9);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid.class, false);
        Assert.assertEquals("Wrong vlan id", 22, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid()
                .getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid()
                .isCfiBit());

        action = actions.get(10);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src.class, false);
        Assert.assertEquals("Wrong ipv6 src", "::5",
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());

        action = actions.get(11);
        setFieldCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) action.getActionChoice();
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetFieldCase", action.getActionChoice().getImplementedInterface().getName());
        entry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst.class, false);
        Assert.assertEquals("Wrong ipv6 dst", "::",
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
    }

    private static void checkEntryHeader(MatchEntry entry, Class<? extends MatchField> field, boolean hasMask) {
        Assert.assertEquals("Wrong oxm class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong oxm field", field, entry.getOxmMatchField());
        Assert.assertEquals("Wrong hasMask", hasMask, entry.isHasMask());
    }
}
