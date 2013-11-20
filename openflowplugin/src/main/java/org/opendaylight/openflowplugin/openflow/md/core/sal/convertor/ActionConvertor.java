package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthertypeActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.GroupIdActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.QueueIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.QueueIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValuesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.openflow.protocol.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 * @author avishnoi@in.ibm.com  Added convertor for OF bucket actions to SAL actions 
 *
 */
public final class ActionConvertor {
    private static final Logger logger = LoggerFactory.getLogger(ActionConvertor.class);
    private static final String PREFIX_SEPARATOR = "/";
    private ActionConvertor() {
        // NOOP
    }

    public static List<ActionsList> getActionList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions,
            short version)

    {
        ActionBuilder  actionBuilder = new ActionBuilder();
        ActionsListBuilder actionsListBuilder = new ActionsListBuilder();
      List<ActionsList> actionsList = new ArrayList<ActionsList>();

        for (int actionItem = 0; actionItem < actions.size(); actionItem++)
      {

            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = actions.get(
                    actionItem).getAction();

            if (action instanceof OutputAction)
                actionsList.add(salToOFOutputAction(action, actionBuilder, actionsListBuilder, version));
            else if (action instanceof GroupAction)
                actionsList.add(SalToOFGroupAction(action, actionBuilder, actionsListBuilder));
            else if (action instanceof CopyTtlOut)
                actionsList.add(SalToOFCopyTTLIOut(actionBuilder, actionsListBuilder));
            else if (action instanceof CopyTtlIn)
                actionsList.add(SalToOFCopyTTLIIn(actionBuilder, actionsListBuilder));
            else if (action instanceof SetMplsTtlAction)
                actionsList.add(SalToOFSetMplsTtl(action, actionBuilder, actionsListBuilder));
            else if (action instanceof DecMplsTtl)
                actionsList.add(SalToOFDecMplsTtl(actionBuilder, actionsListBuilder));
            else if (action instanceof PushVlanAction)
                actionsList.add(SalToOFPushVlanAction(action, actionBuilder, actionsListBuilder));
            else if (action instanceof PopVlanAction)
                actionsList.add(SalToOFPopVlan(action, actionBuilder, actionsListBuilder));
            else if (action instanceof PushMplsAction)
                actionsList.add(SalToOFPushMplsAction(action, actionBuilder, actionsListBuilder));
            else if (action instanceof PopMplsAction)
                actionsList.add(SalToOFPopMpls(action, actionBuilder, actionsListBuilder));
            else if (action instanceof SetQueueAction)
                actionsList.add(SalToOFSetQueue(action, actionBuilder, actionsListBuilder));

            else if (action instanceof SetNwTtlAction)
                actionsList.add(SalToOFSetNwTtl(action, actionBuilder, actionsListBuilder));
            else if (action instanceof DecNwTtl)
                actionsList.add(SalToOFDecNwTtl(action, actionBuilder, actionsListBuilder));
            else if (action instanceof SetField)
                actionsList.add(SalToOFSetField(action, actionBuilder, actionsListBuilder));

            else if (action instanceof PushPbbAction)
                actionsList.add(SalToOFPushPbbAction(action, actionBuilder, actionsListBuilder));
            else if (action instanceof PopPbbAction)
                actionsList.add(SalToOFPopPBB(action, actionBuilder, actionsListBuilder));
            else if (action instanceof ExperimenterAction)
                actionsList.add(SalToOFExperimenter(action, actionBuilder, actionsListBuilder));

        }
        return actionsList;


    }


    private static ActionsList SalToOFSetField(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetField setField = (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetField) action;
        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match = setField.getMatch();

        List<MatchEntries> matchEntries = FlowConvertor.toMatch(match);

        OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();

        oxmFieldsActionBuilder.setMatchEntries(matchEntries);

        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);

        actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();

    }

    private static ActionsList SalToOFDecNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {
        actionBuilder.setType(DecNwTtl.class);

        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPushMplsAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        actionBuilder.setType(PushMpls.class);

        return SalToOFPushAction(((PushMplsAction) action).getEthernetType(), actionBuilder, actionsListBuilder);

    }

    private static ActionsList SalToOFPushPbbAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {
        actionBuilder.setType(PushPbb.class);

        return SalToOFPushAction(((PushPbbAction) action).getEthernetType(), actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPushVlanAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        PushVlanAction pushVlanAction = (PushVlanAction) action;
        VlanId vlanId = new VlanId(pushVlanAction.getVlanId());
        Integer etherType = vlanId.getValue();
        actionBuilder.setType(PushVlan.class);

        return SalToOFPushAction(etherType, actionBuilder, actionsListBuilder);

    }

    private static ActionsList SalToOFSetNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        NwTtlActionBuilder nwTtlActionBuilder = new NwTtlActionBuilder();
        nwTtlActionBuilder.setNwTtl(((SetNwTtlAction) action).getNwTtl());


        actionBuilder.setType(SetNwTtl.class);
        actionBuilder.addAugmentation(NwTtlAction.class, nwTtlActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();

    }

    private static ActionsList SalToOFSetQueue(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        SetQueueAction setQueueAction = (SetQueueAction) action;

        QueueIdActionBuilder queueIdActionBuilder = new QueueIdActionBuilder();
        queueIdActionBuilder.setQueueId(Long.getLong(setQueueAction.getQueue()));
        actionBuilder.setType(SetQueue.class);
        actionBuilder.addAugmentation(QueueIdAction.class, queueIdActionBuilder.build());

        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }

    private static ActionsList SalToOFPopMpls(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        actionBuilder.setType(PopMpls.class);

        return SalToOFPushAction(((PopMplsAction) action).getEthernetType(), actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPopVlan(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        actionBuilder.setType(PushMpls.class);

        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPopPBB(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {
        actionBuilder.setType(PopPbb.class);
        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFExperimenter(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        ExperimenterActionBuilder experimenterActionBuilder = new ExperimenterActionBuilder();
        experimenterActionBuilder.setExperimenter(((ExperimenterAction) action).getExperimenter());
        actionBuilder.setType(Experimenter.class);
        actionBuilder
                .addAugmentation(
                        ExperimenterAction.class,
                        (Augmentation<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action>) experimenterActionBuilder);
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();

    }

    private static ActionsList SalToOFGroupAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        GroupAction groupAction = (GroupAction) action;

        GroupIdActionBuilder groupIdBuilder = new GroupIdActionBuilder();
        groupIdBuilder.setGroupId(Long.getLong(groupAction.getGroup()));
        actionBuilder.setType(Group.class);
        actionBuilder.addAugmentation(GroupIdAction.class, groupIdBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }

    private static ActionsList SalToOFPushAction(Integer ethernetType, ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        EthertypeActionBuilder ethertypeActionBuilder = new EthertypeActionBuilder();
        ethertypeActionBuilder.setEthertype(new EtherType(ethernetType));

        /* OF */
        actionBuilder.addAugmentation(EthertypeAction.class, ethertypeActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }

    private static ActionsList SalToOFDecMplsTtl(ActionBuilder actionBuilder, ActionsListBuilder actionsListBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);
        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFSetMplsTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
            ActionsListBuilder actionsListBuilder) {

        SetMplsTtlAction mplsTtlAction = (SetMplsTtlAction) action;

        MplsTtlActionBuilder mplsTtlActionBuilder = new MplsTtlActionBuilder();
        mplsTtlActionBuilder.setMplsTtl(mplsTtlAction.getMplsTtl()/* SAL */);
        /* OF */
        actionBuilder.setType(SetMplsTtl.class);
        actionBuilder.addAugmentation(MplsTtlAction.class, mplsTtlActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }

    private static ActionsList SalToOFCopyTTLIIn(ActionBuilder actionBuilder, ActionsListBuilder actionsListBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);
        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFCopyTTLIOut(ActionBuilder actionBuilder, ActionsListBuilder actionsListBuilder) {
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);
        return emtpyAction(actionBuilder, actionsListBuilder);

    }

    private static ActionsList emtpyAction(ActionBuilder actionBuilder, ActionsListBuilder actionsListBuilder) {

        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }

    private static ActionsList salToOFOutputAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionBuilder actionBuilder,
 ActionsListBuilder actionsListBuilder, short version) {


        OutputAction outputAction = ((OutputAction) action);
        PortActionBuilder portAction = new PortActionBuilder();
        MaxLengthActionBuilder maxLenActionBuilder = new MaxLengthActionBuilder();
        maxLenActionBuilder.setMaxLength(outputAction.getMaxLength());

        actionBuilder.addAugmentation(MaxLengthAction.class, maxLenActionBuilder.build());

        Uri uri = outputAction.getOutputNodeConnector();
            if (uri.getValue() == NodeConnectorIDType.ALL)
                portAction.setPort(new PortNumber((long) OFPort.OFPP_ALL.getValue()));

            if (uri.getValue() == NodeConnectorIDType.SWSTACK)
                portAction.setPort(new PortNumber((long) OFPort.OFPP_LOCAL.getValue()));

            if ((uri.getValue() == NodeConnectorIDType.HWPATH)
 || (uri.getValue() == NodeConnectorIDType.ONEPK)
                    || (uri.getValue() == NodeConnectorIDType.ONEPK2OPENFLOW)
                    || (uri.getValue() == NodeConnectorIDType.ONEPK2PCEP)
                    || (uri.getValue() == NodeConnectorIDType.OPENFLOW)
                    || (uri.getValue() == NodeConnectorIDType.OPENFLOW2ONEPK)
                    || (uri.getValue() == NodeConnectorIDType.OPENFLOW2PCEP)
                    || (uri.getValue() == NodeConnectorIDType.PCEP)
                    || (uri.getValue() == NodeConnectorIDType.PCEP2ONEPK)
                    || (uri.getValue() == NodeConnectorIDType.PCEP2OPENFLOW)
                || (uri.getValue() == NodeConnectorIDType.PRODUCTION)) {
                portAction.setPort(new PortNumber((long) OFPort.OFPP_NORMAL.getValue()));
            }

            if (uri.getValue() == NodeConnectorIDType.CONTROLLER) {

            if (version == 0X4) {
                // TODO:To remove the and operation once the BitContent is in
                // place in OF Plugin .
                portAction.setPort(new PortNumber(PortNumberValues.CONTROLLER.getIntValue() & 0x00000000ffffffffL));
            } else {
                portAction.setPort(new PortNumber((long) PortNumberValuesV10.CONTROLLER.getIntValue()));

            }
        }

        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
        actionBuilder.addAugmentation(PortAction.class, portAction.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }
    
    /**
     * Method to convert OF actions associated with bucket to SAL Actions.
     * @param actionList
     * @return List of converted SAL Actions.
     */
    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> toSALBucketActions(List<ActionsList> actionList){
        
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> bucketActions = 
                new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>();
        
        for(ActionsList actionDesc : actionList){
            
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action 
            = actionDesc.getAction();

            if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output)
                bucketActions.add(ofToSALOutputAction(action));
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group)
                bucketActions.add(ofToSALGroupAction(action));
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut){
                CopyTtlOutBuilder copyTtlOutaction = new CopyTtlOutBuilder();
                bucketActions.add(copyTtlOutaction.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn){
                CopyTtlInBuilder copyTtlInaction = new CopyTtlInBuilder();
                bucketActions.add(copyTtlInaction.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl)
                bucketActions.add(ofToSALSetMplsTtl(action));
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl){
                DecMplsTtlBuilder decMplsTtl = new DecMplsTtlBuilder(); 
                bucketActions.add(decMplsTtl.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan)
                bucketActions.add(ofToSALPushVlanAction(action));
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan){
                PopVlanActionBuilder popVlan = new PopVlanActionBuilder();
                bucketActions.add(popVlan.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls){
                PushMplsActionBuilder pushMpls = new PushMplsActionBuilder(); 
                bucketActions.add(pushMpls.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls){
                PopMplsActionBuilder popMpls = new PopMplsActionBuilder(); 
                bucketActions.add(popMpls.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue)
                bucketActions.add(ofToSALSetQueue(action));

            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl)
                bucketActions.add(ofToSALSetNwTtl(action));
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl){
                DecNwTtlBuilder decNwTtl = new DecNwTtlBuilder();
                bucketActions.add(decNwTtl.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField)
                bucketActions.add(FlowConvertor.ofToSALSetField(action));

            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb)
                bucketActions.add(ofToSALPushPbbAction(action));
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb){
                PopPbbActionBuilder popPbb = new PopPbbActionBuilder(); 
                bucketActions.add(popPbb.build());
            }
            else if (action instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter){
                //bucketActions.add(ofToSALExperimenter(action));
                // TODO: Need to explore/discuss on how to handle experimenter case.
            }

        }
        return bucketActions;
    }
    
    /**
     * Method converts OF Output action object to SAL Output action object. 
     * @param action org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action
     * @return OutputAction
     */
    public static OutputAction ofToSALOutputAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        OutputActionBuilder outputAction = new OutputActionBuilder();
        PortAction port = action.getAugmentation(PortAction.class);
        if(port != null){
            outputAction.setOutputNodeConnector(new Uri(port.getPort().getValue().toString()));
        }else {
            logger.error("Provided action is not OF Output action, no associated port found!" );
        }
        
        MaxLengthAction length = action.getAugmentation(MaxLengthAction.class);
        if(length != null){
            outputAction.setMaxLength(length.getMaxLength());
        }else{
            logger.error("Provided action is not OF Output action, no associated length found!");
        }

        return outputAction.build();
    }

    /**
     * Method converts OF GroupAction object to SAL GroupAction object
     * @param action
     * @return GroupAction
     */
    public static GroupAction ofToSALGroupAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        GroupActionBuilder groupAction = new GroupActionBuilder();
        
        GroupIdAction groupId = action.getAugmentation(GroupIdAction.class);
        groupAction.setGroupId(groupId.getGroupId());
        
        return groupAction.build();
    }
    
    /**
     * Method converts OF SetMplsTTL action object to SAL SetMplsTTL action object.
     * @param action
     * @return
     */
    public static SetMplsTtlAction ofToSALSetMplsTtl(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action){

        SetMplsTtlActionBuilder mplsTtlAction = new SetMplsTtlActionBuilder();
        MplsTtlAction mplsTtl = action.getAugmentation(MplsTtlAction.class);
        mplsTtlAction.setMplsTtl(mplsTtl.getMplsTtl());
        return mplsTtlAction.build();
    }
    
    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     * @param action
     * @return PushVlanAction
     */
    public static PushVlanAction ofToSALPushVlanAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        PushVlanActionBuilder pushVlanAction = new PushVlanActionBuilder();
        
        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushVlanAction.setVlanId(new VlanId(etherType.getEthertype().getValue()));
        
        return pushVlanAction.build();
    }
    
    /**
     * Method converts OF SetQueue action to SAL SetQueue action.
     * @param action
     * @return SetQueueAction
     */
    public static SetQueueAction ofToSALSetQueue(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        SetQueueActionBuilder setQueueAction = new SetQueueActionBuilder();
        
        QueueIdAction queueId = action.getAugmentation(QueueIdAction.class);
        setQueueAction.setQueueId(queueId.getQueueId());
        
        return setQueueAction.build();
    }

    /**
     * Method converts OF SetNwTtl action to SAL SetNwTtl action.
     * @param action
     * @return SetNwTtlAction
     */
    public static SetNwTtlAction ofToSALSetNwTtl(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        SetNwTtlActionBuilder setNwTtl = new SetNwTtlActionBuilder();
        NwTtlAction nwTtl = action.getAugmentation(NwTtlAction.class);
        setNwTtl.setNwTtl(nwTtl.getNwTtl());
        
        return setNwTtl.build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     * @param action
     * @return PushVlanAction
     */
    public static PushPbbAction ofToSALPushPbbAction(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        PushPbbActionBuilder pushPbbAction = new PushPbbActionBuilder();
        
        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushPbbAction.setEthernetType(etherType.getEthertype().getValue());
        
        return pushPbbAction.build();
    }
    
    public static Object ofToSALExperimenter(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action){

        ExperimenterAction ExperimenterAction = action.getAugmentation(ExperimenterAction.class);
        
        return null;
        /*
         * TODO: Need to explore/discuss about how to handle experimenter
         * 
         */

    }
}
