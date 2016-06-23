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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;

/**
 * test for {@link ActionConvertor}
 */
public class ActionConvertorTest {

    List<Action> actions = new ArrayList<>();
    static Integer actionItem = 0;

    /**
     * prepare OpenflowPortsUtil util class
     */
    @Before
    public void setUp() {
        OpenflowPortsUtil.init();
    }

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
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> OFActionsList = ActionConvertor.getActions(actions, (short) 0X4, BigInteger.ONE, null);

        outputActions(OFActionsList);

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
        AB.setOrder(actionItem).setAction(new SetFieldCaseBuilder().setSetField(setFB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    private void pbbActionData() {
        PushPbbActionBuilder pushpbb = new PushPbbActionBuilder();
        pushpbb.setEthernetType(10);

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pushpbb.build()).build());

        actions.add(actionItem++, AB.build());

        PopPbbActionBuilder popPBB = new PopPbbActionBuilder();

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPBB.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    private void NwTtlAction() {
        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();

        setNwTtlActionBuilder.setNwTtl((short) 1);
        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());

        actions.add(actionItem++, AB.build());

        DecNwTtlBuilder necNwTtlBuilder = new DecNwTtlBuilder();


        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new DecNwTtlCaseBuilder().setDecNwTtl(necNwTtlBuilder.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    private void setGroupAction() {

        GroupActionBuilder grpIdAB = new GroupActionBuilder();
        grpIdAB.setGroup("98");

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new GroupActionCaseBuilder().setGroupAction(grpIdAB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    // TODO - check if this method is needed (private and never used locally) - see line 94

    private static void outputActions(final List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> oFActionsList) {

        for (int item = 0; item < oFActionsList.size(); item++) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action action = oFActionsList
                    .get(item);

            if (action.getActionChoice() instanceof OutputActionCase) {
                OutputActionCase outputActionCase = (OutputActionCase) action.getActionChoice();
                Assert.assertEquals((Integer) 10, (outputActionCase.getOutputAction().getMaxLength()));
                long port = 4294967293L;
                Assert.assertEquals(port, (long) (outputActionCase.getOutputAction().getPort().getValue()));


            }
            if (action.getActionChoice() instanceof CopyTtlInCase) {
                CopyTtlInCase copyTtlInCase = (CopyTtlInCase) action.getActionChoice();
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), CopyTtlInCase.class.getName());

            }
            if (action.getActionChoice() instanceof CopyTtlOutCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), CopyTtlOutCase.class.getName());
            }

            if (action.getActionChoice() instanceof
                    // TODO:getMplsTtl is missing.
                    SetMplsTtlCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), SetMplsTtlCase.class.getName());

            }
            if (action.getActionChoice() instanceof DecMplsTtlCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(),
                        DecMplsTtlCase.class.getName());
            }

            if (action.getActionChoice() instanceof PushMplsCase) {
                PushMplsCase pushMplsCase = (PushMplsCase) action.getActionChoice();
                EtherType etherType = pushMplsCase.getPushMplsAction().getEthertype();

                if (etherType != null) {
                    Assert.assertEquals((Integer) 10, etherType.getValue());
                }
            }

            if (action.getActionChoice() instanceof PopMplsCase) {
                PopMplsCase popMplsCase = (PopMplsCase) action.getActionChoice();
                Assert.assertEquals((Integer) 10, (popMplsCase.getPopMplsAction().getEthertype().getValue()));
            }

            if (action.getActionChoice() instanceof

                    // TODO:SetQueue,I dont have getQueueId
                    SetQueueCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), SetQueueCase.class.getName());
            }

            if (action.getActionChoice() instanceof GroupCase) {
                GroupCase groupCase = (GroupCase) action.getActionChoice();
                Assert.assertEquals(98, (long) (groupCase.getGroupAction().getGroupId()));
            }

            if (action.getActionChoice() instanceof PushVlanCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), PushVlanCase.class.getName());
            }

            if (action.getActionChoice() instanceof PopVlanCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), PopVlanCase.class.getName());
            }

            if (action.getActionChoice() instanceof SetNwTtlCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), SetNwTtlCase.class.getName());
            }
            if (action.getActionChoice() instanceof DecNwTtlCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), DecNwTtlCase.class.getName());
            }
            if (action.getActionChoice() instanceof PushPbbCase) {
                PushPbbCase pushPbbCase = (PushPbbCase) action.getActionChoice();
                if (pushPbbCase.getPushPbbAction().getEthertype() != null) {
                    Assert.assertEquals((Integer) 10, pushPbbCase.getPushPbbAction().getEthertype().getValue());
                }
            }

            if (action.getActionChoice() instanceof PopMplsCase) {
                Assert.assertEquals(action.getActionChoice().getImplementedInterface().getName(), PopMplsCase.class.getName());
            }
            if (action.getActionChoice() instanceof SetFieldCase) {
                SetFieldCase setFieldCase = (SetFieldCase) action.getActionChoice();
                Assert.assertNotNull(setFieldCase.getSetFieldAction());

/*
                Assert.assertEquals(OpenflowBasicClass.class, sf.getMatchEntry().get(0).getOxmClass());

                if (sf.getMatchEntry().get(0).getOxmMatchField().equals(InPort.class)) {
                    InPortCase inPortCase = ((InPortCase) sf.getMatchEntry().get(0).getMatchEntryValue());
                    Assert.assertEquals(2125, inPortCase.getInPort().getPortNumber().getValue().intValue());

                }
*/

            }


        }

    }

    private void OutputActionData() {
        OutputActionBuilder outputB = new OutputActionBuilder();
        outputB.setMaxLength(10);
        Uri uri = new Uri(OutputPortValues.CONTROLLER.toString());
        outputB.setOutputNodeConnector(uri);

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new OutputActionCaseBuilder().setOutputAction(outputB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    private void CopyTtlData() {
        CopyTtlOutBuilder copyB = new CopyTtlOutBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(copyB.build()).build());

        actions.add(actionItem++, AB.build());

        CopyTtlInBuilder copyTtlInBuilder = new CopyTtlInBuilder();

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtlInBuilder.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    private void MplsTtlActionData() {

        SetMplsTtlActionBuilder setMplsTtlActionB = new SetMplsTtlActionBuilder();

        setMplsTtlActionB.setMplsTtl((short) 10);
        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMplsTtlActionB.build()).build());


        actions.add(actionItem++, AB1.build());

        DecMplsTtlBuilder decMplsTtlB = new DecMplsTtlBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(decMplsTtlB.build()).build());

        actions.add(actionItem++, AB1.build());
    }

    private void vlanActionData() {
        PushVlanActionBuilder pvB = new PushVlanActionBuilder();

        pvB.setVlanId(new VlanId(10));

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new PushVlanActionCaseBuilder().setPushVlanAction(pvB.build()).build());

        actions.add(actionItem++, AB1.build());

        PopVlanActionBuilder popVAB = new PopVlanActionBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new PopVlanActionCaseBuilder().setPopVlanAction(popVAB.build()).build());

        actions.add(actionItem++, AB.build());

    }

    private void mplsActionData() {

        PushMplsActionBuilder pushMB = new PushMplsActionBuilder();
        pushMB.setEthernetType(10);

        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new PushMplsActionCaseBuilder().setPushMplsAction(pushMB.build()).build());

        actions.add(actionItem++, AB.build());

        PopMplsActionBuilder popMB = new PopMplsActionBuilder();
        popMB.setEthernetType(10);

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMB.build()).build());

        actions.add(actionItem++, AB1.build());
    }

    /**
     * testing {@link ActionConvertor#ofToSALPushMplsAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action)}
     * with OF-1.3, IPv6
     */
    @Test
    public void testOFtoSALPushMplsAction() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder
                = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        PushMplsCaseBuilder pushMplsCaseBuilder = new PushMplsCaseBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsActionBuilder pushMplsBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsActionBuilder();
        pushMplsBuilder.setEthertype(new EtherType(new Integer(34888)));
        pushMplsCaseBuilder.setPushMplsAction(pushMplsBuilder.build());
        actionBuilder.setActionChoice(pushMplsCaseBuilder.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action action = actionBuilder.build();
        Assert.assertEquals(34888, ActionConvertor.ofToSALPushMplsAction(action).getPushMplsAction().getEthernetType().intValue());
    }

    private void setQueueActionData() {

        SetQueueActionBuilder setQB = new SetQueueActionBuilder();
        setQB.setQueue("99");

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQB.build()).build());

        actions.add(actionItem++, AB1.build());

    }

    /**
     * testing {@link ActionConvertor#salToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv4
     */
    @Test
    public void testSalToOFSetNwDst10v4() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1/32")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        ActionConvertor.salToOFSetNwDst(action, actionBuilder, version);
        Assert.assertEquals(SetNwDstCase.class.getName(), actionBuilder.getActionChoice().getImplementedInterface().getName());
        SetNwDstCase setNwDstCase = (SetNwDstCase) actionBuilder.getActionChoice();
        Assert.assertEquals("10.0.0.1", setNwDstCase.getSetNwDstAction().getIpAddress().getValue());
    }

    /**
     * testing {@link ActionConvertor#salToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv6
     */
    @Test
    public void testSalToOFSetNwDst10v6() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        /* Use canonical form - no leading zeroes!!! */
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:db8:85a3:42:1000:8a2e:370:7334/128")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action ofAction = ActionConvertor.salToOFSetNwDst(action, actionBuilder, version);
        Assert.assertNull(ofAction);
    }


    /**
     * testing {@link ActionConvertor#salToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv4
     */
    @Test
    public void testSalToOFSetNwDst13v4() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1/32")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        ActionConvertor.salToOFSetNwDst(action, actionBuilder, version);
        Assert.assertEquals(SetFieldCase.class.getName(), actionBuilder.getActionChoice().getImplementedInterface().getName());
        SetFieldCase setFieldCase = (SetFieldCase) actionBuilder.getActionChoice();
        MatchEntry matchEntry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        Assert.assertEquals(Ipv4Dst.class.getName(), matchEntry.getOxmMatchField().getName());
        Ipv4DstCase ipv4DstCase = ((Ipv4DstCase) matchEntry.getMatchEntryValue());
        Assert.assertEquals("10.0.0.1", ipv4DstCase.getIpv4Dst().getIpv4Address().getValue());
    }


    /**
     * testing {@link ActionConvertor#salToOFSetNwDst(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv6
     */
    @Test
    public void testSalToOFSetNwDst13v6() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        /* Use canonical form - no leading zeroes and a prefix, even if the prefix is /128 !!! */
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:db8:85a3:42:1000:8a2e:370:7334/128")).build();
        SetNwDstActionCase action = provisionNwDstActionBuilder(address);
        ActionConvertor.salToOFSetNwDst(action, actionBuilder, version);
        Assert.assertEquals(SetFieldCase.class.getName(), actionBuilder.getActionChoice().getImplementedInterface().getName());
        SetFieldCase setFieldCase = (SetFieldCase) actionBuilder.getActionChoice();
        MatchEntry matchEntry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        Assert.assertEquals(Ipv6Dst.class.getName(), matchEntry.getOxmMatchField().getName());
        Ipv6DstCase ipv6DstCase = ((Ipv6DstCase) matchEntry.getMatchEntryValue());
        /* We check the IP only, the netmask should have gone into the wildcard field */
        Assert.assertEquals("2001:db8:85a3:42:1000:8a2e:370:7334", ipv6DstCase.getIpv6Dst().getIpv6Address().getValue());
    }


    /**
     * testing {@link ActionConvertor#salToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv4
     */
    @Test
    public void testSalToOFSetNwSrc10v4() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1/32")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        ActionConvertor.salToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertEquals(SetNwSrcCase.class.getName(), actionBuilder.getActionChoice().getImplementedInterface().getName());
        SetNwSrcCase setNwSrcCase = (SetNwSrcCase) actionBuilder.getActionChoice();
        Assert.assertEquals("10.0.0.1", setNwSrcCase.getSetNwSrcAction().getIpAddress().getValue());
    }

    /**
     * testing {@link ActionConvertor#salToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.0, IPv6
     */
    @Test
    public void testSalToOFSetNwSrc10v6() {
        short version = 1;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        /* Use canonical form - no leading zeroes and a prefix, even if the prefix is /128 !!! */
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:db8:85a3:42:1000:8a2e:370:7334/128")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action ofAction = ActionConvertor.salToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertNull(ofAction);
    }

    /**
     * testing {@link ActionConvertor#salToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv4
     */
    @Test
    public void testSalToOFSetNwSrc13v4() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        address = new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.0.1/32")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        ActionConvertor.salToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertEquals(SetFieldCase.class.getName(), actionBuilder.getActionChoice().getImplementedInterface().getName());
        SetFieldCase setFieldCase = (SetFieldCase) actionBuilder.getActionChoice();
        MatchEntry matchEntry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        Assert.assertEquals(Ipv4Src.class, matchEntry.getOxmMatchField());
        Ipv4SrcCase ipv4SrcCase = ((Ipv4SrcCase) matchEntry.getMatchEntryValue());
        Assert.assertEquals("10.0.0.1", ipv4SrcCase.getIpv4Src().getIpv4Address().getValue());
    }

    /**
     * testing {@link ActionConvertor#salToOFSetNwSrc(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder, short)}
     * with OF-1.3, IPv6
     */
    @Test
    public void testSalToOFSetNwSrc13v6() {
        short version = 4;
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        Address address;
        /* Use canonical form - no leading zeroes and a prefix, even if the prefix is /128 !!! */
        address = new Ipv6Builder().setIpv6Address(new Ipv6Prefix("2001:db8:85a3:42:1000:8a2e:370:7334/128")).build();
        SetNwSrcActionCase action = provisionNwSrcActionBuilder(address);
        ActionConvertor.salToOFSetNwSrc(action, actionBuilder, version);
        Assert.assertEquals(SetFieldCase.class.getName(), actionBuilder.getActionChoice().getImplementedInterface().getName());
        SetFieldCase setFieldCase = (SetFieldCase) actionBuilder.getActionChoice();
        MatchEntry matchEntry = setFieldCase.getSetFieldAction().getMatchEntry().get(0);
        Ipv6SrcCase ipv6SrcCase = ((Ipv6SrcCase) matchEntry.getMatchEntryValue());
        Assert.assertEquals(Ipv6Src.class.getName(), matchEntry.getOxmMatchField().getName());
        /* We check the IP only, the netmask should have gone into the wildcard field */
        Assert.assertEquals("2001:db8:85a3:42:1000:8a2e:370:7334", ipv6SrcCase.getIpv6Src().getIpv6Address().getValue());
    }

    /**
     * testing {@link ActionConvertor#ofToSALPopMplsAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action)}
     * with OF-1.3, IPv6
     */
    @Test
    public void testOFtoSALPopMplsAction() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder actionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder();
        PopMplsCaseBuilder popMplsCaseBuilder = new PopMplsCaseBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsActionBuilder popMplsBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsActionBuilder();
        popMplsBuilder.setEthertype(new EtherType(new EtherType(34888)));
        popMplsCaseBuilder.setPopMplsAction(popMplsBuilder.build());
        actionBuilder.setActionChoice(popMplsCaseBuilder.build());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action action = actionBuilder.build();
        Assert.assertEquals(34888, ActionConvertor.ofToSALPopMplsAction(action).getPopMplsAction().getEthernetType().intValue());
    }

    private static SetNwDstActionCase provisionNwDstActionBuilder(final Address address) {
        SetNwDstAction nwDstAction = new SetNwDstActionBuilder().setAddress(address).build();
        SetNwDstActionCase action = new SetNwDstActionCaseBuilder()
                .setSetNwDstAction(nwDstAction)
                .build();
        return action;
    }

    private static SetNwSrcActionCase provisionNwSrcActionBuilder(final Address address) {
        SetNwSrcAction nwSrcAction = new SetNwSrcActionBuilder().setAddress(address).build();
        SetNwSrcActionCase action = new SetNwSrcActionCaseBuilder()
                .setSetNwSrcAction(nwSrcAction)
                .build();
        return action;
    }

}
