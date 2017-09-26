/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.Dpn;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;

@Command(scope = "shellUtil", name = "showDpn", description = "showDpn -d <dpnID>")
public class ShowDpnCommandProvider extends OsgiCommandSupport {

    public static final String OUTPUT_FORMAT = "%-24s %-20s %-15s";
    public static final String HEADER_SEPARATOR = "---------------------------------------------"
            + "---------------------------------------";

    @Option(name = "-d", description = "DPN Id", required = true, multiValued = false)
    String dpnId;

    private DataBroker dataBroker;

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    protected Object doExecute() throws Exception {
        if (dpnId == null) {
            session.getConsole().println("dpnID not specified");
            return null;
        }
        Dpn dpn = ShellUtil.getDpnX(Long.parseLong(dpnId), dataBroker);
        if (dpn != null) {
            printDpnHeaderOutput();
            printHeaderSeparator();
            printDpnOutput(dpn);
        } else {
            session.getConsole().println("No dpn available for this dpnId");
        }
        return null;
    }

    private void printDpnHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format(OUTPUT_FORMAT, "Dpn", "Name", "Ports").toString();
        formatter.close();
        session.getConsole().println(header);
    }

    private void printHeaderSeparator() {
        session.getConsole().println(HEADER_SEPARATOR);
    }

    private void printDpnOutput(Dpn dpn) {
        Formatter formatter = new Formatter();
        Set<String> portNames = new HashSet<>();
        for (String port : dpn.getPorts()) {
            if (port != null) {
                portNames.add(port);
            }
        }
        String portOutput = formatter.format(OUTPUT_FORMAT, dpn.getDpnId(), dpn.getDpnName(), portNames)
                .toString();
        formatter.close();
        session.getConsole().println(portOutput);
    }
}
