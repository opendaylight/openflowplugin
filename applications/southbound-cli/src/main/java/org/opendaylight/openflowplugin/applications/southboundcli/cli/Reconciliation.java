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
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "reconcile", description = "Launch reconciliation for openflow nodes")
public class Reconciliation extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(Reconciliation.class);
    private ReconciliationService reconciliationService;

    public void setReconciliationService(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Argument(name = "nodeId", description = "The NODE Id", multiValued = true)
    List<Long> nodeIds;

    @Option(name = "-all", description = "Reconcile all operative NODEs")
    boolean reconcileAllNodes;

    @Override
    protected Object doExecute() throws Exception {
        List<BigInteger> nodes = (nodeIds == null)
                ? new ArrayList<>()
                : nodeIds.stream().distinct().map(node -> BigInteger.valueOf(node)).collect(Collectors.toList());
        LOG.debug("Triggering reconciliation for nodes {}", nodes);
        ReconcileInput rpcInput = new ReconcileInputBuilder().setNodes(nodes)
                .setReconcileAllNodes(reconcileAllNodes).build();
        Future<RpcResult<ReconcileOutput>> rpcOutput = reconciliationService.reconcile(rpcInput);
        try {
            RpcResult<ReconcileOutput> rpcResult = rpcOutput.get();
            if (rpcResult.isSuccessful()) {
                session.getConsole().println("Reconciliation triggered for the node(s)");
                printInProgressNodes(rpcResult.getResult());
            } else {
                session.getConsole().println(rpcResult.getErrors().stream().findFirst().get().getMessage());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking reconcile RPC for node {}", nodes, e);
        }
        return null;
    }

    private void printInProgressNodes(ReconcileOutput reconcileOutput) {
        List<BigInteger> inprogressNodes = reconcileOutput.getInprogressNodes();
        if (inprogressNodes.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            final Formatter formatter = new Formatter(stringBuilder);
            session.getConsole().println(getReconcileHeaderOutput());
            session.getConsole().println("----------------------------------------------------");
            for (BigInteger node : inprogressNodes) {
                session.getConsole().println(formatter.format("%-15s %n",node).toString());
                stringBuilder.setLength(0);
            }
        }
    }

    private String getReconcileHeaderOutput() {
        final Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %n", "Reconciliation already InProgress for below node(s)").toString();
        formatter.close();
        return header;
    }
}