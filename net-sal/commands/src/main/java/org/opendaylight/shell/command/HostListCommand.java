/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.commands.Command;
import org.opendaylight.net.host.HostService;
import org.opendaylight.net.model.Host;

import java.util.Iterator;

@Command(scope = "net", name = "hosts", description="Lists all network hosts")
public class HostListCommand extends AbstractShellCommand {

    private static final String FMT = "ip={} mac={} location={}";

    @Override
    protected Object doExecute() throws Exception {
        HostService hs = get(HostService.class);
        Iterator<Host> it = hs.getHosts();
        while (it.hasNext()) {
            print(it.next());
        }

        return null;
    }

    protected void print(Host h) {
        print(FMT, h.ip(), h.mac(), h.location());
    }

}