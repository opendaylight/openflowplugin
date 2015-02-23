/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.droptest;

import com.google.common.base.Preconditions;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.openflowplugin.testcommon.DropTestDsProvider;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcProvider;
import org.osgi.framework.BundleContext;

/**
 *
 * use the same methods defined via karaf commands (defined in drop-test-karaf artifact)
 * Can be deleted after patch https://git.opendaylight.org/gerrit/#/c/15660/ for bug 2750 will be merged.
 *
 */
@Deprecated
public class DropTestCommandProvider implements CommandProvider {
    
    private static final String SESSION_NOT_INIT = "Session not initiated, try again in a few seconds";
    private static final String DROP_ON = "DropAllFlows transitions to on";
    private static final String DROP_ALREADY_ON = "DropAllFlows is already on";
    private static final String DROP_OFF = "DropAllFlows transitions to off";
    private static final String DROP_ALREADY_OFF = "DropAllFlows is already off";
    private final BundleContext ctx;
    private final DropTestDsProvider provider;
    private final DropTestRpcProvider rpcProvider;
    private boolean sessionInitiated = false;

    public DropTestCommandProvider(final BundleContext ctx, final DropTestDsProvider provider,
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

    public void dropAllPackets(final CommandInterpreter ci) {
        if (sessionInitiated) {
            String onoff = ci.nextArgument();
            if (onoff.equalsIgnoreCase("on")) {
                if (! provider.isActive()) {
                    provider.start();
                    ci.println(DROP_ON);
                } else {
                    ci.println(DROP_ALREADY_ON);
                }
            } else if (onoff.equalsIgnoreCase("off")) {
                if (provider.isActive()) {
                    provider.close();
                    ci.println(DROP_OFF);
                } else {
                    ci.println(DROP_ALREADY_OFF);
                }
            }
        } else {
            ci.println(SESSION_NOT_INIT);
        }
    }

    public void dropAllPacketsRpc(final CommandInterpreter ci) {
        if (sessionInitiated) {
            String onoff = ci.nextArgument();
            if (onoff.equalsIgnoreCase("on")) {
                if (! rpcProvider.isActive()) {
                    rpcProvider.start();
                    ci.println(DROP_ON);
                } else {
                    ci.println(DROP_ALREADY_ON);
                }
            } else if (onoff.equalsIgnoreCase("off")) {
                if (rpcProvider.isActive()) {
                    rpcProvider.close();
                    ci.println(DROP_OFF);
                } else {
                    ci.println(DROP_ALREADY_OFF);
                }
            }
        } else {
            ci.println(SESSION_NOT_INIT);
        }
    }

    public void showDropStats(final CommandInterpreter ci) {
        if (sessionInitiated) {
            ci.println("RPC Test Statistics: " + this.rpcProvider.getStats().toString());
            ci.println("FRM Test Statistics: " + this.provider.getStats().toString());
        } else {
            ci.println(SESSION_NOT_INIT);
        }
    }

    public void clearDropStats(final CommandInterpreter ci) {
        if (sessionInitiated) {
            ci.print("Clearing drop statistics... ");
            this.rpcProvider.clearStats();
            this.provider.clearStats();
            ci.println("Done.");

        } else {
            ci.println(SESSION_NOT_INIT);
        }
    }

    @Override
    public String getHelp() {
        StringBuilder help = new StringBuilder();
        help.append("---dropAllPackets---\n");
        help.append("\t dropAllPackets on     - Start dropping all packets\n");
        help.append("\t dropAllPackets off    - Stop dropping all packets\n");
        help.append("\t dropAllPacketsRpc on  - Start dropping all packets but bypassing dataStore\n");
        help.append("\t                       - add flow goes directly to RPC provided OFPlugin\n");
        help.append("\t dropAllPacketsRpc off - Stop dropping all packets but bypassing dataStore\n");
        return help.toString();
    }
}
