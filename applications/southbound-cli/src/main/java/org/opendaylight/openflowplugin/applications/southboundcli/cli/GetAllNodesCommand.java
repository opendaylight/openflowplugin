/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil.LINE_SEPARATOR;

import java.util.Formatter;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.openflowplugin.applications.southboundcli.DpnTracker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;

@Service
@Command(scope = "openflow", name = "getallnodes", description = "Print all nodes from the operational datastore")
public final class GetAllNodesCommand implements Action {
    @Reference
    Session session;
    @Reference
    DpnTracker dpnTracker;

    @Override
    public Object execute() throws Exception {
        if (dpnTracker == null) {
            // not initialized
            return null;
        }

        final var ofNodeList = dpnTracker.currentNodes();
        if (ofNodeList.isEmpty()) {
            session.getConsole().println("No node is connected yet");
            return null;
        }

        final var stringBuilder = new StringBuilder();
        try (var formatter = new Formatter(stringBuilder)) {
            session.getConsole().println("Number of nodes: " + ofNodeList.size());
            session.getConsole().println(getAllLocalNodesHeaderOutput());
            session.getConsole().println(LINE_SEPARATOR);
            for (OFNode ofNode : ofNodeList) {
                session.getConsole().println(formatter.format("%-15s %3s %-15s %n",
                    ofNode.getNodeId(), "", ofNode.getNodeName()).toString());
                stringBuilder.setLength(0);
            }
        }
        return null;
    }

    private static String getAllLocalNodesHeaderOutput() {
        try (var formatter = new Formatter()) {
            return formatter.format("%-15s %3s %-15s", "NodeId", "", "NodeName").toString();
        }
    }
}