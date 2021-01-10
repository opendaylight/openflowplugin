/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.OPENFLOW_PREFIX;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Formatter;
import java.util.Map;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

@Command(scope = "openflow", name = "getflownodecache", description = "Print all flow/group cache")
public class GetFlowGroupCacheProvider extends OsgiCommandSupport {

    @Option(name = "-d", description = "Node Id")
    String dpnId;

    private final FlowGroupCacheManager flowGroupCacheManager;

    public GetFlowGroupCacheProvider(final FlowGroupCacheManager flowGroupCacheManager) {
        this.flowGroupCacheManager = flowGroupCacheManager;
    }

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    protected Object doExecute() {
        if (dpnId == null) {
            printAllNodes();
            return null;
        }

        final String nodeId = OPENFLOW_PREFIX + dpnId;
        Collection<FlowGroupInfo> flowGroupCacheList = flowGroupCacheManager.getAllNodesFlowGroupCache()
            .get(new NodeId(nodeId));
        if (flowGroupCacheList == null) {
            session.getConsole().println("No node available for this NodeID");
            return null;
        }
        if (flowGroupCacheList.isEmpty()) {
            session.getConsole().println("No flow/group is programmed yet for the the node " + nodeId);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        System.out.println(String.format("Number of flows and groups in cache for node %s : %d", nodeId,
            flowGroupCacheList.size()));
        System.out.println(getLocalNodeHeaderOutput());
        System.out.println(getLineSeparator());

        for (FlowGroupInfo cache : flowGroupCacheList) {
            System.out.println(fmt.format("%-10s %1s %-8s %1s %-23s %1s %-60s", cache.getDescription(), "",
                cache.getStatus(), "", getTime(cache), "", cache.getId()).toString());
            sb.setLength(0);
        }
        fmt.close();
        return null;
    }

    private static LocalDateTime getTime(final FlowGroupInfo info) {
        return LocalDateTime.ofInstant(info.getInstantUTC(), ZoneOffset.UTC);
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private void printAllNodes() {
        final Map<NodeId, Collection<FlowGroupInfo>> allGroupInfos = flowGroupCacheManager.getAllNodesFlowGroupCache();
        if (allGroupInfos.isEmpty()) {
            session.getConsole().println("No flow/group is programmed yet");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        System.out.println(getAllLocalNodesHeaderOutput());
        System.out.println(getLineSeparator());
        for (Map.Entry<NodeId, Collection<FlowGroupInfo>> cacheEntry : allGroupInfos.entrySet()) {
            // FIXME: just seek/substring
            String[] temp = cacheEntry.getKey().getValue().split(":");
            String node = temp[1];
            Collection<FlowGroupInfo> flowGroupCacheList = cacheEntry.getValue();
            synchronized (flowGroupCacheList) {
                for (FlowGroupInfo cache : flowGroupCacheList) {
                    System.out.println(fmt.format("%-15s %1s %-10s %1s %-8s %1s %-21s %1s %-60s", node, "",
                        cache.getDescription(), "", cache.getStatus(), "", getTime(cache), "",
                        cache.getId()).toString());
                    sb.setLength(0);
                }
            }
        }
        fmt.close();
    }

    private static String getLocalNodeHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-10s %1s %-8s %1s %-23s %1s %-60s",
                "TableId", "", "Status", "", "Time", "", "Flow/Group Id").toString();
        formatter.close();
        return header;
    }

    private static String getAllLocalNodesHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %1s %-10s %1s %-8s %1s %-23s %1s %-60s",
                "DpnId", "", "TableId", "", "Status", "", "Time", "", "Flow/Group Id").toString();
        formatter.close();
        return header;
    }

    private static String getLineSeparator() {
        return "---------------------------------------------------------------------------------------------"
                + "-----------------------------------------------";
    }
}
