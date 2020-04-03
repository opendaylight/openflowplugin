/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.OPENFLOW_PREFIX;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCache;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;

@Command(scope = "openflow", name = "getflownodecache", description = "Print all flow/group cache")
public class GetFlowGroupCacheProvider extends OsgiCommandSupport {

    @Option(name = "-d", description = "Node Id")
    String dpnId;

    private FlowGroupCacheManager flowGroupCacheManager;

    public GetFlowGroupCacheProvider(final FlowGroupCacheManager flowGroupCacheManager) {
        this.flowGroupCacheManager = flowGroupCacheManager;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @Override
    protected Object doExecute() throws Exception {
        String nodeId = OPENFLOW_PREFIX + dpnId;
        List<String> result = new ArrayList<>();

        if (dpnId == null) {
            Map<String, Queue<FlowGroupCache>> flowGroupCacheListForAllNodes = flowGroupCacheManager
                    .getAllNodesFlowGroupCache();
            if (!flowGroupCacheListForAllNodes.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                Formatter formatter = new Formatter(stringBuilder);
                result.add(getAllLocalNodesHeaderOutput());
                result.add(getLineSeparator());
                for (Map.Entry<String, Queue<FlowGroupCache>> cacheEntry : flowGroupCacheListForAllNodes.entrySet()) {
                    String[] temp = cacheEntry.getKey().split(":");
                    String node = temp[1];
                    Queue<FlowGroupCache> flowGroupCacheList = cacheEntry.getValue();
                    synchronized (flowGroupCacheList) {
                        for (FlowGroupCache cache : flowGroupCacheList) {
                            result.add(formatter.format("%-15s %1s %-10s %1s %-8s %1s %-21s %1s %-60s",
                                    node, "", cache.getDescription(), "", cache.getStatus(), "",
                                    cache.getTime(), "", cache.getId()).toString());
                            stringBuilder.setLength(0);
                        }
                    }
                }
                formatter.close();
                result.stream().forEach(p -> System.out.println(p));
            } else {
                session.getConsole().println("No flow/group is programmed yet");
            }
        } else {
            if (!flowGroupCacheManager.getAllNodesFlowGroupCache().containsKey(nodeId)) {
                session.getConsole().println("No node available for this NodeID");
                return null;
            }
            Queue<FlowGroupCache> flowGroupCacheList = flowGroupCacheManager.getAllNodesFlowGroupCache()
                    .get(nodeId);
            if (!flowGroupCacheList.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                Formatter formatter = new Formatter(stringBuilder);
                result.add(String.format("Number of flows and groups in cache for node %s : %d", nodeId,
                        flowGroupCacheList.size()));
                result.add(getLocalNodeHeaderOutput());
                result.add(getLineSeparator());
                for (FlowGroupCache cache : flowGroupCacheList) {
                    result.add(formatter.format("%-10s %1s %-8s %1s %-23s %1s %-60s",
                            cache.getDescription(), "", cache.getStatus(), "",
                            cache.getTime(), "", cache.getId()).toString());
                    stringBuilder.setLength(0);
                }
                formatter.close();
                result.stream().forEach(p -> System.out.println(p));
            } else {
                session.getConsole().println("No flow/group is programmed yet for the the node " + nodeId);
            }
        }
        return null;
    }

    private String getLocalNodeHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-10s %1s %-8s %1s %-23s %1s %-60s",
                "TableId", "", "Status", "", "Time", "", "Flow/Group Id").toString();
        formatter.close();
        return header;
    }

    private String getAllLocalNodesHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %1s %-10s %1s %-8s %1s %-23s %1s %-60s",
                "DpnId", "", "TableId", "", "Status", "", "Time", "", "Flow/Group Id").toString();
        formatter.close();
        return header;
    }

    private String getLineSeparator() {
        return "---------------------------------------------------------------------------------------------"
                + "-----------------------------------------------";
    }
}
