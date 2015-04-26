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
import org.opendaylight.openflowplugin.testcommon.DropTestDsProvider;

import com.google.common.base.Preconditions;

@Command(scope = "drop-test", name = "dropAllPackets", description="drop packet responder involving dataStore and FRM")
public class DropAllPacketsCommandProvider extends OsgiCommandSupport {

    @Argument(index = 0, name = "on-off", 
            description = "target state of drop responder", 
            required = true, multiValued = false)
    String targetStateArg = null;
    
    
    @Override
    protected Object doExecute() throws Exception {
        PrintStream out = session.getConsole();
        final DropTestDsProvider provider = DropTestProviderImpl.getDropDsProvider();

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
}
