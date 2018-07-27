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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;

@Command(scope = "openflow", name = "getReconciliationCount",
        description = "Displays the number of reconciliation triggered for openflow nodes")
public class ReconciliationCount extends OsgiCommandSupport {

    private DataBroker dataBroker;

    public void setDataBroker(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    protected Object doExecute() throws Exception {
        List<ReconcileCounter> result = ShellUtil.getReconcileCount(dataBroker);
        if (result.isEmpty()) {
            session.getConsole().println("Reconciliation count not yet available for openflow nodes.");
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            final Formatter formatter = new Formatter(stringBuilder);
            session.getConsole().println(getReconcileCountHeaderOutput());
            session.getConsole().println("--------------------------------------------------------------------------"
                    + "---------------------------");
            for (ReconcileCounter reconcile : result) {
                session.getConsole().println(formatter.format("%-15s %3s %-15s %9s %-20s %4s %-20s %n",
                        reconcile.getNodeId(), "", reconcile.getSuccessCount(), "", reconcile.getFailureCount(), "",
                        reconcile.getLastRequestTime().getValue()).toString());
                stringBuilder.setLength(0);
            }
            formatter.close();
        }
        return null;
    }

    private String getReconcileCountHeaderOutput() {
        final Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %3s %-15s %3s %-15s %3s %-15s %n", "NodeId", "",
                "ReconcileSuccessCount", "", "ReconcileFailureCount", "", "LastReconcileTime").toString();
        formatter.close();
        return header;
    }
}
