package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.controller.sal.action.PopVlan;
import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsAction;
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
        List<ActionsList> OFActionsList = ActionConvertor.getActionList(actions);

        OutputActions(OFActionsList);

    }

    private void setExperimenterData() {

        // TODO:SAL API Missing

    }

    private void setFieldData() {

        SetFieldBuilder setFA = new SetFieldBuilder();

        MatchBuilder matchBuilder = new MatchBuilder();

        matchBuilder.setInPort(2125L);

        SetFieldBuilder setFB = new SetFieldBuilder();
        setFB.setMatch(matchBuilder.build());

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(setFB.build());

        actions.add(actionItem++, AB.build());

    }

    private void pbbActionData() {
        PushPbbActionBuilder pushpbb = new PushPbbActionBuilder();
        pushpbb.setEthernetType(10);

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(pushpbb.build());

        actions.add(actionItem++, AB.build());

        PopPbbActionBuilder popPBB = new PopPbbActionBuilder();

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(popPBB.build());

        actions.add(actionItem++, AB1.build());

    }

    private void NwTtlAction() {
        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();

        setNwTtlActionBuilder.setNwTtl((short) 1);
        ActionBuilder AB = new ActionBuilder();
        AB.setAction(setNwTtlActionBuilder.build());

        actions.add(actionItem++, AB.build());

        DecNwTtlBuilder necNwTtlBuilder = new DecNwTtlBuilder();


        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(necNwTtlBuilder.build());

        actions.add(actionItem++, AB1.build());

    }

    private void setGroupAction() {

        GroupActionBuilder grpIdAB = new GroupActionBuilder();
        grpIdAB.setGroup("98");

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(grpIdAB.build());

        actions.add(actionItem++, AB.build());

    }

    private void OutputActions(List<ActionsList> oFActionsList) {

        for (int item = 0; item < oFActionsList.size(); item++) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action = oFActionsList
                    .get(item).getAction();

            if (action.getType().equals(Output.class)) {
                Assert.assertEquals((Integer) 10, (action.getAugmentation(MaxLengthAction.class)).getMaxLength());
                // TOD0: OF needs to changed,once that is done ,revalidation of
                // the data required.
                // Assert.assertEquals(-3, (long)
                // (action.getAugmentation(PortAction.class)).getPort().getValue());
                // // short

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

                Assert.assertEquals(action.getType(), SetNwTtl.class);

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

            if (action.getType().equals(SetField.class)) {

                OxmFieldsAction sf = action.getAugmentation(OxmFieldsAction.class);

                Assert.assertEquals(OpenflowBasicClass.class, sf.getMatchEntries().get(0).getOxmClass());

                if (sf.getMatchEntries().get(0).getOxmMatchField().equals(InPort.class)) {
                    Assert.assertEquals(2125, sf.getMatchEntries().get(0).getAugmentation(PortNumberMatchEntry.class)
                            .getPortNumber().getValue().intValue());


                    }

            }

        }

    }

    private void OutputActionData() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionBuilder outputB = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionBuilder();
        outputB.setMaxLength(10);
        Uri uri = new Uri(NodeConnectorIDType.CONTROLLER);
        outputB.setOutputNodeConnector(uri);

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(outputB.build());

        actions.add(actionItem++, AB.build());

    }

    private void CopyTtlData() {
        CopyTtlOutBuilder copyB = new CopyTtlOutBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(copyB.build());

        actions.add(actionItem++, AB.build());

        CopyTtlInBuilder copyTtlInBuilder = new CopyTtlInBuilder();

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(copyTtlInBuilder.build());

        actions.add(actionItem++, AB1.build());

    }

    private void MplsTtlActionData() {

        SetMplsTtlActionBuilder setMplsTtlActionB = new SetMplsTtlActionBuilder();

        setMplsTtlActionB.setMplsTtl((short) 10);
        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(setMplsTtlActionB.build());

        actions.add(actionItem++, AB1.build());

        DecMplsTtlBuilder decMplsTtlB = new DecMplsTtlBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(decMplsTtlB.build());

        actions.add(actionItem++, AB1.build());
    }

    private void vlanActionData() {
        PushVlanActionBuilder pvB = new PushVlanActionBuilder();

        pvB.setVlanId(new VlanId(10));

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(pvB.build());

        actions.add(actionItem++, AB1.build());

        PopVlanActionBuilder popVAB = new PopVlanActionBuilder();

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(popVAB.build());

        actions.add(actionItem++, AB.build());

    }

    private void mplsActionData() {

        PushMplsActionBuilder pushMB = new PushMplsActionBuilder();
        pushMB.setEthernetType(10);

        ActionBuilder AB = new ActionBuilder();
        AB.setAction(pushMB.build());

        actions.add(actionItem++, AB.build());

        PopMplsActionBuilder popMB = new PopMplsActionBuilder();
        popMB.setEthernetType(10);

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(popMB.build());

        actions.add(actionItem++, AB1.build());
    }

    private void setQueueActionData() {

        SetQueueActionBuilder setQB = new SetQueueActionBuilder();
        setQB.setQueue("99");

        ActionBuilder AB1 = new ActionBuilder();
        AB1.setAction(setQB.build());

        actions.add(actionItem++, AB1.build());

    }

}