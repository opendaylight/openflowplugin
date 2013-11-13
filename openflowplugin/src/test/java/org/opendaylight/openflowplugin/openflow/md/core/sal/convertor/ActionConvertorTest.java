package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaxLengthAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;

public class ActionConvertorTest {



    List<Actions> actions = new ArrayList<Actions>();
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

        List<ActionsList> OFActionsList = ActionConvertor.getActionList(actions);

        OutputActions(OFActionsList);

    }

    private void setGroupAction() {

        GroupActionBuilder grpIdAB = new GroupActionBuilder();
        grpIdAB.setGroup("98");
        GroupAction grpIdA = grpIdAB.build();
        ActionsBuilder actionsB = new ActionsBuilder();
        actionsB.setAction(grpIdA);
        Actions action1 = actionsB.build();
        actions.add(actionItem++, action1);

    }

    private void OutputActions(List<ActionsList> oFActionsList) {
        for (int item = 0; item < oFActionsList.size(); item++) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.action.header.Action action = oFActionsList
                    .get(item).getAction();

            if (action.getType().equals(Output.class)) {
                Assert.assertEquals((Integer) 10, (action.getAugmentation(MaxLengthAction.class)).getMaxLength());
                // TOD0: OF needs to changed,once that is done ,revalidation of
                // the data required.
                Assert.assertEquals(-3, (long) (action.getAugmentation(PortAction.class)).getPort().getValue()); // short

            }
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class)) {
                Assert.assertEquals(action.getClass(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);

            }
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class)) {
                Assert.assertEquals(action.getClass(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);

            }

            if (action.getType().equals(
            // TODO:getMplsTtl is missing.
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class)) {
                Assert.assertEquals(action.getClass(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class);

            }
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class)) {
                Assert.assertEquals(action.getClass(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);

            }
            if (action.getType().equals(

                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class)) {

                Assert.assertEquals(10, (action.getAugmentation(EthertypeAction.class)).getEthertype());

            }
            if (action.getType().equals(
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class)) {
                Assert.assertEquals(10, (action.getAugmentation(EthertypeAction.class)).getEthertype());

            }

            if (action.getType().equals(

            // TODO:SetQueue,I dont have getQueueId
                    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class)) {
                Assert.assertEquals(action.getClass(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class);
            }

            if (action.getType().equals(

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction.class)) {

                Assert.assertEquals(98, (long) (action.getAugmentation(GroupIdAction.class)).getGroupId());

            }
        }

    }

    private void OutputActionData() {

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionBuilder outputB = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionBuilder();
        outputB.setMaxLength(10);
        List<Uri> uriList =new ArrayList<Uri>();
        uriList.add(new Uri(NodeConnectorIDType.CONTROLLER));
        outputB.setOutputNodeConnector(uriList);
        OutputAction outputAction = outputB.build();
        ActionsBuilder actionsB= new ActionsBuilder();
        actionsB.setAction(outputAction);
        Actions action = actionsB.build();
        actions.add(actionItem++, action);
    }

    private void CopyTtlData() {
        CopyTtlOutBuilder copyB = new CopyTtlOutBuilder();

        CopyTtlOut copy = copyB.build();
        ActionsBuilder actionsB = new ActionsBuilder();
        actionsB.setAction(copy);
        Actions action = actionsB.build();
        actions.add(actionItem++, action);

        CopyTtlInBuilder copyTtlInBuilder = new CopyTtlInBuilder();

        CopyTtlIn copyIn = copyTtlInBuilder.build();

        ActionsBuilder actionsB1 = new ActionsBuilder();
        actionsB.setAction(copyIn);
        Actions action1 = actionsB.build();
        actions.add(actionItem++, action1);

    }

    private void MplsTtlActionData() {

        SetMplsTtlActionBuilder setMplsTtlActionB = new SetMplsTtlActionBuilder();

        setMplsTtlActionB.setMplsTtl((short) 10);

        SetMplsTtlAction setMp = setMplsTtlActionB.build();
        ActionsBuilder actionsB  = new ActionsBuilder();
        actionsB.setAction(setMp);
        Actions action = actionsB.build();
        actions.add(actionItem++, action);


        DecMplsTtlBuilder decMplsTtlB = new DecMplsTtlBuilder();
        DecMplsTtl decMplsTtl = decMplsTtlB.build();
        ActionsBuilder actionsB1 = new ActionsBuilder();
        actionsB.setAction(decMplsTtl);
        Actions action1 = actionsB1.build();
        actions.add(actionItem++, action1);
    }


    private void vlanActionData() {
        // PushVlanActionBuilder pvB = new PushVlanActionBuilder();
        // TODO:SAL API waiting...needs to changed to etherType
        // pvB.setEthernetType(10);
        // PushVlanAction pv = pvB.build();
        // ActionsBuilder actionsB = new ActionsBuilder();
        // actionsB.setAction(pv);
        // Actions action = actionsB.build();
        // actions.add(actionItem++, action);

        PopVlanActionBuilder popVAB = new PopVlanActionBuilder();
        ActionsBuilder actionsB1 = new ActionsBuilder();
        PopVlanAction popVA = popVAB.build();
        actionsB1.setAction(popVA);
        Actions action = actionsB1.build();
        actions.add(actionItem++, action);
    }
    private void mplsActionData() {

        PushMplsActionBuilder pushMB = new PushMplsActionBuilder();
        pushMB.setEthernetType(10);
        PushMplsAction pushM = pushMB.build();

        ActionsBuilder actionsB = new ActionsBuilder();
        actionsB.setAction(pushM);
        Actions action1 = actionsB.build();
        actions.add(actionItem++, action1);


        PopMplsActionBuilder popMB = new PopMplsActionBuilder();
        popMB.setEthernetType(10);
        PopMplsAction popM = popMB.build();

        ActionsBuilder actionsB1 = new ActionsBuilder();
        actionsB1.setAction(pushM);
        Actions action = actionsB1.build();
        actions.add(actionItem++, action);
    }


    private void setQueueActionData() {

        SetQueueActionBuilder setQB = new SetQueueActionBuilder();
        setQB.setQueue("99");

        SetQueueAction setQA = setQB.build();

        ActionsBuilder actionsB = new ActionsBuilder();
        actionsB.setAction(setQA);
        Actions action1 = actionsB.build();
        actions.add(actionItem++, action1);

    }




}