/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import com.google.common.base.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowReader implements Runnable, FlowCounterMBean {
    private static final Logger LOG = LoggerFactory.getLogger(FlowReader.class);
    private final DataBroker dataBroker;
    private final Integer dpnCount;
    private final boolean verbose;
    private final int flowsPerDpn;
    private final short startTableId;
    private final short endTableId;
    private final boolean isConfigDs;
    private AtomicLong flowCount = new AtomicLong();
    private AtomicInteger readOpStatus = new AtomicInteger(FlowCounter.OperationStatus.INIT.status());

    private FlowReader(final DataBroker dataBroker,
                      final Integer dpnCount,
                      final int flowsPerDpn,
                      final boolean verbose,
                      final boolean isConfigDs,
                      final short startTableId,
                      final short endTableId) {
        this.dataBroker = dataBroker;
        this.dpnCount = dpnCount;
        this.verbose = verbose;
        this.flowsPerDpn = flowsPerDpn;
        this.startTableId = startTableId;
        this.endTableId = endTableId;
        this.isConfigDs = isConfigDs;
    }

    public static FlowReader getNewInstance(final DataBroker dataBroker,
                                      final Integer dpnCount,
                                      final int flowsPerDpn,
                                      final boolean verbose,
                                      final boolean isConfigDs,
                                      final short startTableId,
                                      final short endTableId) {
        return new FlowReader(dataBroker, dpnCount, flowsPerDpn, verbose,
                isConfigDs, startTableId, endTableId);
    }

    @Override
    public void run() {
        readFlowsX(dpnCount, flowsPerDpn, verbose);
    }

    private void readFlowsX(Integer dpnCount, Integer flowsPerDPN, boolean verbose) {
        readOpStatus.set(FlowCounter.OperationStatus.IN_PROGRESS.status());
        for (int i = 1; i <= dpnCount; i++) {
            String dpId = BulkOMaticUtils.DEVICE_TYPE_PREFIX + i;
            for (int j = 0; j < flowsPerDPN; j++) {
                short tableRollover = (short)(endTableId - startTableId + 1);
                short tableId = (short) (((j) % tableRollover) + startTableId);

                Integer sourceIp = j + 1;

                String flowId = "Flow-" + dpId + "." + tableId + "." + sourceIp;
                InstanceIdentifier<Flow> flowIid = getFlowInstanceIdentifier(dpId, tableId, flowId);

                ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
                try {
                    Optional<Flow> flowOptional;
                    if(isConfigDs) {
                        flowOptional = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, flowIid).checkedGet();
                    } else {
                        flowOptional = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, flowIid).checkedGet();
                    }

                    if (flowOptional.isPresent()) {
                        flowCount.incrementAndGet();
                        if (verbose) {
                            LOG.info("Flow found: {}", flowOptional.get());
                        }
                    } else {
                        if (verbose) {
                            LOG.info("Flow: {} not found", flowIid);
                        }
                    }
                } catch (ReadFailedException e) {
                    readOpStatus.set(FlowCounter.OperationStatus.FAILURE.status());
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        if(readOpStatus.get() != FlowCounter.OperationStatus.FAILURE.status()) {
            readOpStatus.set(FlowCounter.OperationStatus.SUCCESS.status());
        }
        LOG.info("Total Flows read: {}", flowCount);
    }

    private InstanceIdentifier<Flow> getFlowInstanceIdentifier(String dpId, Short tableId, String flowId){
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(new NodeId(dpId)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .child(Flow.class,
                        new FlowKey(new FlowId(flowId)));
    }

    @Override
    public long getFlowCount() {
        return flowCount.get();
    }

    @Override
    public int getReadOpStatus() {
        return readOpStatus.get();
    }
}