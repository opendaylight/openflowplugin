/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static java.util.Objects.requireNonNull;

import java.util.Formatter;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.applications.southboundcli.DpnTracker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;

@Command(scope = "openflow", name = "getallnodes", description = "Print all nodes from the operational datastore")
public class GetAllNodesCommandProvider extends OsgiCommandSupport {
    private final DpnTracker dpnTracker;

    public GetAllNodesCommandProvider(final DpnTracker dpnTracker) {
        this.dpnTracker = requireNonNull(dpnTracker);
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @Override
    protected Object doExecute() throws Exception {
        final var ofNodeList = dpnTracker.currentNodes();
        if (ofNodeList.isEmpty()) {
            System.out.println("No node is connected yet");
            return null;
        }

        final var stringBuilder = new StringBuilder();
        try (var formatter = new Formatter(stringBuilder)) {
            System.out.println("Number of nodes: " + ofNodeList.size());
            System.out.println(getAllLocalNodesHeaderOutput());
            System.out.println("--------------------------------------------------------------------------");
            for (OFNode ofNode : ofNodeList) {
                System.out.println(formatter.format("%-15s %3s %-15s %n",
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