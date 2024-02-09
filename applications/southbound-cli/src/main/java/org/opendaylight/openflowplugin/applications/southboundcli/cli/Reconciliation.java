/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.cli;

import static java.util.Objects.requireNonNull;

import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.southboundcli.NodeListener;
import org.opendaylight.openflowplugin.applications.southboundcli.alarm.AlarmAgent;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "reconcile", description = "Launch reconciliation for openflow nodes")
public class Reconciliation extends OsgiCommandSupport {
    private static final Logger LOG = LoggerFactory.getLogger(Reconciliation.class);

    @Argument(name = "nodeId", description = "The NODE Id", multiValued = true)
    List<Long> nodeIds;

    @Option(name = "-all", description = "Reconcile all operative NODEs")
    boolean reconcileAllNodes;

    private final DataBroker broker;
    private final AlarmAgent alarmAgent;
    private final NodeListener nodeListener;
    private final FlowNodeReconciliation flowNodeReconciliation;
    private final Map<String, ReconciliationState> reconciliationStates;

    public Reconciliation(final DataBroker broker, final ForwardingRulesManager frm, final AlarmAgent alarmAgent,
            final NodeListener nodeListener, final FlowGroupCacheManager flowGroupCacheManager) {
        this.broker = requireNonNull(broker);
        flowNodeReconciliation = frm.getFlowNodeReconciliation();
        this.alarmAgent = requireNonNull(alarmAgent);
        this.nodeListener = requireNonNull(nodeListener);
        reconciliationStates = flowGroupCacheManager.getReconciliationStates();
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @Override
    protected Object doExecute() throws Exception {
        Set<Uint64> nodes = nodeIds == null
                ? Set.of()
                : nodeIds.stream().distinct().map(Uint64::valueOf).collect(Collectors.toUnmodifiableSet());
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
                System.out.println(rpcResult.getErrors().stream().findFirst().orElseThrow().getMessage());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking reconcile RPC for node {}", nodes, e);
        }
        return null;
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    private static void printInProgressNodes(final ReconcileOutput reconcileOutput) {
        Set<Uint64> inprogressNodes = reconcileOutput.getInprogressNodes();
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
