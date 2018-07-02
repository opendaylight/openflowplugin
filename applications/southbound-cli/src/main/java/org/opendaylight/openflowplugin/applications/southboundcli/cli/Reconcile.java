/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "reconcile", description = "Launch reconciliation")
public class Reconcile extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(Reconcile.class);
    private ReconciliationService reconciliationService;

    public void setReconciliationService(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Argument(name = "nodeId", description = "The NODE Id", multiValued = true)
    List<Long> nodeIds;

    @Option(name = "-a", aliases = "--all", description = "All operative NODEs")
    boolean reconcileAllNodes;

    @Override
    protected Object doExecute() throws Exception {
        List<BigInteger> nodes = (nodeIds == null)
                ? new ArrayList<>()
                : nodeIds.stream().distinct().map(node -> BigInteger.valueOf(node)).collect(Collectors.toList());
        LOG.debug("Triggering reconciliation for node {}", nodes);
        ReconcileInput rpcInput = new ReconcileInputBuilder().setNodes(nodes)
                .setReconcileAllNodes(reconcileAllNodes).build();
        Future<RpcResult<ReconcileOutput>> rpcOutput = reconciliationService.reconcile(rpcInput);
        try {
            RpcResult<ReconcileOutput> rpcResult = rpcOutput.get();
            if (rpcResult.isSuccessful()) {
                session.getConsole().println("reconcile successfully completed for the nodes");
            } else {
                LOG.error("reconcile failed with error {}", rpcResult.getErrors());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking reconcile RPC for node {}", nodes, e);
        }
        return null;
    }
}