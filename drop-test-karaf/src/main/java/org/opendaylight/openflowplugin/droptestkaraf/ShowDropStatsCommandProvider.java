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

@Command(scope = "drop-test", name = "showDropStats", description = "Show drop statistics.")
public class ShowDropStatsCommandProvider extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        PrintStream out = session.getConsole();
        final DropTestRpcProvider rpcProvider = DropTestProviderImpl.getDropRpcProvider();
        final DropTestDsProvider provider = DropTestProviderImpl.getDropDsProvider();

        out.format("RPC Test Statistics: %s%n", rpcProvider.getStats().toString());
        out.format("FRM Test Statistics: %s%n", provider.getStats().toString());

        return null;
    }

}
