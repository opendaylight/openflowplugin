/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.reconciliation.cli;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * CLI to display the service priority, service name and service status
 * TODO: service status
 */
@Service
@Command(scope = "reconciliation", name = "getRegisteredServices",
         description = "displaying services registered to Reconciliation Framework")
public class GetRegisteredServices implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(GetRegisteredServices.class);

    @Reference
    private ReconciliationManager reconciliationManager;

    @Override
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public Object execute() {
        LOG.debug("Executing getRegisteredServices to Reconciliation Framework command");
        if (reconciliationManager.getRegisteredServices().isEmpty()) {
            System.out.println("No Services have registered to Reconciliation Framework");
        } else {
            for (var services : reconciliationManager.getRegisteredServices().values()) {
                for (ReconciliationNotificationListener service : services) {
                    System.out.println(String.format("%d %-20s ", service.getPriority(), service.getName()));
                }
            }
        }

        return null;
    }
}
