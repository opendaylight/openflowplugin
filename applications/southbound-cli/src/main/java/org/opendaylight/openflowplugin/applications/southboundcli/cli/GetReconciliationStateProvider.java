/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import java.util.ArrayList;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.applications.frm.ReconciliationJMXServiceMBean;

@Command(scope = "openflow", name = "getreconciliationstate",
        description = "Print reconciliation state for all devices")
public class GetReconciliationStateProvider extends OsgiCommandSupport {
    private final ReconciliationJMXServiceMBean reconciliationJMXServiceMBean;

    public GetReconciliationStateProvider(final ReconciliationJMXServiceMBean reconciliationJMXServiceMBean) {
        this.reconciliationJMXServiceMBean = reconciliationJMXServiceMBean;
    }

    @Override
    protected Object doExecute() {
        final var reconciliationStates  = reconciliationJMXServiceMBean.acquireReconciliationStates();
        if (!reconciliationStates.isEmpty()) {
            final var result = new ArrayList<String>();
            reconciliationStates.forEach((datapathId, reconciliationState) -> {
                String status = String.format("%-17s %-50s", datapathId, reconciliationState);
                result.add(status);
            });
            session.getConsole().println(getHeaderOutput());
            session.getConsole().println(getLineSeparator());
            result.stream().forEach(p -> session.getConsole().println(p));
        } else {
            session.getConsole().println("Reconciliation data not available");
        }
        return null;
    }

    private static String getHeaderOutput() {
        return String.format("%-17s %-25s %-25s", "DatapathId", "Reconciliation Status", "Reconciliation Time");
    }

    private static String getLineSeparator() {
        return "-------------------------------------------------------------------";
    }
}