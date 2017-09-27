/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.impl.util.ShellUtil;

import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

@Command(scope = "shellUtil", name = "showDpn", description = "showDpn -d <dpnID>")
public class ShowDpnCommandProvider extends OsgiCommandSupport {

        public static final String OUTPUT_FORMAT = "%-24s %-20s %-15s";
        public static final String HEADER_SEPARATOR = "------------------------------------------------------------------------------------";

        @Option(name = "-d", description = "DPN Id", required = true, multiValued = false)
        String dpnId;

        private DataBroker dataBroker;

        public void setDataBroker(DataBroker dataBroker) {
                this.dataBroker = dataBroker;
        }

        @Override
        protected Object doExecute() throws Exception {
                if (dpnId == null) {
                        System.out.println("dpnID not specified");
                        return null;
                }
                Dpn dpn = ShellUtil.getDpnX(Long.parseLong(dpnId), dataBroker);
                if (dpn != null) {
                        printDpnHeaderOutput();
                        printHeaderSeparator();
                        printDpnOutput(dpn);
                } else {
                        System.out.println("No dpn available for this dpnId");
                }
                return null;
        }

        private void printDpnHeaderOutput() {
                Formatter formatter = new Formatter();
                String header = formatter.format(OUTPUT_FORMAT, "Dpn", "Name", "Ports").toString();
                formatter.close();
                System.out.println(header);
        }

        private void printHeaderSeparator() {
                System.out.println(HEADER_SEPARATOR);
        }

        private void printDpnOutput(Dpn dpn) {
                Formatter formatter = new Formatter();
                Set<String> portNames = new HashSet<>();
                for (String port : dpn.getPorts()) {
                        if (port != null)
                                portNames.add(port);
                }
                String portOutput = formatter.format(OUTPUT_FORMAT, dpn.getDpId(), dpn.getDpnName(), portNames)
                        .toString();
                formatter.close();
                System.out.println(portOutput);
        }
}
