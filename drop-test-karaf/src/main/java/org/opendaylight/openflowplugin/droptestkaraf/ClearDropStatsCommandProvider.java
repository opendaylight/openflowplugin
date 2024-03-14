/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.openflowplugin.testcommon.DropTestCommiter;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcSender;

@Command(scope = "drop-test", name = "clearDropStats", description = "Clear drop statistics.")
@Service
public class ClearDropStatsCommandProvider implements Action {
    @Reference
    DropTestRpcSender rpcProvider;
    @Reference
    DropTestCommiter dsProvider;
    @Reference
    Session session;

    @Override
    public Object execute() {
        final var out = session.getConsole();
        out.println("Clearing drop statistics... ");
        rpcProvider.clearStats();
        dsProvider.clearStats();
        out.println("Done.");
        return null;
    }
}
