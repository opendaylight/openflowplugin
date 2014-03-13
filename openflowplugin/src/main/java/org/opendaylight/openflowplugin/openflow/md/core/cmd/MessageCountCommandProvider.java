/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.cmd;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.openflow.md.queue.MessageCountDumper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MessageCountCommandProvider implements CommandProvider, AutoCloseable {
    
    private static Logger LOG = LoggerFactory
            .getLogger(MessageCountCommandProvider.class);
    
    private boolean sessionInitiated;
    private BundleContext ctx;
    private MessageCountDumper provider;

    private ServiceRegistration commandRegistration;
    
    /**
     * @param ctx
     * @param provider
     */
    public MessageCountCommandProvider(BundleContext ctx, MessageCountDumper provider) {
        this.ctx = ctx;
        this.provider = provider;
    }

    @Override
    public String getHelp() {
        String helpString = "----------------- dumpMsgCount--------------\n"
                + " dumps message counters \n";
        return helpString;
    }
    
    /**
     * @param session
     */
    public void onSessionInitiated(ProviderContext session) {
        LOG.debug("onSessionInitiated");
        commandRegistration = ctx.registerService(CommandProvider.class.getName(), this, null);
        this.sessionInitiated = true;
    }
    
    /**
     * @param ci
     */
    public void _dumpMsgCount(CommandInterpreter ci) {
        if(sessionInitiated) {
            ci.println("dumping msg counts");
            for (String countItem : provider.dumpMessageCounts()) {
                ci.println(countItem);
            }
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }
    
    @Override
    public void close() {
        LOG.debug("close");
        if (commandRegistration != null) {
            commandRegistration.unregister();
        }
        ctx = null;
        provider = null;
        sessionInitiated = false;
    }

}
