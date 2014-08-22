/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.Path;
import org.opendaylight.net.topology.TopologyService;

import java.util.Set;

import static org.opendaylight.shell.command.LinkListCommand.elementId;

@Command(scope = "net", name = "paths", description = "Lists all network infrastructure paths")
public class PathListCommand extends AbstractShellCommand {

    private static final String FMT = "<{}>";

    @Argument(index = 0, name = "src", description = "Path source",
              required = true, multiValued = false)
    String srcId = null;

    @Argument(index = 1, name = "dst", description = "Path destination",
              required = true, multiValued = false)
    String dstId = null;


    @Override
    protected Object doExecute() throws Exception {
        TopologyService ts = get(TopologyService.class);
        DeviceId src = DeviceId.valueOf(srcId);
        DeviceId dst = DeviceId.valueOf(dstId);
        if (src == null || dst == null)
            return null;

        Set<Path> paths = ts.getPaths(src, dst);
        printPaths(paths);

        return null;
    }

    protected void printPaths(Set<Path> paths) {
        if (paths != null) {
            for (Path path : paths)
                print(path);
        }
    }

    protected void print(Path path) {
        StringBuilder sb = new StringBuilder();
        for (Link link : path.links())
            sb.append(elementId(link.src())).append(":")
                    .append(link.src().interfaceId()).append("-")
                    .append(elementId(link.dst())).append(':')
                    .append(link.dst().interfaceId()).append(", ");
        sb.delete(sb.lastIndexOf(", "), sb.length());
        print(FMT, sb);
    }

}