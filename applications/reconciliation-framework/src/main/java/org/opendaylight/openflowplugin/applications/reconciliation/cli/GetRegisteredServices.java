/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Command(scope = "reconciliation", name = "getRegisteredServices", description = "displaying services registered to "
        + "Reconciliation Framework")
public class GetRegisteredServices extends OsgiCommandSupport {

        private IReconciliationManager reconciliationManager;
        private static final Logger LOG = LoggerFactory.getLogger(GetRegisteredServices.class);
        public static final String CLI_FORMAT = "%d %-20s ";

        public void setReconciliationmgr(IReconciliationManager reconciliationManager) {
                this.reconciliationManager = reconciliationManager;
        }

        @Override protected Object doExecute() throws Exception {
                LOG.debug("Executing getRegisteredServices to Reconciliation Framework command");
                for (Map.Entry<Integer, List<IReconciliationTaskFactory>> registeredService : reconciliationManager
                        .getRegisteredServices().entrySet()) {
                        List<IReconciliationTaskFactory> services = registeredService.getValue();
                        for (IReconciliationTaskFactory service : services) {
                                session.getConsole().println(
                                        String.format(CLI_FORMAT, service.getPriority(), service.getServiceName()));
                        }
                }
                return null;
        }

}


