/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;

/**
 * test for {@link ActionConvertor}
 */
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
        dropActionData();

        ActionConvertorData data = new ActionConvertorData((short) 0X4);
        data.setDatapathId(BigInteger.ONE);
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action>> OFActionsList =
                convertorManager.convert(actions, data);

        outputActions(OFActionsList.orElse(Collections.emptyList()));

    }

    private void setExperimenterData() {

        // TODO:SAL API Missing

    }

    private void dropActionData() {
        ActionBuilder AB = new ActionBuilder();
        AB.setOrder(actionItem).setAction(new DropActionCaseBuilder().build());

        actions.add(actionItem++, AB.build());

    }

    private void setFieldData() {

        SetFieldBuilder matchBuilder = new SetFieldBuilder();

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

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action action : oFActionsList) {

            if (action.getActionChoice() instanceof OutputActionCase) {
                OutputActionCase outputActionCase = (OutputActionCase) action.getActionChoice();
                Assert.assertEquals((Integer) 10, (outputActionCase.getOutputAction().getMaxLength()));
                long port = 4294967293L;
                Assert.assertEquals(port, (long) (outputActionCase.getOutputAction().getPort().getValue()));


            }
            if (action.getActionChoice() instanceof CopyTtlInCase) {
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

        actions.add(actionItem++, AB.build());
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

    private void setQueueActionData() {

        SetQueueActionBuilder setQB = new SetQueueActionBuilder();
        setQB.setQueue("99");

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setOrder(actionItem).setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQB.build()).build());

        actions.add(actionItem++, AB1.build());

    }

}
