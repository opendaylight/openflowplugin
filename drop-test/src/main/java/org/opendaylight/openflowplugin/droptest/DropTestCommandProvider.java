/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.droptest;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.osgi.framework.BundleContext;

import com.google.common.base.Preconditions;

public class DropTestCommandProvider implements CommandProvider {

    private final BundleContext ctx;
    private final DropTestProvider provider;
    private final DropTestRpcProvider rpcProvider;
    private boolean on = false;
    private boolean sessionInitiated = false;

    public DropTestCommandProvider(final BundleContext ctx, final DropTestProvider provider,
            final DropTestRpcProvider rpcProvider) {
        this.ctx = Preconditions.checkNotNull(ctx, "BundleContext can not be null!");
        this.provider = Preconditions.checkNotNull(provider, "DropTestProvider can't be null!");
        this.rpcProvider = Preconditions.checkNotNull(rpcProvider, "DropTestRpcProvider can't be null!");
    }

    public void onSessionInitiated(final ProviderContext session) {
        Preconditions.checkNotNull(session, "ProviderContext can not be null!");
        ctx.registerService(CommandProvider.class.getName(), this, null);
        this.sessionInitiated = true;
    }

    public void _dropAllPackets(final CommandInterpreter ci) {
        if (sessionInitiated) {
            String onoff = ci.nextArgument();
            if (onoff.equalsIgnoreCase("on")) {
                if (on == false) {
                    provider.start();
                    ci.println("DropAllFlows transitions to on");
                } else {
                    ci.println("DropAllFlows is already on");
                }
                on = true;
            } else if (onoff.equalsIgnoreCase("off")) {
                if (on == true) {
                    provider.close();
                    ci.println("DropAllFlows transitions to off");
                } else {
                    ci.println("DropAllFlows is already off");
                }
                on = false;
            }
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void _dropAllPacketsRpc(final CommandInterpreter ci) {
        if (sessionInitiated) {
            String onoff = ci.nextArgument();
            if (onoff.equalsIgnoreCase("on")) {
                if (on == false) {
                    rpcProvider.start();
                    ci.println("DropAllFlows transitions to on");
                } else {
                    ci.println("DropAllFlows is already on");
                }
                on = true;
            } else if (onoff.equalsIgnoreCase("off")) {
                if (on == true) {
                    rpcProvider.close();
                    ci.println("DropAllFlows transitions to off");
                } else {
                    ci.println("DropAllFlows is already off");
                }
                on = false;
            }
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void _showDropStats(final CommandInterpreter ci) {
        if (sessionInitiated) {
            ci.println("RPC Test Statistics: " + this.rpcProvider.getStats().toString());
            ci.println("FRM Test Statistics: " + this.provider.getStats().toString());
        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    public void _clearDropStats(final CommandInterpreter ci) {
        if (sessionInitiated) {
            ci.print("Clearing drop statistics... ");
            this.rpcProvider.clearStats();
            this.provider.clearStats();
            ci.println("Done.");

        } else {
            ci.println("Session not initiated, try again in a few seconds");
        }
    }

    @Override
    public String getHelp() {
        StringBuffer help = new StringBuffer();
        help.append("---dropAllPackets---\n");
        help.append("\t dropAllPackets on     - Start dropping all packets\n");
        help.append("\t dropAllPackets off    - Stop dropping all packets\n");
        help.append("\t dropAllPacketsRpc on  - Start dropping all packets but bypassing dataStore\n");
        help.append("\t                       - add flow goes directly to RPC provided OFPlugin\n");
        help.append("\t dropAllPacketsRpc off - Stop dropping all packets but bypassing dataStore\n");
        return help.toString();
    }
}
