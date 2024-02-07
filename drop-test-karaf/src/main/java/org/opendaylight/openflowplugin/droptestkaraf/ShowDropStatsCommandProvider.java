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

@Command(scope = "drop-test", name = "showDropStats", description = "Show drop statistics.")
public class ShowDropStatsCommandProvider extends OsgiCommandSupport {
    @Reference
    DropTestProviderImpl provider;

    @Override
    protected Object doExecute() {
        var out = session.getConsole();
        out.format("RPC Test Statistics: %s%n", provider.getDropRpcProvider().getStats().toString());
        out.format("FRM Test Statistics: %s%n", provider.getDropDsProvider().getStats().toString());
        return null;
    }

}
