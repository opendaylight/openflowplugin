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
import org.opendaylight.openflowplugin.testcommon.DropTestCommiter;

@Command(scope = "drop-test", name = "dropAllPackets",
         description = "drop packet responder involving dataStore and FRM")
@Service
public class DropAllPacketsCommandProvider implements Action {
    @Reference
    DropTestCommiter provider;
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
}
