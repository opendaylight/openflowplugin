/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.PacketOutConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.ConnectionCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by Jakub Toth jatoth@cisco.com on 9/23/14.
 */

public class PacketOutConvertorTest extends ConvertorManagerInitialization{

    @Override
    public void setUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test for {@link PacketOutConvertor} with null parameters
     */
    @Test
    public void toPacketOutInputAllParmNullTest() {

        TransmitPacketInputBuilder transmitPacketInputBuilder = new TransmitPacketInputBuilder();

        Long bufferId = null;

        String NODE_ID = "0";
        String port = "0";

        NodeRef ref = createNodeRef(NODE_ID);
        NodeConnectorKey nodeConnKey = PacketOutConvertorTest.createNodeConnKey(NODE_ID, port);
        NodeConnectorRef nEgressConfRef = new NodeConnectorRef(
                createNodeConnRef(NODE_ID, nodeConnKey));

        transmitPacketInputBuilder.setBufferId(bufferId);
        transmitPacketInputBuilder.setConnectionCookie(null);
        transmitPacketInputBuilder.setAction(null);
        transmitPacketInputBuilder.setNode(ref);
        transmitPacketInputBuilder.setPayload(null);
        transmitPacketInputBuilder.setEgress(nEgressConfRef);
        transmitPacketInputBuilder.setIngress(null);
        TransmitPacketInput transmitPacketInput = transmitPacketInputBuilder
                .build();

        Short version = (short) 0x04;
        Long xid = null;
        PacketOutConvertorData data = new PacketOutConvertorData(version);
        PacketOutInput message = convert(transmitPacketInput, data);

        //FIXME : this has to be fixed along with actions changed in openflowjava

        Assert.assertEquals(buildActionForNullTransmitPacketInputAction(nodeConnKey, version), message.getAction());

        Assert.assertEquals(OFConstants.OFP_NO_BUFFER, message.getBufferId());
        Assert.assertEquals(new PortNumber(0xfffffffdL), message.getInPort());
        Assert.assertEquals(version, message.getVersion());
        Assert.assertEquals(xid, message.getXid());
        Assert.assertArrayEquals(transmitPacketInput.getPayload(), message.getData());
    }

    /**
     * Test for PacketOutConvertor
     */
    @Test
    public void toPacketOutInputAllParmTest() {
        TransmitPacketInputBuilder transmitPacketInputBuilder = new TransmitPacketInputBuilder();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder ab = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder();

        OutputActionBuilder output = new OutputActionBuilder();
        output.setMaxLength(OFConstants.OFPCML_NO_BUFFER);
        Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
        output.setOutputNodeConnector(value);
        ab.setAction(new OutputActionCaseBuilder().setOutputAction(
                output.build()).build());
        ab.setOrder(0);
        ab.setKey(new ActionKey(0));
        actionList.add(ab.build());

        Long bufferId = 0xfL;

        Long valueForCookie = 0xeL;
        ConnectionCookie connCook = new ConnectionCookie(valueForCookie);

        String NODE_ID = "node:1";

        NodeRef ref = createNodeRef(NODE_ID);

        String portO = "0xfffffffd";
        NodeConnectorKey nEgrConKey = PacketOutConvertorTest.createNodeConnKey(NODE_ID, portO);
        NodeConnectorRef nEgressConfRef = new NodeConnectorRef(
                createNodeConnRef(NODE_ID, nEgrConKey));

        String inPort = "2";
        NodeConnectorKey nIngrConKey = PacketOutConvertorTest.createNodeConnKey(NODE_ID, inPort);
        NodeConnectorRef nIngressConRef = new NodeConnectorRef(
                createNodeConnRef(NODE_ID, nIngrConKey));

        String _string = new String("sendOutputMsg_TEST");
        byte[] msg = _string.getBytes();

        byte[] payload = msg;

        transmitPacketInputBuilder.setAction(actionList);
        transmitPacketInputBuilder.setBufferId(bufferId);
        transmitPacketInputBuilder.setConnectionCookie(connCook);
        transmitPacketInputBuilder.setEgress(nEgressConfRef);
        transmitPacketInputBuilder.setIngress(nIngressConRef);
        transmitPacketInputBuilder.setNode(ref);
        transmitPacketInputBuilder.setPayload(payload);

        TransmitPacketInput transmitPacketInput = transmitPacketInputBuilder
                .build();

        short version = (short) 0x04;
        byte[] datapathIdByte = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        for (int i = 0; i < datapathIdByte.length; i++) {
            datapathIdByte[i] = 1;
        }
        BigInteger datapathId = new BigInteger(1, datapathIdByte);
        Long xid = 0xfffffL;

        OpenflowPortsUtil.init();

        PacketOutConvertorData data = new PacketOutConvertorData(version);
        data.setXid(xid);
        data.setDatapathId(datapathId);
        PacketOutInput message = convert(transmitPacketInput, data);

        Assert.assertEquals(transmitPacketInput.getBufferId(),
                message.getBufferId());
        Assert.assertEquals("PortNumber [_value=" + inPort + "]", message
                .getInPort().toString());
        Assert.assertEquals((Object) version,
                Short.valueOf(message.getVersion()));
        Assert.assertEquals(xid, message.getXid());

        ActionConvertorData actionConvertorData = new ActionConvertorData(version);
        actionConvertorData.setDatapathId(datapathId);

        Optional<List<Action>> actionsOptional = getConvertorManager().convert(
                actionList, actionConvertorData);

        List<Action> actions = actionsOptional.orElse(Collections.emptyList());
        Assert.assertEquals(actions, message.getAction());
        Assert.assertArrayEquals(transmitPacketInput.getPayload(), message.getData());
    }

    /**
     * create action
     *
     * @param nConKey
     * @param version
     * @return
     */
    private static List<Action> buildActionForNullTransmitPacketInputAction(
            final NodeConnectorKey nConKey, final short version) {

        PortNumber outPort = getPortNumber(nConKey, version);
        List<Action> actions = new ArrayList<>();
        ActionBuilder aBuild = new ActionBuilder();

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder outputActionCaseBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder();

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder outputActionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder();

        outputActionBuilder.setPort(outPort);
        outputActionBuilder.setMaxLength(0xffff);
        outputActionCaseBuilder.setOutputAction(outputActionBuilder.build());
        aBuild.setActionChoice(outputActionCaseBuilder.build());
        actions.add(aBuild.build());
        return actions;
    }

    /**
     * create PortNumber
     *
     * @param nConKey
     * @param ofVersion
     * @return
     */
    private static PortNumber getPortNumber(final NodeConnectorKey nConKey,
                                            final Short ofVersion) {
        Long port = InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                OpenflowVersion.get(ofVersion), nConKey.getId());
        return new PortNumber(port);
    }

    /**
     * create NodeConnectorRef
     *
     * @param nodeId
     * @param nConKey
     * @return
     */
    private static NodeConnectorRef createNodeConnRef(final String nodeId,
                                                      final NodeConnectorKey nConKey) {

        InstanceIdentifier<NodeConnector> path = InstanceIdentifier
                .<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class,
                        new NodeKey(new NodeId(nodeId)))
                .<NodeConnector, NodeConnectorKey>child(NodeConnector.class,
                        nConKey).build();

        return new NodeConnectorRef(path);
    }

    /**
     * create NodeConnectorKey
     *
     * @param nodeId
     * @param port
     * @return
     */
    private static NodeConnectorKey createNodeConnKey(final String nodeId,
                                                      final String port) {
        StringBuilder sBuild = new StringBuilder(nodeId).append(':').append(
                port);

        return new NodeConnectorKey(new NodeConnectorId(sBuild.toString()));
    }

    /**
     * create NodeRef
     *
     * @param nodeId
     * @return
     */
    private static NodeRef createNodeRef(final String nodeId) {
        NodeKey key = new NodeKey(new NodeId(nodeId));
        InstanceIdentifier<Node> path = InstanceIdentifier
                .<Nodes>builder(Nodes.class)
                .<Node, NodeKey>child(Node.class, key).build();
        return new NodeRef(path);
    }

    private PacketOutInput convert(TransmitPacketInput transmitPacketInput, PacketOutConvertorData data) {
        Optional<PacketOutInput> messageOptional = getConvertorManager().convert(transmitPacketInput, data);
        return messageOptional.orElse(PacketOutConvertor.defaultResult(data.getVersion()));
    }
}
