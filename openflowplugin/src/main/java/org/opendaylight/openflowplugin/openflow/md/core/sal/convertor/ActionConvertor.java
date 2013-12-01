package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 * @author avishnoi@in.ibm.com Added convertor for OF bucket actions to SAL
 *         actions
 *
 */
public final class ActionConvertor {
    private static final Logger logger = LoggerFactory.getLogger(ActionConvertor.class);
    private static final String PREFIX_SEPARATOR = "/";
    final private static short OF10 = 1;
    final private static short OF13 = 4;

    private ActionConvertor() {
        // NOOP
    }

    public static List<ActionsList> getActionList(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions,
            short version)

    {
        ActionsListBuilder actionsListBuilder = new ActionsListBuilder();
        List<ActionsList> actionsList = new ArrayList<ActionsList>();

        for (int actionItem = 0; actionItem < actions.size(); actionItem++) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = actions.get(
                    actionItem).getAction();

            if (action instanceof OutputActionCase)
                actionsList.add(salToOFOutputAction(action, actionsListBuilder, version));
            else if (action instanceof GroupActionCase)
                actionsList.add(SalToOFGroupAction(action, actionsListBuilder));
            else if (action instanceof CopyTtlOutCase)
                actionsList.add(SalToOFCopyTTLIOut(actionsListBuilder));
            else if (action instanceof CopyTtlIn)
                actionsList.add(SalToOFCopyTTLIIn(actionsListBuilder));
            else if (action instanceof SetMplsTtlAction)
                actionsList.add(SalToOFSetMplsTtl(action, actionsListBuilder));
            else if (action instanceof DecMplsTtl)
                actionsList.add(SalToOFDecMplsTtl(actionsListBuilder));
            else if (action instanceof PushVlanAction)
                actionsList.add(SalToOFPushVlanAction(action, actionsListBuilder));
            else if (action instanceof PopVlanAction)
                actionsList.add(SalToOFPopVlan(action, actionsListBuilder));
            else if (action instanceof PushMplsAction)
                actionsList.add(SalToOFPushMplsAction(action, actionsListBuilder));
            else if (action instanceof PopMplsAction)
                actionsList.add(SalToOFPopMpls(action, actionsListBuilder));
            else if (action instanceof SetQueueAction)
                actionsList.add(SalToOFSetQueue(action, actionsListBuilder));

            else if (action instanceof SetNwTtlAction)
                actionsList.add(SalToOFSetNwTtl(action, actionsListBuilder));
            else if (action instanceof DecNwTtl)
                actionsList.add(SalToOFDecNwTtl(action, actionsListBuilder));
            else if (action instanceof SetField)
                actionsList.add(SalToOFSetField(action, actionsListBuilder));

            else if (action instanceof PushPbbAction)
                actionsList.add(SalToOFPushPbbAction(action, actionsListBuilder));
            else if (action instanceof PopPbbAction)
                actionsList.add(SalToOFPopPBB(action, actionsListBuilder));
            else if (action instanceof ExperimenterAction)
                actionsList.add(SalToOFExperimenter(action, actionsListBuilder));

        }
        return actionsList;

    }

    private static ActionsList SalToOFSetField(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField setField = (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField) action;
        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match = setField;

        List<MatchEntries> matchEntries = MatchConvertor.toMatch(match);

        OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();

        oxmFieldsActionBuilder.setMatchEntries(matchEntries);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);

        actionBuilder.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();

    }

    private static ActionsList SalToOFDecNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class);

        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPushMplsAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(PushMpls.class);

        return SalToOFPushAction(((PushMplsAction) action).getEthernetType(), actionBuilder, actionsListBuilder);

    }

    private static ActionsList SalToOFPushPbbAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(PushPbb.class);

        return SalToOFPushAction(((PushPbbAction) action).getEthernetType(), actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPushVlanAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        PushVlanAction pushVlanAction = (PushVlanAction) action;
        VlanId vlanId = new VlanId(pushVlanAction.getVlanId());
        Integer etherType = vlanId.getValue();
        actionBuilder.setType(PushVlan.class);

        return SalToOFPushAction(etherType, actionBuilder, actionsListBuilder);

    }

    private static ActionsList SalToOFSetNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        NwTtlActionBuilder nwTtlActionBuilder = new NwTtlActionBuilder();
        nwTtlActionBuilder.setNwTtl(((SetNwTtlAction) action).getNwTtl());

        actionBuilder.setType(SetNwTtl.class);
        actionBuilder.addAugmentation(NwTtlAction.class, nwTtlActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();

    }

    private static ActionsList SalToOFSetQueue(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
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
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(PopMpls.class);

        return SalToOFPushAction(((PopMplsAction) action).getEthernetType(), actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPopVlan(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(PopVlan.class);

        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFPopPBB(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setType(PopPbb.class);
        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFExperimenter(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {

        ActionBuilder actionBuilder = new ActionBuilder();
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
            ActionsListBuilder actionsListBuilder) {

        GroupActionCase groupActionCase = (GroupActionCase) action;
        GroupAction groupAction = groupActionCase.getGroupAction();

        GroupIdActionBuilder groupIdBuilder = new GroupIdActionBuilder();
        groupIdBuilder.setGroupId(Long.getLong(groupAction.getGroup()));
        ActionBuilder actionBuilder = new ActionBuilder();
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

    private static ActionsList SalToOFDecMplsTtl(ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);
        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFSetMplsTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();

        SetMplsTtlAction mplsTtlAction = (SetMplsTtlAction) action;

        MplsTtlActionBuilder mplsTtlActionBuilder = new MplsTtlActionBuilder();
        mplsTtlActionBuilder.setMplsTtl(mplsTtlAction.getMplsTtl()/* SAL */);
        /* OF */
        actionBuilder.setType(SetMplsTtl.class);
        actionBuilder.addAugmentation(MplsTtlAction.class, mplsTtlActionBuilder.build());
        actionsListBuilder.setAction(actionBuilder.build());
        return actionsListBuilder.build();
    }

    private static ActionsList SalToOFCopyTTLIIn(ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder
                .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);
        return emtpyAction(actionBuilder, actionsListBuilder);
    }

    private static ActionsList SalToOFCopyTTLIOut(ActionsListBuilder actionsListBuilder) {
        ActionBuilder actionBuilder = new ActionBuilder();
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
            ActionsListBuilder actionsListBuilder, short version) {


        OutputActionCase outputActionCase = ((OutputActionCase) action);
        OutputAction outputAction = outputActionCase.getOutputAction();
        PortActionBuilder portAction = new PortActionBuilder();
        MaxLengthActionBuilder maxLenActionBuilder = new MaxLengthActionBuilder();
        maxLenActionBuilder.setMaxLength(outputAction.getMaxLength());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.addAugmentation(MaxLengthAction.class, maxLenActionBuilder.build());

        Uri uri = outputAction.getOutputNodeConnector();
        if (uri.getValue() == NodeConnectorIDType.ALL) {
            if (version >= OF13) {
                portAction.setPort(new PortNumber(BinContent.intToUnsignedLong(PortNumberValues.ALL.getIntValue())));

            } else if (version == OF10) {
                portAction.setPort(new PortNumber((long) PortNumberValuesV10.ALL.getIntValue()));

            }
        }
        if (uri.getValue() == NodeConnectorIDType.SWSTACK) {
            if (version >= OF13) {
                portAction.setPort(new PortNumber(BinContent.intToUnsignedLong(PortNumberValues.LOCAL.getIntValue())));

            } else if (version == OF10) {
                portAction.setPort(new PortNumber((long) PortNumberValuesV10.LOCAL.getIntValue()));

            }
        }

        if ((uri.getValue() == NodeConnectorIDType.HWPATH) || (uri.getValue() == NodeConnectorIDType.ONEPK)
                || (uri.getValue() == NodeConnectorIDType.ONEPK2OPENFLOW)
                || (uri.getValue() == NodeConnectorIDType.ONEPK2PCEP)
                || (uri.getValue() == NodeConnectorIDType.OPENFLOW)
                || (uri.getValue() == NodeConnectorIDType.OPENFLOW2ONEPK)
                || (uri.getValue() == NodeConnectorIDType.OPENFLOW2PCEP)
                || (uri.getValue() == NodeConnectorIDType.PCEP) || (uri.getValue() == NodeConnectorIDType.PCEP2ONEPK)
                || (uri.getValue() == NodeConnectorIDType.PCEP2OPENFLOW)
                || (uri.getValue() == NodeConnectorIDType.PRODUCTION)) {
            if (version >= OF13) {
                portAction.setPort(new PortNumber((long) PortNumberValuesV10.NORMAL.getIntValue()));

            } else if (version == OF10) {
                portAction.setPort(new PortNumber((long) PortNumberValuesV10.NORMAL.getIntValue()));
            }
        }

        if (uri.getValue() == NodeConnectorIDType.CONTROLLER) {

            if (version >= OF13) {
                portAction.setPort(new PortNumber(BinContent.intToUnsignedLong(PortNumberValues.CONTROLLER
                        .getIntValue())));

            } else if (version == OF10) {
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
     *
     * @param actionList
     * @return List of converted SAL Actions.
     */
    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> toSALBucketActions(
            List<ActionsList> actionList) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> bucketActions = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>();
        for (ActionsList actionDesc : actionList) {

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action = actionDesc
                    .getAction();

            if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class))
                bucketActions.add(ofToSALOutputAction(action));
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class))
                bucketActions.add(ofToSALGroupAction(action));
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class)){
                CopyTtlOutBuilder copyTtlOutaction = new CopyTtlOutBuilder();
                bucketActions.add(new CopyTtlOutCaseBuilder().setCopyTtlOut(copyTtlOutaction.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class)){
                CopyTtlInBuilder copyTtlInaction = new CopyTtlInBuilder();
                bucketActions.add(new CopyTtlInCaseBuilder().setCopyTtlIn(copyTtlInaction.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class))
                bucketActions.add(ofToSALSetMplsTtl(action));
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class)){
                DecMplsTtlBuilder decMplsTtl = new DecMplsTtlBuilder();
                bucketActions.add(new DecMplsTtlCaseBuilder().setDecMplsTtl(decMplsTtl.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class))
                bucketActions.add(ofToSALPushVlanAction(action));
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class)){
                PopVlanActionBuilder popVlan = new PopVlanActionBuilder();
                bucketActions.add(new PopVlanActionCaseBuilder().setPopVlanAction(popVlan.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class)){
                PushMplsActionBuilder pushMpls = new PushMplsActionBuilder();
                bucketActions.add(new PushMplsActionCaseBuilder().setPushMplsAction(pushMpls.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class)){
                PopMplsActionBuilder popMpls = new PopMplsActionBuilder();
                bucketActions.add(new PopMplsActionCaseBuilder().setPopMplsAction(popMpls.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class))
                bucketActions.add(ofToSALSetQueue(action));

            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class))
                bucketActions.add(ofToSALSetNwTtl(action));
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class)){
                DecNwTtlBuilder decNwTtl = new DecNwTtlBuilder();
                bucketActions.add(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class))
                bucketActions.add(new SetFieldCaseBuilder().setSetField(MatchConvertor.ofToSALSetField(action)).build());

            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class))
                bucketActions.add(ofToSALPushPbbAction(action));
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class)){
                PopPbbActionBuilder popPbb = new PopPbbActionBuilder();
                bucketActions.add(new PopPbbActionCaseBuilder().setPopPbbAction(popPbb.build()).build());
            }
            else if (action.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter.class)){
                //bucketActions.add(ofToSALExperimenter(action));
                // TODO: Need to explore/discuss on how to handle experimenter case.
            }

        }
        return bucketActions;
    }

    /**
     * Method converts OF Output action object to SAL Output action object.
     *
     * @param action
     *            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.
     *            action.rev130731.actions.actions.list.Action
     * @return OutputAction
     */
    public static OutputActionCase ofToSALOutputAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        OutputActionBuilder outputAction = new OutputActionBuilder();
        PortAction port = action.getAugmentation(PortAction.class);
        if (port != null) {
            outputAction.setOutputNodeConnector(new Uri(port.getPort().getValue().toString()));
        } else {
            logger.error("Provided action is not OF Output action, no associated port found!");
        }

        MaxLengthAction length = action.getAugmentation(MaxLengthAction.class);
        if (length != null) {
            outputAction.setMaxLength(length.getMaxLength());
        } else {
            logger.error("Provided action is not OF Output action, no associated length found!");
        }

        return new OutputActionCaseBuilder().setOutputAction(outputAction.build()).build();
    }

    /**
     * Method converts OF GroupAction object to SAL GroupAction object
     *
     * @param action
     * @return GroupAction
     */
    public static GroupActionCase ofToSALGroupAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        GroupActionBuilder groupAction = new GroupActionBuilder();

        GroupIdAction groupId = action.getAugmentation(GroupIdAction.class);
        groupAction.setGroupId(groupId.getGroupId());

        return new GroupActionCaseBuilder().setGroupAction(groupAction.build()).build();
    }

    /**
     * Method converts OF SetMplsTTL action object to SAL SetMplsTTL action
     * object.
     *
     * @param action
     * @return
     */
    public static SetMplsTtlActionCase ofToSALSetMplsTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        SetMplsTtlActionBuilder mplsTtlAction = new SetMplsTtlActionBuilder();
        MplsTtlAction mplsTtl = action.getAugmentation(MplsTtlAction.class);
        mplsTtlAction.setMplsTtl(mplsTtl.getMplsTtl());
        return new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(mplsTtlAction.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action
     * @return PushVlanAction
     */
    public static PushVlanActionCase ofToSALPushVlanAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        PushVlanActionBuilder pushVlanAction = new PushVlanActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushVlanAction.setVlanId(new VlanId(etherType.getEthertype().getValue()));

        return new PushVlanActionCaseBuilder().setPushVlanAction(pushVlanAction.build()).build();
    }

    /**
     * Method converts OF SetQueue action to SAL SetQueue action.
     *
     * @param action
     * @return SetQueueAction
     */
    public static SetQueueActionCase ofToSALSetQueue(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        SetQueueActionBuilder setQueueAction = new SetQueueActionBuilder();

        QueueIdAction queueId = action.getAugmentation(QueueIdAction.class);
        setQueueAction.setQueueId(queueId.getQueueId());

        return new SetQueueActionCaseBuilder().setSetQueueAction(setQueueAction.build()).build();
    }

    /**
     * Method converts OF SetNwTtl action to SAL SetNwTtl action.
     *
     * @param action
     * @return SetNwTtlAction
     */
    public static SetNwTtlActionCase ofToSALSetNwTtl(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        SetNwTtlActionBuilder setNwTtl = new SetNwTtlActionBuilder();
        NwTtlAction nwTtl = action.getAugmentation(NwTtlAction.class);
        setNwTtl.setNwTtl(nwTtl.getNwTtl());

        return new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtl.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action
     * @return PushVlanAction
     */
    public static PushPbbActionCase ofToSALPushPbbAction(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        PushPbbActionBuilder pushPbbAction = new PushPbbActionBuilder();

        EthertypeAction etherType = action.getAugmentation(EthertypeAction.class);
        pushPbbAction.setEthernetType(etherType.getEthertype().getValue());

        return new PushPbbActionCaseBuilder().setPushPbbAction(pushPbbAction.build()).build();
    }

    public static Object ofToSALExperimenter(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {

        ExperimenterAction ExperimenterAction = action.getAugmentation(ExperimenterAction.class);

        return null;
        /*
         * TODO: Need to explore/discuss about how to handle experimenter
         */

    }
}
