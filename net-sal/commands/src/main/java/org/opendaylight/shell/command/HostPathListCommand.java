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
import org.opendaylight.net.model.HostId;
import org.opendaylight.net.model.Path;
import org.opendaylight.net.path.PathSelectionService;
import org.opendaylight.util.net.IpAddress;

import java.util.Set;

import static org.opendaylight.net.model.SegmentId.UNKNOWN;

@Command(scope = "net", name = "host-paths", description = "Lists all host paths")
public class HostPathListCommand extends PathListCommand {

    private static final String FMT = "<{}>";

    @Override
    protected Object doExecute() throws Exception {
        HostService hs = get(HostService.class);
        Host src = hs.getHost(HostId.valueOf(IpAddress.ip(srcId), UNKNOWN));
        Host dst = hs.getHost(HostId.valueOf(IpAddress.ip(dstId), UNKNOWN));
        if (src == null || dst == null)
            return null;

        PathSelectionService pss = get(PathSelectionService.class);
        Set<Path> paths = pss.getPaths(src, dst);
        printPaths(paths);

        return null;
    }

}