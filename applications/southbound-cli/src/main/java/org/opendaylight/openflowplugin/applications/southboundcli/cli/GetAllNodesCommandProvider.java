/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import java.util.Formatter;
import java.util.List;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.southboundcli.NodeListener;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "getallnodes", description = "Print all nodes from the operational datastore")
public class GetAllNodesCommandProvider extends OsgiCommandSupport {
    private static final Logger LOG = LoggerFactory.getLogger(GetAllNodesCommandProvider.class);

    private DataBroker dataBroker;
    private NodeListener nodeListener;

    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setNodeListener(final NodeListener nodeListener) {
        this.nodeListener = nodeListener;
    }

    @Override
    protected Object doExecute() throws Exception {
        List<OFNode> ofNodeList = ShellUtil.getAllNodes(nodeListener);
        if (ofNodeList.isEmpty()) {
            session.getConsole().println("No node is connected yet");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            Formatter formatter = new Formatter(stringBuilder);
            session.getConsole().println("Number of nodes: " + ofNodeList.size());
            session.getConsole().println(getAllLocalNodesHeaderOutput());
            session.getConsole().println("--------------------------------------------------------------------------");
            for (OFNode ofNode : ofNodeList) {
                session.getConsole().println(formatter.format("%-15s %3s %-15s %n",
                        ofNode.getNodeId(), "", ofNode.getNodeName()).toString());
                stringBuilder.setLength(0);
            }
            formatter.close();
        }
        return null;
    }

    private String getAllLocalNodesHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %3s %-15s %n", "NodeId", "", "NodeName").toString();
        formatter.close();
        return header;
    }
}
