/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.testcommon.DropTestCommiter;

@Command(scope = "drop-test", name = "dropAllPackets",
         description = "drop packet responder involving dataStore and FRM")
public class DropAllPacketsCommandProvider extends OsgiCommandSupport {
    @Reference
    DropTestCommiter provider;

    @Argument(index = 0, name = "on-off",
            description = "target state of drop responder",
            required = true, multiValued = false)
    String targetStateArg;

    @Override
    protected Object doExecute() {
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
