/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli.cli;

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
import org.opendaylight.yangtools.yang.common.Uint64;
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

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @Override
    protected Object doExecute() throws Exception {
        List<Uint64> nodes = nodeIds == null
                ? new ArrayList<>()
                : nodeIds.stream().distinct().map(Uint64::valueOf).collect(Collectors.toList());
        LOG.debug("Triggering reconciliation for nodes {}", nodes);
        ReconcileInput rpcInput = new ReconcileInputBuilder().setNodes(nodes)
                .setReconcileAllNodes(reconcileAllNodes).build();
        Future<RpcResult<ReconcileOutput>> rpcOutput = reconciliationService.reconcile(rpcInput);
        try {
            RpcResult<ReconcileOutput> rpcResult = rpcOutput.get();
            if (rpcResult.isSuccessful()) {
                System.out.println("Reconciliation triggered for the node(s)");
                printInProgressNodes(rpcResult.getResult());
            } else {
                System.out.println(rpcResult.getErrors().stream().findFirst().get().getMessage());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking reconcile RPC for node {}", nodes, e);
        }
        return null;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printInProgressNodes(ReconcileOutput reconcileOutput) {
        List<Uint64> inprogressNodes = reconcileOutput.getInprogressNodes();
        if (inprogressNodes.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            final Formatter formatter = new Formatter(stringBuilder);
            System.out.println(getReconcileHeaderOutput());
            System.out.println("----------------------------------------------------");
            for (Uint64 node : inprogressNodes) {
                System.out.println(formatter.format("%-15s %n",node).toString());
                stringBuilder.setLength(0);
            }
        }
    }

    private static String getReconcileHeaderOutput() {
        final Formatter formatter = new Formatter();
        String header = formatter.format("%-15s %n", "Reconciliation already InProgress for below node(s)").toString();
        formatter.close();
        return header;
    }
}