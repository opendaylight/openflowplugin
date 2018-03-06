/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import java.util.Formatter;
import java.util.List;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "getAdminReconcileCount",
        description = "Displays the number of admin reconciliation launched")
public class AdminReconciliationCount extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(AdminReconciliationCount.class);

    private DataBroker dataBroker;

    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    protected Object doExecute() throws Exception {
        String msg = "Admin reconcile launched:  ";
        List<ReconcileCounter> result = ShellUtil.getAdminReconcileCount(dataBroker);
        if (result.isEmpty()) {
            session.getConsole().println("Admin Reconciliation is not done for any node");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            Formatter formatter = new Formatter(stringBuilder);
            session.getConsole().println(getAllLocalNodesHeaderOutput());
            session.getConsole().println("--------------------------------------------------------------------------");
            for (ReconcileCounter reconcile : result) {
                session.getConsole().println(formatter.format("%-15s %3s %-15s %n",
                        reconcile.getNodeId(), "", reconcile.getNumberAdminReconciliation()).toString());
                stringBuilder.setLength(0);
            }
            formatter.close();
        }
        return null;
    }

    private String getAllLocalNodesHeaderOutput() {
        Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %3s %-15s %n", "NodeId", "", "AdminReconcileCount").toString();
        formatter.close();
        return header;
    }
}
