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
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputTestCommandProvider implements CommandProvider {

    private PacketProcessingService packetProcessingService;
    private ProviderContext pc;
    private BundleContext ctx;
    private boolean sessionInitiated = false;
    private static Logger LOG = LoggerFactory.getLogger(OutputTestCommandProvider.class);

    public OutputTestCommandProvider(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        packetProcessingService = session
                .getRpcService(PacketProcessingService.class);
        ctx.registerService(CommandProvider.class.getName(), this, null);
        this.sessionInitiated = true;
    }

    public void _sendOutputMsg(CommandInterpreter ci) {
        /* Sending package OUT */
        LOG.info("SendOutMsg");
        if (sessionInitiated) {
            String inNodeKey = ci.nextArgument();
            
//          String resultOfPingFlow = OutputTestUtil.makePingFlowForNode(inNodeKey, pc);
//          ci.println(resultOfPingFlow);
            
            TransmitPacketInput input = OutputTestUtil.buildTransmitInputPacket(
                                inNodeKey, 
                                new String("sendOutputMsg_TEST").getBytes(),
                                "0xfffffffd", // port
                                "0");

            packetProcessingService.transmitPacket(input);
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void _sendOutTopologyMsg(CommandInterpreter ci) {
        /* Sending package OUT */
        LOG.info("SendOutTopologyMsg");
        
        
    }
    
    @Override
    public String getHelp() {
        StringBuilder strBuf = new StringBuilder("-------------- OUT Package ----------\n")
                .append(" sendOutputMsg command + nodeId as param sends empty package out \n ");
        return strBuf.toString();
    }
}
