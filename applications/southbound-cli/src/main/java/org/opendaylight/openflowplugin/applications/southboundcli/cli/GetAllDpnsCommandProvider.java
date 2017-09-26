/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.Dpn;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Formatter;
import java.util.List;

@Command(scope = "shellUtil", name = "getAllDpns", description = "Print all dpns from operational datastore")
public class GetAllDpnsCommandProvider extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(GetAllDpnsCommandProvider.class);

    private DataBroker dataBroker;

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    protected Object doExecute() throws Exception {
        List<Dpn> dpnList = ShellUtil.getAllDpns(dataBroker);
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);
        if (dpnList != null && dpnList.size() > 0) {
            System.out.println("Number of nodes: " + dpnList.size());
            System.out.println(getAllLocalNodesHeaderOutput());
            System.out.println("---------------------------------------------------------------------------");
            for (Dpn dpn : dpnList) {
                System.out.println(formatter.format("%-15s %3s %-15s %n",
                        dpn.getDpnId(), "", dpn.getDpnName()).toString());
                stringBuilder.setLength(0);
            }
        }
        formatter.close();
        return null;
    }

    private String getAllLocalNodesHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %3s %-15s %n", "DPID", "", "DPNName").toString();
        formatter.close();
        return header;
    }
}
