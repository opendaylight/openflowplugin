/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.table.miss.enforcer;

import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mastership change service
 */
public class TableMissEnforcerMastershipChangeService implements MastershipChangeService {
    private static final Logger LOG = LoggerFactory.getLogger(TableMissEnforcerMastershipChangeService.class);
    private final SalFlowService salFlowService;

    public TableMissEnforcerMastershipChangeService(final SalFlowService salFlowService) {
        this.salFlowService = salFlowService;
    }

    @Override
    public Future<Void> onBecomeOwner(@Nonnull DeviceInfo deviceInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Table miss enforcer connected node: {}", deviceInfo.getLOGValue());
        }

        final AddFlowInput addFlowInput = new AddFlowInputBuilder(TableMissUtils.createFlow())
                .setNode(new NodeRef(deviceInfo.getNodeInstanceIdentifier()))
                .setFlowRef(new FlowRef(
                        deviceInfo
                                .getNodeInstanceIdentifier()
                                .augmentation(FlowCapableNode.class)
                                .child(Table.class, new TableKey(TableMissUtils.TABLE_ID))
                                .child(Flow.class, new FlowKey(new FlowId(TableMissUtils.DEFAULT_FLOW_ID)))))
                .build();

        return Futures.lazyTransform(salFlowService.addFlow(addFlowInput), result -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Table miss flow added for node: {}", deviceInfo.getLOGValue());
            }

            return null;
        });
    }

    @Override
    public Future<Void> onLoseOwnership(@Nonnull DeviceInfo deviceInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Table miss enforcer disconnected node: {}", deviceInfo.getLOGValue());
        }

        return Futures.immediateFuture(null);
    }

    @Override
    public void close() throws Exception {
        // NOOP
    }
}
