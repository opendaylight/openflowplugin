/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.shell.command;

import org.apache.karaf.shell.commands.Command;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.TopologyCluster;
import org.opendaylight.net.topology.TopologyService;

import java.util.Set;

@Command(scope = "net", name = "clusters", description = "Lists all network topology clusters")
public class ClusterListCommand extends AbstractShellCommand {

    private static final String FMT = "{} root={} devices={} links={}";

    @Override
    protected Object doExecute() throws Exception {
        TopologyService ts = get(TopologyService.class);
        for (TopologyCluster cluster : ts.getClusters())
            print(cluster, ts.getClusterDevices(cluster));
        return null;
    }

    protected void print(TopologyCluster cluster, Set<DeviceId> deviceIds) {
        print(FMT, cluster.id(), cluster.root(), cluster.deviceCount(), cluster.linkCount());
        StringBuilder sb = new StringBuilder();
        for (DeviceId id : deviceIds)
            sb.append(id).append(", ");
        sb.delete(sb.lastIndexOf(", "), sb.length());
        print("    {{}}", sb);
    }

}