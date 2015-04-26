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
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;

import com.google.common.base.Preconditions;

@Command(scope = "drop-test", name = "dropAllPacketsRpc", description="drop packet responder involving SalFlowService")
public class DropAllPacketsRpcCommandProvider extends OsgiCommandSupport {

    @Argument(index = 0, name = "on-off", 
            description = "target state of drop responder", 
            required = true, multiValued = false)
    String targetStateArg = null;
    
    @Override
    protected Object doExecute() throws Exception {
        PrintStream out = session.getConsole();
        final DropTestRpcProvider provider = DropTestProviderImpl.getDropRpcProvider();
        
        if ("on".equalsIgnoreCase(targetStateArg)) {
            if (! provider.isActive()) {
                provider.start();
                out.println("DropAllFlows transitions to on");
            } else {
                out.println("DropAllFlows is already on");
            }
        } else if ("off".equalsIgnoreCase(targetStateArg)) {
            if (provider.isActive()) {
                provider.close();
                out.println("DropAllFlows transitions to off");
            } else {
                out.println("DropAllFlows is already off");
            }
        }
        return null;
    }

    //TODO: create commands
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
