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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.AdminReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ExecReconciliationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ExecReconciliationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ExecReconciliationOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "execreconciliation", description = "Launch an admin reconciliation")
public class ExecReconciliation extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecReconciliation.class);
    private AdminReconciliationService adminReconciliationService;

    public void setAdminReconciliationService(AdminReconciliationService adminReconciliationService) {
        this.adminReconciliationService = adminReconciliationService;
    }

    @Argument(name = "nodeId", description = "The NODE Id", multiValued = true)
    List<Long> nodeIds;

    @Option(name = "-a", aliases = "--all", description = "All operative NODEs")
    boolean reconcileAllNodes;

    @Override
    protected Object doExecute() throws Exception {
        LOG.info("execReconciliation: doExecute()");
        List<BigInteger> nodes = (nodeIds == null)
                ? new ArrayList<>()
                : nodeIds.stream().distinct().map(node -> BigInteger.valueOf(node)).collect(Collectors.toList());
        LOG.debug("Triggering admin reconciliation for node {}", nodes);
        ExecReconciliationInput rpcInput = new ExecReconciliationInputBuilder().setNodes(nodes)
                .setReconcileAllNodes(reconcileAllNodes).build();
        Future<RpcResult<ExecReconciliationOutput>> rpcOutput = adminReconciliationService.execReconciliation(rpcInput);
        try {
            RpcResult<ExecReconciliationOutput> rpcResult = rpcOutput.get();
            if (rpcResult.isSuccessful()) {
                LOG.info("execReconciliation successfully completed for the nodes");
            } else {
                LOG.error("execReconciliation fail with error {}", rpcResult.getErrors());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking execReconciliation RPC for node {}", nodes, e);
        }
        return null;
    }
}