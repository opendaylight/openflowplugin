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
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
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

    private static final String OUTPUT_MSG = "sendOutputMsg_TEST";
    private static final String OUT_PORT = "0xfffffffd";
    private PacketProcessingService packetProcessingService;
    private final BundleContext ctx;
    private boolean sessionInitiated = false;
    private static final Logger LOG = LoggerFactory.getLogger(OutputTestCommandProvider.class);

    public OutputTestCommandProvider(final BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(final ProviderContext session) {
        packetProcessingService = session.getRpcService(PacketProcessingService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        this.sessionInitiated = true;
    }

    public void sendOutputMsg(final CommandInterpreter ci) {
        /* Sending package OUT */
        LOG.debug("SendOutMsg");
        if (sessionInitiated) {
            String inNodeKey = ci.nextArgument();

            TransmitPacketInput input = OutputTestUtil.buildTransmitInputPacket(inNodeKey, OUT_PORT, "0");

            packetProcessingService.transmitPacket(input);
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void sendPacketOutputMsg(final CommandInterpreter ci) {
        /* Sending package OUT with action */
        LOG.debug("SendOutMsgWithAction");
        if (sessionInitiated) {
            String inNodeKey = ci.nextArgument();
            String inPort = ci.nextArgument();

            List<Byte> list = new ArrayList<Byte>(40);
            byte[] msg = OUTPUT_MSG.getBytes();
            int index = 0;
            for (final byte b : msg) {
                {
                    list.add(Byte.valueOf(b));
                    boolean lessThan = (index < 7);
                    if (lessThan) {
                        int indexIncrement = (index + 1);
                        index = indexIncrement;
                    } else {
                        index = 0;
                    }
                }
            }
            boolean lessThan = (index < 8);
            boolean condition = lessThan;
            while (condition) {
                {
                    list.add((byte)0);
                    int indexIncrement = (index + 1);
                    index = indexIncrement;
                }
                boolean lessThan1 = (index < 8);
                condition = lessThan1;
            }
            NodeRef ref = OutputTestUtil.createNodeRef(inNodeKey);

            TransmitPacketInputBuilder packetOut = new TransmitPacketInputBuilder();

            NodeConnectorRef nIngressConRef = OutputTestUtil.createNodeConnRef(inNodeKey, inPort);
            NodeConnectorRef nEngressConRef = OutputTestUtil.createNodeConnRef(inNodeKey, OUT_PORT);

            final List<Byte> convertedList = list;
            byte[] primitive = ArrayUtils.toPrimitive(convertedList.toArray(new Byte[0]));

            List<Action> actionList = new ArrayList<Action>();
            ActionBuilder ab = new ActionBuilder();

            OutputActionBuilder output = new OutputActionBuilder();
            output.setMaxLength(Integer.valueOf(0xffff));
            Uri value = new Uri(OutputPortValues.CONTROLLER.toString());
            output.setOutputNodeConnector(value);
            ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
            ab.setOrder(0);
            ab.setKey(new ActionKey(0));
            actionList.add(ab.build());

            packetOut.setConnectionCookie(null);
            packetOut.setAction(actionList);
            packetOut.setPayload(primitive);
            packetOut.setNode(ref);
            packetOut.setIngress(nIngressConRef);
            packetOut.setEgress(nEngressConRef);
            packetOut.setBufferId(OFConstants.OFP_NO_BUFFER);

            packetProcessingService.transmitPacket(packetOut.build());
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void sendOutTopologyMsg(final CommandInterpreter ci) {
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
