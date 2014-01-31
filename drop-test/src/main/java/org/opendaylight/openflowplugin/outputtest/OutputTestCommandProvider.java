/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.outputtest;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputTestCommandProvider implements CommandProvider {

    private PacketProcessingService packetProcessingService;
    private ProviderContext pc;
    private final BundleContext ctx;
    private boolean sessionInitiated = false;
    private static Logger LOG = LoggerFactory.getLogger(OutputTestCommandProvider.class);

    public OutputTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        packetProcessingService = session.getRpcService(PacketProcessingService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        this.sessionInitiated = true;
    }

    public void _sendOutputMsg(CommandInterpreter ci) {
        /* Sending package OUT */
        LOG.debug("SendOutMsg");
        if (sessionInitiated) {
            String inNodeKey = ci.nextArgument();

            // String resultOfPingFlow =
            // OutputTestUtil.makePingFlowForNode(inNodeKey, pc);
            // ci.println(resultOfPingFlow);

            TransmitPacketInput input = OutputTestUtil.buildTransmitInputPacket(inNodeKey, new String(
                    "sendOutputMsg_TEST").getBytes(), "0xfffffffd", // port
                    "0");

            packetProcessingService.transmitPacket(input);
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void _sendPacketOutputMsg(CommandInterpreter ci) {
        /* Sending package OUT with action */
        LOG.debug("SendOutMsgWithAction");
        if (sessionInitiated) {
            String inNodeKey = ci.nextArgument();
            String inPort = ci.nextArgument();
            String outPort = "0xfffffffd";

            ArrayList<Byte> _arrayList = new ArrayList<Byte>(40);
            ArrayList<Byte> list = _arrayList;
            String _string = new String("sendOutputMsg_TEST");
            byte[] msg = _string.getBytes();
            int index = 0;
            for (final byte b : msg) {
                {
                    list.add(Byte.valueOf(b));
                    boolean _lessThan = (index < 7);
                    if (_lessThan) {
                        int _plus = (index + 1);
                        index = _plus;
                    } else {
                        index = 0;
                    }
                }
            }
            boolean _lessThan = (index < 8);
            boolean _while = _lessThan;
            while (_while) {
                {
                    Byte _byte = new Byte("0");
                    list.add(_byte);
                    int _plus = (index + 1);
                    index = _plus;
                }
                boolean _lessThan_1 = (index < 8);
                _while = _lessThan_1;
            }
            NodeRef ref = OutputTestUtil.createNodeRef(inNodeKey);

            TransmitPacketInputBuilder packet_out = new TransmitPacketInputBuilder();

            NodeConnectorRef _createNodeConnRef_1 = OutputTestUtil.createNodeConnRef(inNodeKey, inPort);
            NodeConnectorRef _nodeConnectorRef_1 = new NodeConnectorRef(_createNodeConnRef_1);
            NodeConnectorRef nIngressConRef = _nodeConnectorRef_1;

            NodeConnectorRef _createNodeConnRef_2 = OutputTestUtil.createNodeConnRef(inNodeKey, outPort);
            NodeConnectorRef _nodeConnectorRef_2 = new NodeConnectorRef(_createNodeConnRef_2);
            NodeConnectorRef nEngressConRef = _nodeConnectorRef_2;

            final ArrayList<Byte> _converted_list = list;
            byte[] _primitive = ArrayUtils.toPrimitive(((Byte[]) Conversions.unwrapArray(_converted_list, Byte.class)));

            List<Action> actionList = new ArrayList<Action>();
            ActionBuilder ab = new ActionBuilder();

            OutputActionBuilder output = new OutputActionBuilder();
            output.setMaxLength(new Integer(0xffff));
            Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
            output.setOutputNodeConnector(value);
            ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
            ab.setOrder(0);
            ab.setKey(new ActionKey(0));
            actionList.add(ab.build());

            packet_out.setCookie(null);
            packet_out.setAction(actionList);
            packet_out.setPayload(_primitive);
            packet_out.setNode(ref);
            packet_out.setIngress(nIngressConRef);
            packet_out.setEgress(nEngressConRef);
            packet_out.setBufferId(new Long(0xffffffffL));

            packetProcessingService.transmitPacket(packet_out.build());
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void _sendOutTopologyMsg(CommandInterpreter ci) {
        /* Sending package OUT */
        LOG.debug("SendOutTopologyMsg");

    }

    @Override
    public String getHelp() {
        StringBuilder strBuf = new StringBuilder("-------------- OUT Package ----------\n")
                .append(" sendOutputMsg command + nodeId as param sends empty package out \n ");
        return strBuf.toString();
    }
}
