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
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;

@Service
@Command(scope = "openflow", name = "shownode", description = "shownode -d <NodeID>")
@Deprecated
public class ShowNodeCommand implements Action {
    public static final String OUTPUT_FORMAT = "%-24s %-20s %-15s";
    public static final String NEW_LINE = "%-24s %-20s %-15s %n";

    @Option(name = "-d", description = "Node Id", required = true)
    String nodeId;
    @Reference
    Session session;
    @Reference
    DataBroker dataBroker;

    @Override
    public Object execute() {
        if (nodeId == null) {
            session.getConsole().println("NodeID not specified");
            return null;
        }
        OFNode node = ShellUtil.getNode(Long.parseLong(nodeId), dataBroker);
        if (node != null) {
            printNodeHeaderOutput();
            session.getConsole().println(LINE_SEPARATOR);
            printNodeOutput(node);
        } else {
            session.getConsole().println("No node available for this NodeID");
        }
        return null;
    }

    private void printNodeHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format(OUTPUT_FORMAT, "NodeId", "Name", "Ports").toString();
        formatter.close();
        session.getConsole().println(header);
    }

    private void printNodeOutput(final OFNode ofNode) {
        String ofNodeId = ofNode.getNodeId().toString();
        String ofNodeName = ofNode.getNodeName();
        session.getConsole().print(new Formatter().format(NEW_LINE, ofNodeId, ofNodeName, ofNode.getPorts()));
    }
}