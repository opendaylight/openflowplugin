/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: usha.m.s@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.controller.sal.action.PopVlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IpAddressAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;

public class ActionConvertorTest {

    List<Action> actions = new ArrayList<>();
    static Integer actionItem = 0;

    @Test
    public void testActionConvertorwithallParameters() {
        OutputActionData();
        CopyTtlData();
        MplsTtlActionData();
        vlanActionData();
        mplsActionData();
        setQueueActionData();
        setGroupAction();
        NwTtlAction();
        pbbActionData();
        setFieldData();
        setExperimenterData();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731
        .actions.grouping.Action> OFActionsList = ActionConvertor.getActions(actions, (short) 0X4,BigInteger.valueOf(1));

       // OutputActions(OFActionsList);

    }

    private void setExperimenterData() {

        // TODO:SAL API Missing

    }

    private void setFieldData() {

        SetFieldBuilder setFA = new SetFieldBuilder();

        SetFieldBuilder matchBuilder = setFA;

        matchBuilder.setInPort(new NodeConnectorId("openflow:1:2125"));

        SetFieldBuilder setFB = new SetFieldBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new SetFieldCaseBuilder().setSetField(setFB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    private void pbbActionData() {
        PushPbbActionBuilder pushpbb = new PushPbbActionBuilder();
        pushpbb.setEthernetType(10);

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pushpbb.build()).build());

        actions.add(actionItem++, AB.build());

        PopPbbActionBuilder popPBB = new PopPbbActionBuilder();

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPBB.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    private void NwTtlAction() {
        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();

        setNwTtlActionBuilder.setNwTtl((short) 1);
        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());

        actions.add(actionItem++, AB.build());

        DecNwTtlBuilder necNwTtlBuilder = new DecNwTtlBuilder();


        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(necNwTtlBuilder.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    private void setGroupAction() {

        GroupActionBuilder grpIdAB = new GroupActionBuilder();
        grpIdAB.setGroup("98");

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new GroupActionCaseBuilder().setGroupAction(grpIdAB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    // TODO - check if this method is needed (private and never used locally) - see line 94
    private void OutputActions(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
            .action.rev130731.actions.grouping.Action> oFActionsList) {

        for (int item = 0; item < oFActionsList.size(); item++) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action action = oFActionsList
                    .get(item);

            if (action.getType().equals(Output.class)) {
                Assert.assertEquals((Integer) 10, (action.getAugmentation(MaxLengthAction.class)).getMaxLength());
                long port = 4294967293L;
           //     Assert.assertEquals(port, (long) (action.getAugmentation(PortAction.class)).getPort().getValue());


            }
            if (action.getType().equals(CopyTtlIn.class)) {
                Assert.assertEquals(action.getType(), CopyTtlIn.class);

            }
            if (action.getType().equals(CopyTtlOut.class)) {

                Assert.assertEquals(action.getType(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);

            }

            if (action.getType().equals(
            // TODO:getMplsTtl is missing.
                    SetMplsTtl.class)) {
                Assert.assertEquals(action.getType(), SetMplsTtl.class);

            }
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class)) {
                Assert.assertEquals(action.getType(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);

            }

            if (action.getType().equals(PushMpls.class)) {

                EthertypeAction etherTypeAction = action
                        .getAugmentation(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction.class);

                if (etherTypeAction != null) {


                    Assert.assertEquals((Integer) 10, etherTypeAction.getEthertype().getValue());
                }
            }

            if (action.getType().equals(PopMpls.class)) {
                Assert.assertEquals((Integer) 10, (action.getAugmentation(EthertypeAction.class)).getEthertype()
                        .getValue());

            }

            if (action.getType().equals(

            // TODO:SetQueue,I dont have getQueueId
                    SetQueue.class)) {
                Assert.assertEquals(action.getType(), SetQueue.class);
            }

            if (action.getType().equals(

            GroupIdAction.class)) {

                Assert.assertEquals(98, (long) (action.getAugmentation(GroupIdAction.class)).getGroupId());

            }

            if (action.getType().equals(

                    PushVlan.class)) {

                Assert.assertEquals(action.getType(), PushVlan.class);

                    }
            if (action.getType().equals(

            PopVlan.class)) {

                Assert.assertEquals(action.getType(), PopVlan.class);

            }

            if (action.getType().equals(

            SetNwTtl.class)) {

                Assert.assertEquals(action.getType(), SetNwTtl.class);

            }
            if (action.getType().equals(

            DecNwTtl.class)) {

                Assert.assertEquals(action.getType(), DecNwTtl.class);

            }
            if (action.getType().equals(PushPbb.class)) {

                EthertypeAction etherTypeAction = action
                        .getAugmentation(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction.class);

                if (etherTypeAction != null) {

                    Assert.assertEquals((Integer) 10, etherTypeAction.getEthertype().getValue());
                }
            }

            if (action.getType().equals(PopMpls.class)) {
                Assert.assertEquals(action.getType(), PopMpls.class);

            }
            /* TODO - fix this test case
            if (action.getType().equals(SetField.class)) {

                OxmFieldsAction sf = action.getAugmentation(OxmFieldsAction.class);

                Assert.assertEquals(OpenflowBasicClass.class, sf.getMatchEntries().get(0).getOxmClass());

                if (sf.getMatchEntries().get(0).getOxmMatchField().equals(InPort.class)) {
                    Assert.assertEquals(2125, sf.getMatchEntries().get(0).getAugmentation(PortNumberMatchEntry.class)
                            .getPortNumber().getValue().intValue());


                    }

            }
            */

        }

    }

    private void OutputActionData() {        
        OutputActionBuilder outputB = new OutputActionBuilder();
        outputB.setMaxLength(10);
        Uri uri = new Uri(OutputPortValues.CONTROLLER.toString());
        outputB.setOutputNodeConnector(uri);

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new OutputActionCaseBuilder().setOutputAction(outputB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    private void CopyTtlData() {
        CopyTtlOutBuilder copyB = new CopyTtlOutBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(copyB.build()).build());

        actions.add(actionItem++, AB.build());

        CopyTtlInBuilder copyTtlInBuilder = new CopyTtlInBuilder();

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtlInBuilder.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    private void MplsTtlActionData() {

        SetMplsTtlActionBuilder setMplsTtlActionB = new SetMplsTtlActionBuilder();

        setMplsTtlActionB.setMplsTtl((short) 10);
        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMplsTtlActionB.build()).build());


        actions.add(actionItem++, AB1.build());

        DecMplsTtlBuilder decMplsTtlB = new DecMplsTtlBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(decMplsTtlB.build()).build());

        actions.add(actionItem++, AB1.build());
    }

    private void vlanActionData() {
        PushVlanActionBuilder pvB = new PushVlanActionBuilder();

        pvB.setVlanId(new VlanId(10));

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(pvB.build()).build());

        actions.add(actionItem++, AB1.build());

        PopVlanActionBuilder popVAB = new PopVlanActionBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVAB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    private void mplsActionData() {

        PushMplsActionBuilder pushMB = new PushMplsActionBuilder();
        pushMB.setEthernetType(10);

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(pushMB.build()).build());

        actions.add(actionItem++, AB.build());

        PopMplsActionBuilder popMB = new PopMplsActionBuilder();
        popMB.setEthernetType(10);

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMB.build()).build());

        actions.add(actionItem++, AB1.build());
    }

    private void setQueueActionData() {

        SetQueueActionBuilder setQB = new SetQueueActionBuilder();
        setQB.setQueue("99");

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQB.build()).build());

        actions.add(actionItem++, AB1.build());

    }
    
    /**
     * testing {@link ActionConvertor#SalToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv4 
     */
    @Test
    public void testSalToOFSetNwDst10v4() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        ActionConvertor.SalToOFSetNwDst(action, actionBuilder, version);
        Assert.assertEquals(SetNwDst.class, actionBuilder.getType());
        Assert.assertEquals("10.0.0.1", actionBuilder.getAugmentation(IpAddressAction.class).getIpAddress().getValue());
    }

    /**
     * testing {@link ActionConvertor#SalToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv6 
     */
    @Test
    public void testSalToOFSetNwDst10v6() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:0db8:85a3:0042:1000:8a2e:0370:7334")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action ofAction = ActionConvertor.SalToOFSetNwDst(action, actionBuilder, version);
        Assert.assertNull(ofAction);
    }
    
    
    /**
     * testing {@link ActionConvertor#SalToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv4 
     */
    @Test
    public void testSalToOFSetNwDst13v4() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        ActionConvertor.SalToOFSetNwDst(action, actionBuilder, version);
        Assert.assertEquals(SetField.class, actionBuilder.getType());
        MatchEntries matchEntry = actionBuilder.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        Assert.assertEquals(Ipv4Dst.class, matchEntry.getOxmMatchField());
        Assert.assertEquals("10.0.0.1", matchEntry.getAugmentation(Ipv4AddressMatchEntry.class).getIpv4Address().getValue());
    }
    
    
    /**
     * testing {@link ActionConvertor#SalToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv6 
     */
    @Test
    public void testSalToOFSetNwDst13v6() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:0db8:85a3:0042:1000:8a2e:0370:7334")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        ActionConvertor.SalToOFSetNwDst(action, actionBuilder, version);
        Assert.assertEquals(SetField.class, actionBuilder.getType());
        MatchEntries matchEntry = actionBuilder.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        Assert.assertEquals(Ipv6Dst.class, matchEntry.getOxmMatchField());
        Assert.assertEquals("2001:0db8:85a3:0042:1000:8a2e:0370:7334", matchEntry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
    }
    
   
    
    /**
     * testing {@link ActionConvertor#SalToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv4 
     */
    @Test
    public void testSalToOFSetNwSrc10v4() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        ActionConvertor.SalToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertEquals(SetNwSrc.class, actionBuilder.getType());
        Assert.assertEquals("10.0.0.1", actionBuilder.getAugmentation(IpAddressAction.class).getIpAddress().getValue());
    }

    /**
     * testing {@link ActionConvertor#SalToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv6 
     */
    @Test
    public void testSalToOFSetNwSrc10v6() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:0db8:85a3:0042:1000:8a2e:0370:7334")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action ofAction = ActionConvertor.SalToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertNull(ofAction);
    }

    /**
     * testing {@link ActionConvertor#SalToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv4 
     */
    @Test
    public void testSalToOFSetNwSrc13v4() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        ActionConvertor.SalToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertEquals(SetField.class, actionBuilder.getType());
        MatchEntries matchEntry = actionBuilder.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        Assert.assertEquals(Ipv4Src.class, matchEntry.getOxmMatchField());
        Assert.assertEquals("10.0.0.1", matchEntry.getAugmentation(Ipv4AddressMatchEntry.class).getIpv4Address().getValue());
    }

    /**
     * testing {@link ActionConvertor#SalToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv6 
     */
    @Test
    public void testSalToOFSetNwSrc13v6() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:0db8:85a3:0042:1000:8a2e:0370:7334")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        ActionConvertor.SalToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertEquals(SetField.class, actionBuilder.getType());
        MatchEntries matchEntry = actionBuilder.getAugmentation(OxmFieldsAction.class).getMatchEntries().get(0);
        Assert.assertEquals(Ipv6Src.class, matchEntry.getOxmMatchField());
        Assert.assertEquals("2001:0db8:85a3:0042:1000:8a2e:0370:7334", matchEntry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
    }

    private static SetNwDstActionCase provisionNwDstActionBuilder(Address address) {
        SetNwDstAction nwDstAction = new SetNwDstActionBuilder().setAddress(address).build();
        SetNwDstActionCase action = new SetNwDstActionCaseBuilder()
            .setSetNwDstAction(nwDstAction)
            .build(); 
        return action;
    }
    
    private static SetNwSrcActionCase provisionNwSrcActionBuilder(Address address) {
        SetNwSrcAction nwSrcAction = new SetNwSrcActionBuilder().setAddress(address).build();
        SetNwSrcActionCase action = new SetNwSrcActionCaseBuilder()
            .setSetNwSrcAction(nwSrcAction)
            .build(); 
        return action;
    }

}