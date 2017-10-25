/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;

public class OpenflowpluginGroupTestCommandProvider implements CommandProvider {

    private DataBroker dataBroker;
    private ProviderContext pc;
    private final BundleContext ctx;
    private Group testGroup;
    private Group testGroup2;
    private Node testNode;
    private final String originalGroupName = "Foo";
    private final String updatedGroupName = "Bar";

    public OpenflowpluginGroupTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBroker = session.getSALService(DataBroker.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestNode();
    }

    private void createUserNode(String nodeRef) {
        NodeRef nodeOne = createNodeRef(nodeRef);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private void createTestNode() {
        NodeRef nodeOne = createNodeRef(OpenflowpluginTestActivator.NODE_ID);
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.getKey());
    }

    private GroupBuilder createTestGroup(String actiontype, String type, String mod) {
        // Sample data , committing to DataStore

        String GroupType = type;
        String ActionType = actiontype;
        String Groupmod = mod;

        long id = 1;
        GroupKey key = new GroupKey(new GroupId(id));
        GroupBuilder group = new GroupBuilder();
        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 12));
        bucket.setKey(new BucketKey(new BucketId((long) 12)));

        if (GroupType == null) {
            GroupType = "g1";
        }
        if (ActionType == null) {
            ActionType = "a1";
        }

        switch (GroupType) {
            case "g1":
                group.setGroupType(GroupTypes.GroupSelect);
                break;
            case "g2":
                group.setGroupType(GroupTypes.GroupAll);
                break;
            case "g3":
                group.setGroupType(GroupTypes.GroupIndirect);
                break;
            case "g4":
                group.setGroupType(GroupTypes.GroupFf);
                break;
        }

        switch (ActionType) {
            case "a1":
                bucket.setAction(createPopVlanAction());
                break;
            case "a2":
                bucket.setAction(createPushVlanAction());
                break;
            case "a3":
                bucket.setAction(createPushMplsAction());
                break;
            case "a4":
                bucket.setAction(createPopMplsAction());
                break;
            case "a5":
                bucket.setAction(createPopPbbAction());
                break;
            case "a6":
                bucket.setAction(createPushPbbAction());
                break;
            case "a7":
                bucket.setAction(createPushPbbAction());
                break;
            case "a8":
                bucket.setAction(createCopyTtlInAction());
                break;
            case "a9":
                bucket.setAction(createCopyTtlOutAction());
                break;
            case "a10":
                bucket.setAction(createDecMplsTtlAction());
                break;
            case "a11":
                bucket.setAction(createDecNwTtlAction());
                break;
            case "a12":
                bucket.setAction(createSetQueueAction());
                break;
            case "a13":
                bucket.setAction(createSetNwTtlAction());
                break;
            case "a14":
                bucket.setAction(createGroupAction());
                break;
            case "a15":
                bucket.setAction(createSetMplsTtlAction());
                break;
            case "a16":
                bucket.setAction(createFloodOutputAction());
                break;
            case "a17":
                bucket.setAction(createAllOutputAction());
                break;
            case "a18":
                bucket.setAction(createNormalOutputAction());
                break;
            case "a19":
                bucket.setAction(creatTableOutputAction());
                break;
            case "a20":
                bucket.setAction(createControllerAction());
                break;
            case "a21":
                bucket.setAction(createLocalOutputAction());
                break;
            case "a22":
                bucket.setAction(createAnyOutputAction());
                break;
            case "a23":
                bucket.setAction(createInportOutputAction());
                break;
            case "a24":
                bucket.setAction(null);
                break;
            case "a25":
                bucket.setAction(createNonAppyOutputAction());
                break;
            case "a26":
                bucket.setAction(createNonAppyPushMplsAction());
                break;
            case "a27":
                bucket.setAction(createNonAppyPushPbbAction());
                break;
            case "a28":
                bucket.setAction(createNonAppyPushVlanAction());
                break;

        }

        if (Groupmod == "add") {
            bucket.setWatchGroup((long) 14);
            bucket.setWatchPort((long) 1234);
            bucket.setWeight(50);
        } else {
            bucket.setWatchGroup((long) 13);
            bucket.setWatchPort((long) 134);
            bucket.setWeight(30);
        }
        group.setKey(key);
        // group.setInstall(false);
        group.setGroupId(new GroupId(id));
        group.setGroupName(originalGroupName);
        group.setBarrier(false);
        BucketsBuilder value = new BucketsBuilder();
        List<Bucket> value1 = new ArrayList<Bucket>();
        value1.add(bucket.build());
        value.setBucket(value1);
        group.setBuckets(value.build());
        testGroup = group.build();
        return group;
    }


    private List<Action> createPopVlanAction() {
        PopVlanActionBuilder vlanAction = new PopVlanActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(vlanAction.build()).build());
        action.setKey(new ActionKey(0));
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPushVlanAction() {
        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(0x8100);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPushMplsAction() {
        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(0x8847);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPopMplsAction() {
        PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(0XB);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPopPbbAction() {
        PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPbbActionBuilder.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createPushPbbAction() {
        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(0x88E7);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createSetMplsTtlAction() {
        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMplsTtlActionBuilder.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createSetNwTtlAction() {
        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();
        setNwTtlActionBuilder.setNwTtl((short) 1);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createSetQueueAction() {
        SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
        setQueueActionBuilder.setQueueId(1L);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQueueActionBuilder.build()).build());
        action.setKey(new ActionKey(0));
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }


    private List<Action> createCopyTtlInAction() {
        CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(ttlin.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createCopyTtlOutAction() {
        CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(ttlout.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createDecMplsTtlAction() {
        DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(mpls.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private List<Action> createDecNwTtlAction() {
        DecNwTtlBuilder nwttl = new DecNwTtlBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(nwttl.build()).build());
        List<Action> actions = new ArrayList<Action>();

        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        //  output.setMaxLength(30);
        Uri value = new Uri("2");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        ab.setOrder(0);
        actions.add(ab.build());
        actions.add(action.build());
        return actions;
    }

    private List<Action> createFloodOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.FLOOD.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createAllOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.ALL.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createAnyOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.ANY.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createNormalOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.NORMAL.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createInportOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.INPORT.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> creatTableOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.TABLE.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createControllerAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createLocalOutputAction() {

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri(OutputPortValues.LOCAL.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());
        return actions;
    }

    private List<Action> createGroupAction() {

        GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(1L);
        groupActionB.setGroup("0");
        ActionBuilder action = new ActionBuilder();
        action.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
        action.setKey(new ActionKey(0));
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createNonAppyOutputAction() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        //output.setMaxLength(null);
        output.setMaxLength(66000);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        actionList.add(ab.build());
        return actionList;
    }

    private static List<Action> createNonAppyPushMplsAction() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(0x8849);
        ab.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        actionList.add(ab.build());
        return actionList;

    }

    private static List<Action> createNonAppyPushPbbAction() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(0x88E8);
        ab.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        actionList.add(ab.build());
        return actionList;

    }


    private static List<Action> createNonAppyPushVlanAction() {

        List<Action> actionList = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(0x8101);
        ab.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        actionList.add(ab.build());
        return actionList;

    }


    private GroupBuilder createTestRemoveGroup() {
        long id = 123;
        GroupKey key = new GroupKey(new GroupId(id));
        GroupBuilder group = new GroupBuilder();
     /*   BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 12));
        bucket.setKey(new BucketKey(new BucketId((long) 12))); */
        group.setKey(key);
        //    group.setInstall(false);

        group.setGroupId(new GroupId(id));
      /*  PopVlanActionBuilder vlanAction = new PopVlanActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(vlanAction.build()).build());
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build()); */
     /*   bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15); */
        //   group.setGroupType(GroupTypes.GroupSelect);
        //   group.setGroupName(originalGroupName);
        //   group.setBarrier(false);
        //    BucketsBuilder value = new BucketsBuilder();
        //    List<Bucket> value1 = new ArrayList<Bucket>();
        //    value1.add(bucket.build());
        //   value.setBucket(value1);
        //  group.setBuckets(value.build());
        testGroup2 = group.build();
        return group;
    }


    public void _removeGroup(final CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        GroupBuilder gbuilder = createTestGroup(ci.nextArgument(), ci.nextArgument(), "add");
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(gbuilder.getGroupId()));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    public void _addGroup(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        createTestGroup(ci.nextArgument(), ci.nextArgument(), "add");
        writeGroup(ci, testGroup);
    }

    private void writeGroup(final CommandInterpreter ci, Group group) {
        ReadWriteTransaction modification = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId()));
        modification.merge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode), testNode, true);
        modification.merge(LogicalDatastoreType.CONFIGURATION, path1, group, true);
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        Futures.addCallback(commitFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        });
    }

    public void _modifyGroup(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        GroupBuilder group = createTestGroup(ci.nextArgument(), ci.nextArgument(), "modify");
        writeGroup(ci, group.build());

        //     group.setGroupName(originalGroupName);
        //   writeGroup(ci, group.build());
    }

    @Override
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("---FRM MD-SAL Group test module---\n");
        help.append("\t addGroup <node id>        - node ref\n");
        help.append("\t modifyGroup <node id>        - node ref\n");
        help.append("\t removeGroup <node id>        - node ref\n");

        return help.toString();
    }

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path =
                InstanceIdentifier.create(Nodes.class).child(Node.class, key);

        return new NodeRef(path);
    }
}
