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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.resync.service.rev180227.ExecResyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.resync.service.rev180227.ExecResyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.resync.service.rev180227.ExecResyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.resync.service.rev180227.ResyncService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "execresync", description = "Launch an administrative Resync")
public class ExecResync extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecResync.class);
    private ResyncService resyncService;

    public void setResyncService(ResyncService resyncService) {
        this.resyncService = resyncService;
    }

    @Argument(name = "nodeId", description = "The NODE Id", multiValued = true)
    List<Long> nodeIds;

    @Option(name = "-a", aliases = "--all", description = "All operative NODEs")
    boolean reconcileAllNodes;

    @Override
    protected Object doExecute() throws Exception {
        LOG.info("execResync: doExecute()");
        List<BigInteger> nodes = (nodeIds == null)
                ? new ArrayList<>()
                : nodeIds.stream().distinct().map(node -> BigInteger.valueOf(node)).collect(Collectors.toList());
        LOG.debug("Triggering admin resync for DPN {}", nodes);
        ExecResyncInput rpcInput = new ExecResyncInputBuilder().setNodes(nodes)
                .setReconcileAllNodes(reconcileAllNodes).build();
        Future<RpcResult<ExecResyncOutput>> rpcOutput = resyncService.execResync(rpcInput);
        try {
            RpcResult<ExecResyncOutput> rpcResult = rpcOutput.get();
            if (rpcResult.isSuccessful()) {
                LOG.info("execResync success for nodes {}", nodes);
            } else {
                LOG.error("execResync fail with error {}", rpcResult.getErrors());
            }
        } catch (ExecutionException e) {
            LOG.error("Error occurred while invoking execresync RPC for node {}", nodes, e);
        }
        return null;
    }
}