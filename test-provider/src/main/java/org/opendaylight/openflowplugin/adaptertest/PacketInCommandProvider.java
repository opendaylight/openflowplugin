/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */ 	  				 	 	 
package org.opendaylight.openflowplugin.adaptertest;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.compatibility.DataPacketAdapter;
import org.opendaylight.openflowjava.protocol.impl.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.Cookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceivedBuilder;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author jsebin
 *
 */
public class PacketInCommandProvider implements CommandProvider {

    static Logger LOG = LoggerFactory.getLogger(PacketInCommandProvider.class);
    
    private final BundleContext ctx;    
    
    private final DataPacketAdapter adapter;
    
    public PacketInCommandProvider(BundleContext ctx, DataPacketAdapter adapter) {
        LOG.info("creating command provider");
        this.ctx = ctx;
        this.adapter = adapter;
    }

    public void onSessionInitiated(ProviderContext session) {       
        LOG.info("registering command provider service");
        ctx.registerService(CommandProvider.class.getName(), this, null);               
    }
    
    public void _sendPacketIn(CommandInterpreter ci) {
        adapter.onPacketReceived(buildPacket());
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.osgi.framework.console.CommandProvider#getHelp()
     */
    @Override
    public String getHelp() {        
        String helpString = " sendPacketIn - simulate sending and adapting packet from MD-SAL to AD-SAL \n" 
                + "\t no arguments";                        
        return helpString;
    }

    private static PacketReceived buildPacket() {
        //Build packet, maybe use PacketInTranslator?
        PacketReceivedBuilder builderPckRec = new PacketReceivedBuilder();
        builderPckRec.setCookie(new Cookie(1l));
        builderPckRec.setPayload(ByteBufUtils.hexStringToBytes
                ("33 33 00 00 00 02 4e 79 c0 9c 26 ed 86 dd"
              + " 60 00 00 00 00 10 3a ff fe 80 00 00 00 00"
              + " 00 00 4c 79 c0 ff fe 9c 26 ed ff 02 00 00"
              + " 00 00 00 00 00 00 00 00 00 00 00 02 85 00"
              + " 13 28 00 00 00 00 01 01 4e 79 c0 9c 26 ed"));
        LOG.info("sending testing packet");
        return builderPckRec.build();
    }
}
