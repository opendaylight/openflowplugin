/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.droptestkaraf;

import java.io.PrintStream;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.testcommon.DropTestDsProvider;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcProvider;

@Command(scope = "drop-test", name = "clearDropStats", description = "Clear drop statistics.")
public class ClearDropStatsCommandProvider extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        PrintStream out = session.getConsole();
        final DropTestRpcProvider rpcProvider = DropTestProviderImpl.getDropRpcProvider();
        final DropTestDsProvider provider = DropTestProviderImpl.getDropDsProvider();

        out.println("Clearing drop statistics... ");
        rpcProvider.clearStats();
        provider.clearStats();
        out.println("Done.");

        return null;
    }

}
