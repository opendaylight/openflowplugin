package org.opendaylight.openflowplugin.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.md.sal.common.api.TransactionStatus;
import org.opendaylight.controller.md.sal.common.api.data.DataModification;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;

public class OpenflowpluginGroupTestCommandProvider implements CommandProvider {

    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private Group testGroup;
    private Node testNode;
    private final String originalGroupName = "Foo";
    private final String updatedGroupName = "Bar";

    public OpenflowpluginGroupTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
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
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    private GroupBuilder createTestGroup() {
        // Sample data , committing to DataStore
        DataModification modification = dataBrokerService.beginTransaction();

        long id = 123;
        GroupKey key = new GroupKey(new GroupId(id));
        GroupBuilder group = new GroupBuilder();
        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 12));
        group.setKey(key);
        group.setInstall(false);
        group.setGroupId(new GroupId(id));
        PopVlanActionBuilder vlanAction = new PopVlanActionBuilder();
        ActionBuilder action = new ActionBuilder();
        action.setAction(new PopVlanActionCaseBuilder().setPopVlanAction(vlanAction.build()).build());
        action.setOrder(0);
        List<Action> actions = new ArrayList<Action>();
        actions.add(action.build());
        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);
        group.setGroupType(GroupTypes.GroupSelect);
        group.setGroupName(originalGroupName);
        group.setBarrier(false);
        BucketsBuilder value = new BucketsBuilder();
        List<Bucket> value1 = new ArrayList<Bucket>();

        value1.add(bucket.build());
        value1.add(Action1().build()); // setField-ipv4
        value1.add(Action2().build()); // push Pbb
        value1.add(Action3().build()); // pop Pbb
        value1.add(Action4().build()); // push mpls
        value1.add(Action5().build()); // pop mpls
        value1.add(Action6().build()); // push vlan
        value1.add(Action7().build()); // Output-ALL
        value1.add(Action8().build()); // Dec_MPLS
        value1.add(Action9().build()); // Set_MPLS
        value1.add(Action10().build()); // setNwTTL
        value1.add(Action11().build()); // decNwTTL
        value1.add(Action12().build()); // Output-Inport
        value1.add(Action13().build()); // Output-Flood
        value1.add(Action14().build()); // Output-Normal
        value1.add(Action15().build()); // Output-Local
        value1.add(Action16().build()); // Output-Table
        value1.add(Action17().build()); // Copy-ttl-in
        value1.add(Action18().build()); // copy-ttl-out
        value1.add(Action19().build()); // set-queue
        value1.add(Action20().build()); // send to controller

        value.setBucket(value1);
        group.setBuckets(value.build());
        testGroup = group.build();
        return group;
    }

    private BucketBuilder Action1() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 13));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();
        ActionBuilder ab1 = new ActionBuilder();

        SetFieldBuilder setFieldBuilder = new SetFieldBuilder();
        SetFieldBuilder setFieldBuilder1 = new SetFieldBuilder();
        // IPv4
        Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
        Ipv4MatchBuilder ipv4Match1 = new Ipv4MatchBuilder();
        Ipv4Prefix dstip = new Ipv4Prefix("200.71.9.5210");
        Ipv4Prefix srcip = new Ipv4Prefix("100.1.1.1");
        ipv4Match1.setIpv4Destination(dstip);
        ipv4Match.setIpv4Source(srcip);
        setFieldBuilder.setLayer3Match(ipv4Match.build());
        ab.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        setFieldBuilder1.setLayer3Match(ipv4Match1.build());
        ab1.setAction(new SetFieldCaseBuilder().setSetField(setFieldBuilder1.build()).build());
        ab1.setKey(new ActionKey(1));
        actions.add(ab1.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action2() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 14));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PushPbbActionBuilder pbb = new PushPbbActionBuilder();
        pbb.setEthernetType(new Integer(0x88E7));
        ab.setAction(new PushPbbActionCaseBuilder().setPushPbbAction(pbb.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action3() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 15));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PopPbbActionBuilder popPbbActionBuilder = new PopPbbActionBuilder();
        ab.setAction(new PopPbbActionCaseBuilder().setPopPbbAction(popPbbActionBuilder.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action4() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 16));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PushMplsActionBuilder push = new PushMplsActionBuilder();
        push.setEthernetType(new Integer(0x8847));
        ab.setAction(new PushMplsActionCaseBuilder().setPushMplsAction(push.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action5() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 17));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PopMplsActionBuilder popMplsActionBuilder = new PopMplsActionBuilder();
        popMplsActionBuilder.setEthernetType(0XB);
        ab.setAction(new PopMplsActionCaseBuilder().setPopMplsAction(popMplsActionBuilder.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action6() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 18));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        PushVlanActionBuilder vlan = new PushVlanActionBuilder();
        vlan.setEthernetType(new Integer(0x8100));
        ab.setAction(new PushVlanActionCaseBuilder().setPushVlanAction(vlan.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action7() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 19));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri("ALL");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action8() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 20));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        DecMplsTtlBuilder mpls = new DecMplsTtlBuilder();
        ab.setAction(new DecMplsTtlCaseBuilder().setDecMplsTtl(mpls.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action9() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 21));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 0X1);
        ab.setAction(new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(setMplsTtlActionBuilder.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action10() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 22));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetNwTtlActionBuilder setNwTtlActionBuilder = new SetNwTtlActionBuilder();
        setNwTtlActionBuilder.setNwTtl((short) 1);
        ab.setAction(new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtlActionBuilder.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action11() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 23));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        DecNwTtlBuilder ta = new DecNwTtlBuilder();
        DecNwTtl decNwTtl = ta.build();
        ab.setAction(new DecNwTtlCaseBuilder().setDecNwTtl(decNwTtl).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action12() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 24));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri("INPORT");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action13() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 25));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri("FLOOD");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action14() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 26));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri("NORMAL");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action15() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 27));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri("LOCAL");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action16() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 28));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(30);
        Uri value = new Uri("TABLE");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action17() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 29));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        CopyTtlInBuilder ttlin = new CopyTtlInBuilder();
        ab.setAction(new CopyTtlInCaseBuilder().setCopyTtlIn(ttlin.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action18() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 30));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        CopyTtlOutBuilder ttlout = new CopyTtlOutBuilder();
        ab.setAction(new CopyTtlOutCaseBuilder().setCopyTtlOut(ttlout.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action19() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 31));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
        setQueueActionBuilder.setQueueId(1L);
        ab.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQueueActionBuilder.build()).build());
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    private BucketBuilder Action20() {

        BucketBuilder bucket = new BucketBuilder();
        bucket.setBucketId(new BucketId((long) 31));

        List<Action> actions = new ArrayList<Action>();
        ActionBuilder ab = new ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(56);
        Uri value = new Uri("CONTROLLER");
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actions.add(ab.build());

        bucket.setAction(actions);
        bucket.setWatchGroup((long) 14);
        bucket.setWatchPort((long) 1234);
        bucket.setWeight(15);

        return bucket;
    }

    public void _removeGroup(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        GroupBuilder gbuilder = createTestGroup();
        DataModification<InstanceIdentifier<?>, DataObject> modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(gbuilder.getGroupId())).build();
        // DataObject cls = (DataObject)
        // modification.readConfigurationData(path1);
        // modification.removeOperationalData(nodeToInstanceId(testNode));
        modification.removeOperationalData(path1);
        // modification.removeConfigurationData(nodeToInstanceId(testNode));
        modification.removeConfigurationData(path1);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Group Data Loaded Transaction: " + status);
            ci.println("Status of Group Data Loaded Transaction: ");

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        createTestGroup();
        writeGroup(ci, testGroup);
    }

    private void writeGroup(CommandInterpreter ci, Group group) {
        DataModification modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Nodes.class).child(Node.class, testNode.getKey())
                .augmentation(FlowCapableNode.class).child(Group.class, new GroupKey(group.getGroupId())).build();
        modification.putOperationalData(nodeToInstanceId(testNode), testNode);
        modification.putOperationalData(path1, group);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, group);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Group Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        GroupBuilder group = createTestGroup();
        group.setGroupName(updatedGroupName);
        writeGroup(ci, group.build());
        group.setGroupName(originalGroupName);
        writeGroup(ci, group.build());
    }

    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---FRM MD-SAL Group test module---\n");
        help.append("\t addGroup <node id>        - node ref\n");
        help.append("\t modifyGroup <node id>        - node ref\n");
        help.append("\t removeGroup <node id>        - node ref\n");

        return help.toString();
    }

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class).child(Node.class, key).toInstance();

        return new NodeRef(path);
    }

    private static void removeMeImFaick() {

    }
}
