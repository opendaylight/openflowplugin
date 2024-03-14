/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcSender;

@Command(scope = "drop-test", name = "dropAllPacketsRpc",
         description = "drop packet responder involving SalFlowService")
@Service
public class DropAllPacketsRpcCommandProvider implements Action {
    @Reference
    DropTestRpcSender provider;
    @Reference
    Session session;

    @Argument(index = 0, name = "on-off",
            description = "target state of drop responder",
            required = true, multiValued = false)
    @Completion(DropAllPacketsCompleter.class)
    String targetStateArg;

    @Override
    public Object execute() {
        final var out = session.getConsole();
        if ("on".equalsIgnoreCase(targetStateArg)) {
            if (provider.start()) {
                out.println("DropAllFlows transitions to on");
            } else {
                out.println("DropAllFlows is already on");
            }
        } else if ("off".equalsIgnoreCase(targetStateArg)) {
            if (provider.stop()) {
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
