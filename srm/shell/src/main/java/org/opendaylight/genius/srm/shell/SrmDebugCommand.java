/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.genius.srm.shell;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.ops.rev170711.ServiceOps;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "srm", name = "debug", description = "SRM debug commands")
public class SrmDebugCommand extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SrmDebugCommand.class);

    private final DataBroker txDataBroker;

    public SrmDebugCommand(DataBroker dataBroker) {
        this.txDataBroker = dataBroker;
    }

    @Option(name = "-c", aliases = {"--clear-ops"}, description = "Clear operations DS",
        required = true, multiValued = false)
    boolean clearOps;

    @Override
    protected Object doExecute() throws Exception {
        if (clearOps) {
            clearOpsDs();
        }
        return null;
    }

    private void clearOpsDs() throws Exception {
        InstanceIdentifier<ServiceOps> path = getInstanceIdentifier();
        WriteTransaction tx = txDataBroker.newWriteOnlyTransaction();
        tx.delete(LogicalDatastoreType.OPERATIONAL, path);
        tx.submit();
    }

    private static InstanceIdentifier<ServiceOps> getInstanceIdentifier() {
        return InstanceIdentifier.create(ServiceOps.class);
    }

}
