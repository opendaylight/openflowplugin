/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.reconciliation.cli;

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * CLI to display the service priority, service name and service status
 * FIXME: service status
 */
@Command(scope = "reconciliation", name = "getRegisteredServices",
    description = "displaying services registered to Reconciliation Framework")
public class GetRegisteredServices extends OsgiCommandSupport {
    private static final Logger LOG = LoggerFactory.getLogger(GetRegisteredServices.class);
    private static final String CLI_FORMAT = "%d %-20s ";

    @Reference
    ReconciliationManager reconciliationManager;

    @Override
    protected Object doExecute() {
        LOG.debug("Executing getRegisteredServices to Reconciliation Framework command");
        if (reconciliationManager.getRegisteredServices().isEmpty()) {
            session.getConsole().println("No Services have registered to Reconciliation Framework");
        } else {
            for (var services : reconciliationManager.getRegisteredServices() .values()) {
                for (var service : services) {
                    session.getConsole().println(String.format(CLI_FORMAT, service.getPriority(), service.getName()));
                }
            }
        }
        return null;
    }
}
