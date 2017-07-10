/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.cli;

import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "reconciliation", name = "getRegisteredServices", description = "displaying services registered to "
        + "Reconciliation Framework")
public class GetRegisteredServices extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(GetRegisteredServices.class);
    public static final String CLI_FORMAT = "%d %-20s ";

    private ReconciliationManager reconciliationManager;

    public void setReconciliationmgr(ReconciliationManager reconciliationManager) {
        this.reconciliationManager = reconciliationManager;
    }

    @Override 
    protected Object doExecute() throws Exception {
        LOG.debug("Executing getRegisteredServices to Reconciliation Framework command");
        for (Map.Entry<Integer, List<ReconciliationTaskFactory>> registeredService : reconciliationManager
                .getRegisteredServices().entrySet()) {
            List<ReconciliationTaskFactory> services = registeredService.getValue();
            for (ReconciliationTaskFactory service : services) {
                session.getConsole().println(
                        String.format(CLI_FORMAT, service.getPriority(), service.getServiceName()));
            }
        }
        return null;
    }

}


