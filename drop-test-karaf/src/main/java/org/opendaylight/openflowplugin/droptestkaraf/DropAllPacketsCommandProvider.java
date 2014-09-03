/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.droptestkaraf;

import java.io.PrintStream;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.testcommon.DropTestProvider;

import com.google.common.base.Preconditions;

@Command(scope = "drop-test", name = "dropAllPackets", description="drop packet responder involving dataStore and FRM")
public class DropAllPacketsCommandProvider extends OsgiCommandSupport {

    private final DropTestProvider provider;
//    private final DropTestRpcProvider rpcProvider;
    private boolean responderActive = false;
    
    @Argument(index = 0, name = "on-off", 
            description = "target state of drop responder", 
            required = true, multiValued = false)
    String targetStateArg = null;
    
    /**
     * default ctor
     */
    public DropAllPacketsCommandProvider() {
        provider = new DropTestProvider();
    }
    
    @Override
    protected Object doExecute() throws Exception {
        PrintStream out = session.getConsole();
        provider.setDataService(Preconditions.checkNotNull(
                bundleContext.getService(bundleContext.getServiceReference(DataBroker.class))));
        provider.setNotificationService(Preconditions.checkNotNull(
                bundleContext.getService(bundleContext.getServiceReference(NotificationProviderService.class))));

        if ("on".equalsIgnoreCase(targetStateArg)) {
            if (! responderActive) {
                provider.start();
                out.println("DropAllFlows transitions to on");
            } else {
                out.println("DropAllFlows is already on");
            }
            responderActive = true;
        } else if ("off".equalsIgnoreCase(targetStateArg)) {
            if (responderActive == true) {
                provider.close();
                out.println("DropAllFlows transitions to off");
            } else {
                out.println("DropAllFlows is already off");
            }
            responderActive = false;
        }
        out.println("hello commander");
        return null;
    }

//    public void _dropAllPacketsRpc(final CommandInterpreter ci) {
//        if (sessionInitiated) {
//            String onoff = ci.nextArgument();
//            if (onoff.equalsIgnoreCase("on")) {
//                if (on == false) {
//                    rpcProvider.start();
//                    ci.println("DropAllFlows transitions to on");
//                } else {
//                    ci.println("DropAllFlows is already on");
//                }
//                on = true;
//            } else if (onoff.equalsIgnoreCase("off")) {
//                if (on == true) {
//                    rpcProvider.close();
//                    ci.println("DropAllFlows transitions to off");
//                } else {
//                    ci.println("DropAllFlows is already off");
//                }
//                on = false;
//            }
//        } else {
//            ci.println("Session not initiated, try again in a few seconds");
//        }
//    }
//
//    public void _showDropStats(final CommandInterpreter ci) {
//        if (sessionInitiated) {
//            ci.println("RPC Test Statistics: " + this.rpcProvider.getStats().toString());
//            ci.println("FRM Test Statistics: " + this.provider.getStats().toString());
//        } else {
//            ci.println("Session not initiated, try again in a few seconds");
//        }
//    }
//
//    public void _clearDropStats(final CommandInterpreter ci) {
//        if (sessionInitiated) {
//            ci.print("Clearing drop statistics... ");
//            this.rpcProvider.clearStats();
//            this.provider.clearStats();
//            ci.println("Done.");
//
//        } else {
//            ci.println("Session not initiated, try again in a few seconds");
//        }
//    }
   
}
