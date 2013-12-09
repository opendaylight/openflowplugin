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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.config.rev131024.Meters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.osgi.framework.BundleContext;

public class OpenflowpluginMeterTestCommandProvider implements CommandProvider {

    private DataBrokerService dataBrokerService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private Meter testMeter;
    private Node testNode;
    private final String originalMeterName = "Foo";
    private final String updatedMeterName = "Bar";

    public OpenflowpluginMeterTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        dataBrokerService = session.getSALService(DataBrokerService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        createTestNode();
        createTestMeter();
    }

    private void createUserNode(String nodeRef) {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(nodeRef));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private void createTestNode() {
        NodeBuilder builder = new NodeBuilder();
        builder.setId(new NodeId(OpenflowpluginTestActivator.NODE_ID));
        builder.setKey(new NodeKey(builder.getId()));
        testNode = builder.build();
    }

    private InstanceIdentifier<Node> nodeToInstanceId(Node node) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, node.getKey()).toInstance();
    }

    private MeterBuilder createTestMeter() {
        // Sample data , committing to DataStore
        DataModification modification = dataBrokerService.beginTransaction();

        long id = 12;
        MeterKey key = new MeterKey(new MeterId(id));
        MeterBuilder meter = new MeterBuilder();
        meter.setMeterId(new MeterId((long) 9));
        meter.setKey(key);
        meter.setMeterName(originalMeterName);
        meter.setFlags(new MeterFlags(true, false, false, false));
        MeterBandHeadersBuilder bandHeaders = new MeterBandHeadersBuilder();
        List<MeterBandHeader> bandHdr = new ArrayList<MeterBandHeader>();
        MeterBandHeaderBuilder bandHeader = new MeterBandHeaderBuilder();
        bandHeader.setRate((long) 234);
        bandHeader.setBurstSize((long) 444);
        DscpRemarkBuilder dscpRemark = new DscpRemarkBuilder();
        dscpRemark.setBurstSize((long) 5);
        dscpRemark.setPercLevel((short) 1);
        dscpRemark.setRate((long) 12);
        bandHeader.setBandType(dscpRemark.build());
        MeterBandTypesBuilder bandTypes = new MeterBandTypesBuilder();
        MeterBandType bandType = new MeterBandType(false, true, false);
        bandTypes.setFlags(bandType);
        bandHeader.setMeterBandTypes(bandTypes.build());
        bandHdr.add(bandHeader.build());
        bandHeaders.setMeterBandHeader(bandHdr);
        meter.setMeterBandHeaders(bandHeaders.build());

        testMeter = meter.build();
        return meter;
    }

    public void _removeMeter(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        MeterBuilder mBuilder = createTestMeter();
        DataModification modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(testMeter.getMeterId()))
                .build();
        modification.removeOperationalData(nodeToInstanceId(testNode));
        modification.removeOperationalData(path1);
        modification.removeConfigurationData(nodeToInstanceId(testNode));
        modification.removeConfigurationData(path1);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Meter Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _addMeter(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        createTestMeter();
        writeMeter(ci, testMeter);
    }

    private void writeMeter(CommandInterpreter ci, Meter meter) {
        DataModification modification = dataBrokerService.beginTransaction();
        InstanceIdentifier<Meter> path1 = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, testNode.getKey()).augmentation(FlowCapableNode.class)
                .child(Meter.class, new MeterKey(meter.getMeterId()))
                .build();
        DataObject cls = (DataObject) modification.readConfigurationData(path1);
        modification.putOperationalData(nodeToInstanceId(testNode), testNode);
        modification.putOperationalData(path1, meter);
        modification.putConfigurationData(nodeToInstanceId(testNode), testNode);
        modification.putConfigurationData(path1, meter);
        Future<RpcResult<TransactionStatus>> commitFuture = modification.commit();
        try {
            RpcResult<TransactionStatus> result = commitFuture.get();
            TransactionStatus status = result.getResult();
            ci.println("Status of Meter Data Loaded Transaction: " + status);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void _modifyMeter(CommandInterpreter ci) {
        String nref = ci.nextArgument();

        if (nref == null) {
            ci.println("test node added");
            createTestNode();
        } else {
            ci.println("User node added" + nref);
            createUserNode(nref);
        }
        MeterBuilder meter = createTestMeter();
        meter.setMeterName(updatedMeterName);
        writeMeter(ci, meter.build());
        meter.setMeterName(originalMeterName);
        writeMeter(ci, meter.build());
    }

    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---FRM MD-SAL Group test module---\n");
        help.append("\t addMeter <node id>        - node ref\n");
        help.append("\t modifyMeter <node id>        - node ref\n");
        help.append("\t removeMeter <node id>        - node ref\n");

        return help.toString();
    }

}
