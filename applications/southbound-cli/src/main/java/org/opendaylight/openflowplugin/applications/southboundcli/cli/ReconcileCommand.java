/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil.LINE_SEPARATOR;

import java.util.Formatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.openflowplugin.applications.southboundcli.ReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Command(scope = "openflow", name = "reconcile", description = "Launch reconciliation for openflow nodes")
public final class ReconcileCommand implements Action {
    private static final Logger LOG = LoggerFactory.getLogger(ReconcileCommand.class);
    @Reference
    Session session;
    @Reference
    ReconcileService reconciliationService = null;

    @Argument(name = "nodeId", description = "The NODE Id", multiValued = true)
    List<Long> nodeIds;

    @Option(name = "-all", description = "Reconcile all operative NODEs")
    boolean reconcileAllNodes;

    @Override
    public Object execute() throws Exception {
        if (reconciliationService == null) {
            // not initialized
            return null;
        }

        final var nodes = nodeIds == null ? Set.<Uint64>of()
            : nodeIds.stream().map(Uint64::valueOf).collect(Collectors.toSet());
        final var rpcOutput = reconcileAllNodes ? reconciliationService.reconcileAll()
            : reconciliationService.reconcile(nodes);
        LOG.debug("Triggering reconciliation for nodes {}", nodes);
        try {
            final var rpcResult = rpcOutput.get();
            if (rpcResult.isSuccessful()) {
                session.getConsole().println("Reconciliation triggered for the node(s)");
                printInProgressNodes(rpcResult.getResult());
            } else {
                session.getConsole().println(rpcResult.getErrors().stream().findFirst().orElseThrow().getMessage());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking reconcile RPC for node {}", nodes, e);
        }
        return null;
    }

    private void printInProgressNodes(final ReconcileOutput reconcileOutput) {
        final var inprogressNodes = reconcileOutput.getInprogressNodes();
        if (inprogressNodes.size() > 0) {
            final var stringBuilder = new StringBuilder();
            try (var formatter = new Formatter(stringBuilder)) {
                session.getConsole().println(getReconcileHeaderOutput());
                session.getConsole().println(LINE_SEPARATOR);
                for (Uint64 node : inprogressNodes) {
                    session.getConsole().println(formatter.format("%-15s %n",node));
                    stringBuilder.setLength(0);
                }
            }
        }
    }

    private static String getReconcileHeaderOutput() {
        try (var formatter = new Formatter()) {
            return formatter.format("%-15s %n", "Reconciliation already InProgress for below node(s)").toString();
        }
    }
}
