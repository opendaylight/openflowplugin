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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.strip.vlan.action._case.StripVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;

/**
 * @author michal.polkorab
 *
 */
public class ActionConvertorV10Test {

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor#convert(java.util.List, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData)}}
     */
    @Test
    public void testGetActions() {
        List<Action> salActions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        SetVlanPcpActionCaseBuilder vlanPcpCaseBuilder = new SetVlanPcpActionCaseBuilder();
        SetVlanPcpActionBuilder pcpBuilder = new SetVlanPcpActionBuilder();
        pcpBuilder.setVlanPcp(new VlanPcp((short) 7));
        vlanPcpCaseBuilder.setSetVlanPcpAction(pcpBuilder.build());
        actionBuilder.setAction(vlanPcpCaseBuilder.build());
        actionBuilder.setOrder(0);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        StripVlanActionCaseBuilder stripCaseBuilder = new StripVlanActionCaseBuilder();
        StripVlanActionBuilder stripBuilder = new StripVlanActionBuilder();
        stripCaseBuilder.setStripVlanAction(stripBuilder.build());
        actionBuilder.setAction(stripCaseBuilder.build());
        actionBuilder.setOrder(1);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetDlDstActionCaseBuilder dlDstCaseBuilder = new SetDlDstActionCaseBuilder();
        SetDlDstActionBuilder dlDstBuilder = new SetDlDstActionBuilder();
        dlDstBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        dlDstCaseBuilder.setSetDlDstAction(dlDstBuilder.build());
        actionBuilder.setAction(dlDstCaseBuilder.build());
        actionBuilder.setOrder(2);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetDlSrcActionCaseBuilder dlSrcCaseBuilder = new SetDlSrcActionCaseBuilder();
        SetDlSrcActionBuilder dlSrcBuilder = new SetDlSrcActionBuilder();
        dlSrcBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        dlSrcCaseBuilder.setSetDlSrcAction(dlSrcBuilder.build());
        actionBuilder.setAction(dlSrcCaseBuilder.build());
        actionBuilder.setOrder(3);
        salActions.add(actionBuilder.build());
       
        actionBuilder = new ActionBuilder();
        SetNwSrcActionCaseBuilder nwSrcCaseBuilder = new SetNwSrcActionCaseBuilder();
        SetNwSrcActionBuilder nwSrcBuilder = new SetNwSrcActionBuilder();
        Ipv4Builder ipv4Builder = new Ipv4Builder();
        /* Use prefix which is correct in canonical representation in test */
        ipv4Builder.setIpv4Address(new Ipv4Prefix("10.0.0.0/24"));
        nwSrcBuilder.setAddress(ipv4Builder.build());
        nwSrcCaseBuilder.setSetNwSrcAction(nwSrcBuilder.build());
        actionBuilder.setAction(nwSrcCaseBuilder.build());
        actionBuilder.setOrder(4);
        salActions.add(actionBuilder.build());
        
        actionBuilder = new ActionBuilder();
        SetNwDstActionCaseBuilder nwDstCaseBuilder = new SetNwDstActionCaseBuilder();
        SetNwDstActionBuilder nwDstBuilder = new SetNwDstActionBuilder();
        ipv4Builder = new Ipv4Builder();
        ipv4Builder.setIpv4Address(new Ipv4Prefix("10.0.0.2/32"));
        nwDstBuilder.setAddress(ipv4Builder.build());
        nwDstCaseBuilder.setSetNwDstAction(nwDstBuilder.build());
        actionBuilder.setAction(nwDstCaseBuilder.build());
        actionBuilder.setOrder(5);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetTpSrcActionCaseBuilder tpSrcCaseBuilder = new SetTpSrcActionCaseBuilder();
        SetTpSrcActionBuilder tpSrcBuilder = new SetTpSrcActionBuilder();
        tpSrcBuilder.setPort(new PortNumber(54));
        tpSrcCaseBuilder.setSetTpSrcAction(tpSrcBuilder.build());
        actionBuilder.setAction(tpSrcCaseBuilder.build());
        actionBuilder.setOrder(6);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetTpDstActionCaseBuilder tpDstCaseBuilder = new SetTpDstActionCaseBuilder();
        SetTpDstActionBuilder tpDstBuilder = new SetTpDstActionBuilder();
        tpDstBuilder.setPort(new PortNumber(45));
        tpDstCaseBuilder.setSetTpDstAction(tpDstBuilder.build());
        actionBuilder.setAction(tpDstCaseBuilder.build());
        actionBuilder.setOrder(7);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetNwTosActionCaseBuilder tosCaseBuilder = new SetNwTosActionCaseBuilder();
        SetNwTosActionBuilder tosBuilder = new SetNwTosActionBuilder();
        tosBuilder.setTos(18);
        tosCaseBuilder.setSetNwTosAction(tosBuilder.build());
        actionBuilder.setAction(tosCaseBuilder.build());
        actionBuilder.setOrder(8);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetVlanIdActionCaseBuilder vlanIdCaseBuilder = new SetVlanIdActionCaseBuilder();
        SetVlanIdActionBuilder vlanIdBuilder = new SetVlanIdActionBuilder();
        vlanIdBuilder.setVlanId(new VlanId(22));
        vlanIdCaseBuilder.setSetVlanIdAction(vlanIdBuilder.build());
        actionBuilder.setAction(vlanIdCaseBuilder.build());
        actionBuilder.setOrder(9);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        PopVlanActionCaseBuilder popVlanActionCaseBuilder = new PopVlanActionCaseBuilder();
        actionBuilder.setAction(popVlanActionCaseBuilder.build());
        actionBuilder.setOrder(10);
        salActions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        vlanMatchBuilder.setVlanId(new VlanIdBuilder().setVlanId(new VlanId(22)).build());
        setFieldBuilder.setVlanMatch(vlanMatchBuilder.build());
        setFieldCaseBuilder.setSetField(setFieldBuilder.build());
        actionBuilder.setAction(setFieldCaseBuilder.build());
        actionBuilder.setOrder(11);
        salActions.add(actionBuilder.build());
        
        IpMatchBuilder ipMatchBld = new IpMatchBuilder().setIpProto(IpVersion.Ipv4);
        MatchBuilder matchBld = new MatchBuilder().setIpMatch(ipMatchBld.build());
        FlowBuilder flowBld = new FlowBuilder().setMatch(matchBld.build());
        Flow flow = flowBld.build();

        ActionConvertorData data = new ActionConvertorData(OFConstants.OFP_VERSION_1_0);
        data.setDatapathId(new BigInteger("42"));
        if (flow.getMatch() != null && flow.getMatch().getIpMatch() != null) {
            data.setIpProtocol(flow.getMatch().getIpMatch().getIpProtocol());
        }

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action>> actionsOptional =
                ConvertorManager.getInstance().convert(salActions, data);

        Assert.assertTrue("Action convertor not found", actionsOptional.isPresent());

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> actions = actionsOptional.get();
        
        Assert.assertEquals("Wrong number of actions", 12, actions.size());
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action action = actions.get(0);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetVlanPcpCase", action.getActionChoice().getImplementedInterface().getName());
        SetVlanPcpCase setVlanPcpCase = (SetVlanPcpCase) action.getActionChoice();
        Assert.assertEquals("Wrong vlan pcp", 7, setVlanPcpCase.getSetVlanPcpAction().getVlanPcp().intValue());
        
        action = actions.get(1);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.StripVlanCase", action.getActionChoice().getImplementedInterface().getName());
        
        action = actions.get(2);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetDlDstCase", action.getActionChoice().getImplementedInterface().getName());
        SetDlDstCase setDlDstCase = (SetDlDstCase) action.getActionChoice();
        Assert.assertEquals("Wrong dl dst", "00:00:00:00:00:06", setDlDstCase.getSetDlDstAction().getDlDstAddress().getValue());
        
        action = actions.get(3);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetDlSrcCase", action.getActionChoice().getImplementedInterface().getName());
        SetDlSrcCase setDlSrcCase = (SetDlSrcCase) action.getActionChoice();
        Assert.assertEquals("Wrong dl src", "00:00:00:00:00:05", setDlSrcCase.getSetDlSrcAction().getDlSrcAddress().getValue());
        
        action = actions.get(4);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetNwSrcCase", action.getActionChoice().getImplementedInterface().getName());
        SetNwSrcCase setNwSrcCase = (SetNwSrcCase) action.getActionChoice();
        Assert.assertEquals("Wrong nw src", "10.0.0.0", setNwSrcCase.getSetNwSrcAction().getIpAddress().getValue());
        
        action = actions.get(5);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetNwDstCase", action.getActionChoice().getImplementedInterface().getName());
        SetNwDstCase setNwDstCase = (SetNwDstCase) action.getActionChoice();
        Assert.assertEquals("Wrong nw dst", "10.0.0.2", setNwDstCase.getSetNwDstAction().getIpAddress().getValue());
        
        action = actions.get(6);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetTpSrcCase", action.getActionChoice().getImplementedInterface().getName());
        SetTpSrcCase setTpSrcCase = (SetTpSrcCase) action.getActionChoice();
        Assert.assertEquals("Wrong tp src", 54, setTpSrcCase.getSetTpSrcAction().getPort().getValue().intValue());
        
        action = actions.get(7);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetTpDstCase", action.getActionChoice().getImplementedInterface().getName());
        SetTpDstCase setTpDstCase = (SetTpDstCase) action.getActionChoice();
        Assert.assertEquals("Wrong tp dst", 45, setTpDstCase.getSetTpDstAction().getPort().getValue().intValue());
        
        action = actions.get(8);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetNwTosCase", action.getActionChoice().getImplementedInterface().getName());
        SetNwTosCase setNwTosCase = (SetNwTosCase) action.getActionChoice();
        Assert.assertEquals("Wrong nw tos", 18, setNwTosCase.getSetNwTosAction().getNwTos().intValue());
        
        action = actions.get(9);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetVlanVidCase", action.getActionChoice().getImplementedInterface().getName());
        SetVlanVidCase setVlanVidCase = (SetVlanVidCase) action.getActionChoice();
        Assert.assertEquals("Wrong vlan id", 22, setVlanVidCase.getSetVlanVidAction().getVlanVid().intValue());

        action = actions.get(10);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.StripVlanCase", action.getActionChoice().getImplementedInterface().getName());

        action = actions.get(11);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".action.rev150203.action.grouping.action.choice.SetVlanVidCase", action.getActionChoice().getImplementedInterface().getName());
        setVlanVidCase = (SetVlanVidCase) action.getActionChoice();
        Assert.assertEquals("Wrong vlan id", 22, setVlanVidCase.getSetVlanVidAction().getVlanVid().intValue());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor#convert(java.util.List, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData)}}
     */
    @Test
    public void testToMDSalActions() {
        OpenflowPortsUtil.init();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> actions = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionbuilder = 
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber(14L));
        outputBuilder.setMaxLength(555);
        caseBuilder.setOutputAction(outputBuilder.build());
        actionbuilder.setActionChoice(caseBuilder.build());
        actions.add(actionbuilder.build());

        ActionResponseConvertorData data = new ActionResponseConvertorData(OFConstants.OFP_VERSION_1_0);
        data.setActionPath(ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
        .Action>> mdSalActionsOptional =
                ConvertorManager.getInstance().convert(
                        actions, data);

        Assert.assertTrue("Action response convertor not found", mdSalActionsOptional.isPresent());

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action
                .Action> mdSalActions = mdSalActionsOptional.get();

        Assert.assertEquals("Wrong number of output actions", 1, mdSalActions.size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = mdSalActions.get(0);
        Assert.assertEquals("Wrong action type", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types"
                + ".rev131112.action.action.OutputActionCase", action.getImplementedInterface().getName());
        OutputActionCase outputAction = (OutputActionCase) action;
        Assert.assertEquals("Wrong output port", "14", outputAction.getOutputAction().getOutputNodeConnector().getValue());
        Assert.assertEquals("Wrong max length", 555, outputAction.getOutputAction().getMaxLength().intValue());
    }
}
