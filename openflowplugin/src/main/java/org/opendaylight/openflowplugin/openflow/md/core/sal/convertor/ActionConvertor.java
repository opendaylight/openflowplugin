package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueAction;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.openflow.protocol.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;







/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 *
 */
public final class ActionConvertor {
    private static final Logger logger = LoggerFactory.getLogger(ActionConvertor.class);
    private static final String PREFIX_SEPARATOR = "/";
    private ActionConvertor() {
        // NOOP
    }

    public static List<ActionsList> getActionList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions)

    {
        ActionBuilder  actionBuilder = new ActionBuilder();
        ActionsListBuilder actionsListBuilder = new ActionsListBuilder();
      List<ActionsList> actionsList = new ArrayList<ActionsList>();

        for (int actionItem = 0; actionItem < actions.size(); actionItem++)
      {

            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = actions.get(
                    actionItem).getAction();

            if (action instanceof OutputAction)
               actionsList.add(salToOFOutputAction(action,actionBuilder,actionsListBuilder));
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
            ActionsListBuilder actionsListBuilder) {


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

            // portAction.setPort(new PortNumber((long)
            // OFPort.OFPP_CONTROLLER.getValue()));

        }

        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
        actionBuilder.addAugmentation(PortAction.class, portAction.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }
}
