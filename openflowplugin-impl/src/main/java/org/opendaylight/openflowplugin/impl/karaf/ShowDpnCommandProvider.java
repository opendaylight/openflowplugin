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

@Command(scope = "logicalport", name = "showDpn", description = "showDpn -d <dpID>")
public class ShowDpnCommandProvider extends OsgiCommandSupport {

        public static final String OUTPUT_FORMAT = "%-24s %-20s %-15s";
        public static final String HEADER_SEPARATOR = "------------------------------------------------------------------------------------";

        @Option(name = "-d", description = "DPN Id", required = true, multiValued = false)
        String dpId;

        private DataBroker dataBroker;

        public void setDataBroker(DataBroker dataBroker) {
                this.dataBroker = dataBroker;
        }

        @Override
        protected Object doExecute() throws Exception {

                if (dpId == null) {
                        System.out.println("dpID not specified");
                        return null;
                }
                Dpn dpn = ShellUtil.getDpnX(Long.parseLong(dpId), dataBroker);
                if (dpn != null) {
                        printDpnHeaderOutput();
                        printHeaderSeparator();
                        printDpnOutput(dpn);
                } else {
                        System.out.println("No dpn available for this dpId");
                }
                return null;
        }

        public void printDpnHeaderOutput() {
                Formatter fmt = new Formatter();
                String header = fmt.format(OUTPUT_FORMAT,
                        "Dpn", "Name", "Ports").toString();
                fmt.close();
                System.out.println(header);
        }

        public void printHeaderSeparator() {
                System.out.println(HEADER_SEPARATOR);
        }

        public void printDpnOutput(Dpn dpn) {
                Formatter fmt = new Formatter();
                Set<String> portNames = new HashSet<>();
                for (String port : dpn.getPorts()) {
                        if (port != null)
                                portNames.add(port);
                }
                String lportStr = fmt.format(OUTPUT_FORMAT,
                        dpn.getDpId(), dpn.getDpnName(), portNames)
                        .toString();
                fmt.close();
                System.out.println(lportStr);
        }
}
