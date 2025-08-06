/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm.shell;

import java.util.concurrent.ExecutionException;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.ops.rev180626.ServiceOps;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

/**
 * Implementation class of "srm:debug" Karaf shell command.
 */
@Service
@Command(scope = "srm", name = "debug", description = "SRM debug commands")
public class SrmDebugCommand implements Action {
    @Option(name = "-c", aliases = {"--clear-ops"}, description = "Clear operations DS",
        required = true, multiValued = false)
    private boolean clearOps;
    @Reference
    private DataBroker txDataBroker;

    @Override
    public @Nullable Object execute() throws ExecutionException, InterruptedException {
        if (clearOps && txDataBroker != null) {
            final var tx = txDataBroker.newWriteOnlyTransaction();
            tx.delete(LogicalDatastoreType.OPERATIONAL, DataObjectIdentifier.builder(ServiceOps.class).build());
            tx.commit().get();
        }
        return null;
    }
}
