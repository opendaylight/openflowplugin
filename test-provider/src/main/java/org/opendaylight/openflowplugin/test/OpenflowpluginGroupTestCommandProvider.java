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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.config.rev131024.Groups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.config.rev131024.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.config.rev131024.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.config.rev131024.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
        createTestGroup();
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

    private void createTestGroup() {
        // Sample data , committing to DataStore
        DataModification modification = dataBrokerService.beginTransaction();
        long id = 123;
        GroupKey key = new GroupKey(id, new NodeRef(new NodeRef(nodeToInstanceId(testNode))));
        GroupBuilder group = new GroupBuilder();
        BucketBuilder bucket = new BucketBuilder();
        bucket.setOrder(12);
        group.setKey(key);
        group.setInstall(false);
        group.setId(id);
        group.setGroupType(GroupType.GroupSelect);
        group.setGroupName(originalGroupName);
        group.setBarrier(false);
        BucketsBuilder value = new BucketsBuilder();
        List<Bucket> value1 = new ArrayList<Bucket>();
        value1.add(bucket.build());
        value.setBucket(value1);
        group.setBuckets(value.build());
        testGroup = group.build();
    }

    public void _removeGroup(CommandInterpreter ci) {
        DataModification modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Groups.class)
                .child(Group.class, testGroup.getKey()).toInstance();
        DataObject cls = (DataObject) modification.readConfigurationData(path1);
        modification.removeOperationalData(nodeToInstanceId(testNode));
        modification.removeOperationalData(path1);
        modification.removeConfigurationData(nodeToInstanceId(testNode));
        modification.removeConfigurationData(path1);
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

    public void _addGroup(CommandInterpreter ci) {
        writeGroup(ci, testGroup);
    }

    private void writeGroup(CommandInterpreter ci, Group group) {
        DataModification modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Group> path1 = InstanceIdentifier.builder(Groups.class).child(Group.class, group.getKey())
                .toInstance();
        DataObject cls = (DataObject) modification.readConfigurationData(path1);
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
        GroupBuilder group = new GroupBuilder(testGroup);
        group.setGroupName(updatedGroupName);
        writeGroup(ci, group.build());
        group.setGroupName(originalGroupName);
        writeGroup(ci, group.build());
    }

    private static NodeRef createNodeRef(String string) {
        NodeKey key = new NodeKey(new NodeId(string));
        InstanceIdentifier<Node> path = InstanceIdentifier.builder().node(Nodes.class).node(Node.class, key)
                .toInstance();

        return new NodeRef(path);
    }

    @Override
    public String getHelp() {
        return "No help";
    }
}
