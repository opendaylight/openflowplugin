/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.framework.BundleContext;

@SuppressWarnings("checkstyle:MethodName")
public class OpenflowpluginGroupTestCommandProvider implements CommandProvider {
    private static final String ORIGINAL_GROUP_NAME = "Foo";

    private final DataBroker dataBroker;
    private final BundleContext ctx;
    private Group testGroup;
    private Node testNode;

    public OpenflowpluginGroupTestCommandProvider(DataBroker dataBroker, BundleContext ctx) {
        this.dataBroker = dataBroker;
        this.ctx = ctx;
    }

    public void init() {
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestNode();
    }

    private void createUserNode(String nodeRef) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.withKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private void createTestNode() {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.withKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, node.key());
    }

    private GroupBuilder createTestGroup(String actionType, String groupType, String groupMod) {
        // Sample data , committing to DataStore

        GroupBuilder group = new GroupBuilder();
        BucketBuilder bucket = new BucketBuilder().withKey(new BucketKey(new BucketId(Uint32.valueOf(12))));

        if (groupType == null) {
            groupType = "g1";
        }
        if (actionType == null) {
            actionType = "a1";
        }

        switch (groupType) {
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
            default:
                break;
        }

        switch (actionType) {
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
                bucket.setAction(Collections.emptyList());
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
            default:
                break;
        }

        if ("add".equals(groupMod)) {
            bucket.setWatchGroup(Uint32.valueOf(14));
            bucket.setWatchPort(Uint32.valueOf(1234));
            bucket.setWeight(Uint16.valueOf(50));
        } else {
            bucket.setWatchGroup(Uint32.valueOf(13));
            bucket.setWatchPort(Uint32.valueOf(134));
            bucket.setWeight(Uint16.valueOf(30));
        }

        GroupKey key = new GroupKey(new GroupId(Uint32.ONE));
        group.withKey(key);
        // group.setInstall(false);
        group.setGroupName(ORIGINAL_GROUP_NAME);
        group.setBarrier(false);
        BucketsBuilder value = new BucketsBuilder();
        List<Bucket> value1 = new ArrayList<>();
        value1.add(bucket.build());
        value.setBucket(value1);
        group.setBuckets(value.build());
        testGroup = group.build();
        return group;
    }


    private static List<Action> createPopVlanAction() {
        PopVlanActionBuilder vlanAction = new PopVlanActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(vlanAction.build()).build());
        action.withKey(new ActionKey(0));
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createPushVlanAction() {
        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(Uint16.valueOf(0x8100));
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createPushMplsAction() {
        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(Uint16.valueOf(0x8847));
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createPopMplsAction() {
        PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(Uint16.valueOf(0XB));
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createPopPbbAction() {
        PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPbbActionBuilder.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createPushPbbAction() {
        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(Uint16.valueOf(0x88E7));
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createSetMplsTtlAction() {
        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl(Uint8.ONE);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(
                setMplsTtlActionBuilder.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createSetNwTtlAction() {
        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();
        setNwTtlActionBuilder.setNwTtl(Uint8.ONE);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createSetQueueAction() {
        SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
        setQueueActionBuilder.setQueueId(Uint32.ONE);
        ActionBuilder action = new ActionBuilder();
        action.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQueueActionBuilder.build()).build());
        action.withKey(new ActionKey(0));
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }


    private static List<Action> createCopyTtlInAction() {
        CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(ttlin.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createCopyTtlOutAction() {
        CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(ttlout.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createDecMplsTtlAction() {
        DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(mpls.build()).build());
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createDecNwTtlAction() {
        DecNwTtlBuilder nwttl = new DecNwTtlBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(nwttl.build()).build());

        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        //  output.setMaxLength(30);
        Uri value = new Uri("2");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));
        ab.setOrder(0);

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createFloodOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.FLOOD.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createAllOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.ALL.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createAnyOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.ANY.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createNormalOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.NORMAL.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createInportOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.INPORT.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> creatTableOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.TABLE.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createControllerAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createLocalOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(Uint16.valueOf(30));
        Uri value = new Uri(OutputPortValues.LOCAL.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.withKey(new ActionKey(0));

        List<Action> actions = new ArrayList<>();
        actions.add(ab.build());
        return actions;
    }

    private static List<Action> createGroupAction() {
        GroupActionBuilder groupActionB = new GroupActionBuilder();
        groupActionB.setGroupId(Uint32.ONE);
        groupActionB.setGroup("0");
        ActionBuilder action = new ActionBuilder();
        action.setAction(new GroupActionCaseBuilder().setGroupAction(groupActionB.build()).build());
        action.withKey(new ActionKey(0));
        List<Action> actions = new ArrayList<>();
        actions.add(action.build());
        return actions;
    }

    private static List<Action> createNonAppyOutputAction() {
        ActionBuilder ab = new ActionBuilder();
        OutputActionBuilder output = new OutputActionBuilder();
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        //output.setMaxLength(null);
        output.setMaxLength(Uint16.valueOf(66000));
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());

        List<Action> actionList = new ArrayList<>();
        actionList.add(ab.build());
        return actionList;
    }

    private static List<Action> createNonAppyPushMplsAction() {
        List<Action> actionList = new ArrayList<>();
        ActionBuilder ab = new ActionBuilder();
        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(Uint16.valueOf(0x8849));
        ab.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        actionList.add(ab.build());
        return actionList;
    }

    private static List<Action> createNonAppyPushPbbAction() {
        List<Action> actionList = new ArrayList<>();
        ActionBuilder ab = new ActionBuilder();
        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(Uint16.valueOf(0x88E8));
        ab.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        actionList.add(ab.build());
        return actionList;
    }

    private static List<Action> createNonAppyPushVlanAction() {
        List<Action> actionList = new ArrayList<>();
        ActionBuilder ab = new ActionBuilder();
        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(Uint16.valueOf(0x8101));
        ab.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        actionList.add(ab.build());
        return actionList;
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
        ReadWriteTransaction modification = Preconditions.checkNotNull(dataBroker).newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class).child(Node.class, testNode.key())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(gbuilder.getGroupId()));
        modification.delete(LogicalDatastoreType.CONFIGURATION, path1);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
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
        ReadWriteTransaction modification = Preconditions.checkNotNull(dataBroker).newReadWriteTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, testNode.key()).augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId()));
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, nodeToInstanceId(testNode),
                testNode);
        modification.mergeParentStructureMerge(LogicalDatastoreType.CONFIGURATION, path1, group);
        modification.commit().addCallback(new FutureCallback<CommitInfo>() {
            @Override
            public void onSuccess(CommitInfo notUsed) {
                ci.println("Status of Group Data Loaded Transaction: success.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                ci.println(String.format("Status of Group Data Loaded Transaction : failure. Reason : %s", throwable));
            }
        }, MoreExecutors.directExecutor());
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
}
