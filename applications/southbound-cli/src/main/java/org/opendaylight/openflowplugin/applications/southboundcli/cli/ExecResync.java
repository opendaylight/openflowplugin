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
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.InitReconciliationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.InitReconciliationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.InitReconciliationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "execresync", description = "Launch an administrative Resync")
public class ExecResync extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecResync.class);
    private ReconciliationService reconciliationService;

    public void setFrmReconciliationService(ReconciliationService reconciliationService ) {
        this.reconciliationService = reconciliationService;
    }

    @Argument(name = "nodeId", description = "The DPN Id", required = false, multiValued = false)
    List<Long> nodeIds;

    @Override
    protected Object doExecute() throws Exception {
        List<BigInteger> nodes = (nodeIds == null)
                ? new ArrayList<>()
                : nodeIds.stream().distinct().map(node -> BigInteger.valueOf(node)).collect(Collectors.toList());
        for (BigInteger node : nodes) {
            final NodeKey key = new NodeKey(new NodeId("openflow:" + node));
            InitReconciliationInput initReconInput = new InitReconciliationInputBuilder().
                    setDpnId(node).setNode(new NodeRef(InstanceIdentifier.builder(Nodes.class).
                    child(Node.class, key).build())).build();
            Future<RpcResult<InitReconciliationOutput>> initReconOutput = reconciliationService.initReconciliation(initReconInput);
            try {
                RpcResult<InitReconciliationOutput> rpcResult = initReconOutput.get();
                if (rpcResult.isSuccessful()) {
                    System.out.println("Resync successfully completed for DPN " + node);
                    LOG.info("Resync successfully completed for DPN {}", node);
                } else {
                    System.out.println("Resync failed for DPN " + node + ", please check logs");
                    LOG.error("Resync failed for DPN {} with error {}", node, rpcResult.getErrors());
                }
            } catch (ExecutionException e) {
                LOG.error("Error occurred while invoking execresync RPC for DPN {}", node, e);
            }
        }
        return null;
    }
}