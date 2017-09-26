/*
 * Copyright Ericsson AB 2015 and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import org.apache.felix.gogo.commands.Command;

import org.apache.karaf.shell.console.OsgiCommandSupport;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.impl.util.ShellUtil;
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

                LOG.info("GetAllDpns : doExecute()");

                List<Dpn> lstNode = ShellUtil.getAllDpns(dataBroker);
                StringBuilder sb = new StringBuilder();
                Formatter fmt = new Formatter(sb);
                LOG.info("GetAllDpns : got all Dpns");

                if (lstNode != null && lstNode.size() > 0) {
                        int nSize;

                        nSize = lstNode.size();

                        Dpn dpnCur;

                        System.out.println("Number of nodes: " + nSize);

                        System.out.println(getAllLocalNodesHeaderOutput());
                        System.out
                                .println("---------------------------------------------------------------------------");

                        for (int i = 0; i < lstNode.size(); i++) {
                                dpnCur = lstNode.get(i);

                                System.out.println(fmt.format("%-15s %3s %-15s %n",
                                        dpnCur.getDpId(), "", dpnCur.getDpnName()).toString());
                                sb.setLength(0);
                        }
                }
                fmt.close();
                return null;
        }

        private String getAllLocalNodesHeaderOutput() {
                Formatter fmt = new Formatter();
                String header = fmt.format("%-15s %3s %-15s %n", "DPID", "", "DPNName")
                        .toString();
                fmt.close();
                return header;
        }

}
