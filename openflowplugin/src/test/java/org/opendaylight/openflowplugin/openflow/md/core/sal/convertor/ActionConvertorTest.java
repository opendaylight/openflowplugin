package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.controller.sal.action.PopVlan;
import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;

public class ActionConvertorTest {

    List<Action> actions = new ArrayList<Action>();
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
        List<ActionsList> OFActionsList = ActionConvertor.getActionList(actions, (short) 0X4);

       // OutputActions(OFActionsList);

    }

    private void setExperimenterData() {

        // TODO:SAL API Missing

    }

    private void setFieldData() {

        SetFieldBuilder setFA = new SetFieldBuilder();

        SetFieldBuilder matchBuilder = setFA;

        matchBuilder.setInPort(2125L);

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

    private void OutputActions(List<ActionsList> oFActionsList) {

        for (int item = 0; item < oFActionsList.size(); item++) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action = oFActionsList
                    .get(item).getAction();

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
        Uri uri = new Uri(NodeConnectorIDType.CONTROLLER);
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

}