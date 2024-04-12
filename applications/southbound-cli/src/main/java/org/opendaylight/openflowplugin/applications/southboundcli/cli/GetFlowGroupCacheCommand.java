/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.OPENFLOW_PREFIX;
import static org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil.LINE_SEPARATOR;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Formatter;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfo;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistories;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupInfoHistory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

@Service
@Command(scope = "openflow", name = "getflownodecache", description = "Print all flow/group cache")
public final class GetFlowGroupCacheCommand implements Action {
    @Option(name = "-d", description = "Node Id")
    String dpnId;

    @Reference
    Session session;
    @Reference
    FlowGroupInfoHistories histories;

    @Override
    public Object execute() {
        if (histories == null) {
            // not initialized
            return null;
        }
        if (dpnId == null) {
            printAllNodes();
            return null;
        }

        final String nodeId = OPENFLOW_PREFIX + dpnId;
        final FlowGroupInfoHistory history = histories.getFlowGroupHistory(new NodeId(nodeId));
        if (history == null) {
            session.getConsole().println("No node available for this NodeID");
            return null;
        }
        final Collection<FlowGroupInfo> entries = history.readEntries();
        if (entries.isEmpty()) {
            session.getConsole().println("No flow/group is programmed yet for the the node " + nodeId);
            return null;
        }

        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        session.getConsole().println(String.format("Number of flows and groups in cache for node %s : %d", nodeId,
            entries.size()));
        session.getConsole().println(getLocalNodeHeaderOutput());
        session.getConsole().println(LINE_SEPARATOR);

        for (FlowGroupInfo entry : entries) {
            session.getConsole().println(fmt.format("%-10s %1s %-8s %1s %-23s %1s %-60s", entry.getDescription(), "",
                entry.getStatus(), "", getTime(entry), "", entry.getId()));
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
        final Map<NodeId, FlowGroupInfoHistory> allHistories = histories.getAllFlowGroupHistories();
        if (allHistories.isEmpty()) {
            session.getConsole().println("No flow/group is programmed yet");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        session.getConsole().println(getAllLocalNodesHeaderOutput());
        session.getConsole().println(LINE_SEPARATOR);
        for (Entry<NodeId, FlowGroupInfoHistory> entry : allHistories.entrySet()) {
            // FIXME: just seek/substring
            String[] temp = entry.getKey().getValue().split(":");
            String node = temp[1];
            for (FlowGroupInfo info : entry.getValue().readEntries()) {
                session.getConsole().println(fmt.format("%-15s %1s %-10s %1s %-8s %1s %-21s %1s %-60s", node, "",
                    info.getDescription(), "", info.getStatus(), "", getTime(info), "", info.getId()).toString());
                sb.setLength(0);
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
}
