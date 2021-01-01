/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import java.util.Formatter;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;

@Command(scope = "openflow", name = "shownode", description = "shownode -d <NodeID>")
public class ShowNodeCommandProvider extends OsgiCommandSupport {
    public static final String OUTPUT_FORMAT = "%-24s %-20s %-15s";
    public static final String NEW_LINE = "%-24s %-20s %-15s %n";
    public static final String HEADER_SEPARATOR = "---------------------------------------------"
            + "---------------------------------------";

    @Option(name = "-d", description = "Node Id", required = true, multiValued = false)
    String nodeId;

    private DataBroker dataBroker;

    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @Deprecated
    @Override
    protected Object doExecute() {
        if (nodeId == null) {
            System.out.println("NodeID not specified");
            return null;
        }
        OFNode node = ShellUtil.getNode(Long.parseLong(nodeId), dataBroker);
        if (node != null) {
            printNodeHeaderOutput();
            printHeaderSeparator();
            printNodeOutput(node);
        } else {
            System.out.println("No node available for this NodeID");
        }
        return null;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printNodeHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format(OUTPUT_FORMAT, "NodeId", "Name", "Ports").toString();
        formatter.close();
        System.out.println(header);
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printHeaderSeparator() {
        System.out.println(HEADER_SEPARATOR);
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printNodeOutput(final OFNode ofNode) {
        String ofNodeId = ofNode.getNodeId().toString();
        String ofNodeName = ofNode.getNodeName();
        System.out.print(new Formatter().format(NEW_LINE, ofNodeId, ofNodeName, ofNode.getPorts()).toString());
    }
}