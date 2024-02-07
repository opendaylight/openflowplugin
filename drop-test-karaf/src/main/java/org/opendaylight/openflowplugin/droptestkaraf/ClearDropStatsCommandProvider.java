/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.testcommon.DropTestCommiter;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcSender;

@Command(scope = "drop-test", name = "clearDropStats", description = "Clear drop statistics.")
public class ClearDropStatsCommandProvider extends OsgiCommandSupport {
    @Reference
    DropTestRpcSender rpcProvider;
    @Reference
    DropTestCommiter dsProvider;

    @Override
    protected Object doExecute() {
        final var out = session.getConsole();
        out.println("Clearing drop statistics... ");
        rpcProvider.clearStats();
        dsProvider.clearStats();
        out.println("Done.");
        return null;
    }
}
