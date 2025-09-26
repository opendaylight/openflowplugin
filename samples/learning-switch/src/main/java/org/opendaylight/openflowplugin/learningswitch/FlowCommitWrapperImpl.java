/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

public class FlowCommitWrapperImpl implements FlowCommitWrapper {
    private final DataBroker dataBrokerService;

    public FlowCommitWrapperImpl(final DataBroker dataBrokerService) {
        this.dataBrokerService = dataBrokerService;
    }

    @Override
    public ListenableFuture<?> writeFlowToConfig(final DataObjectIdentifier<Flow> flowPath, final Flow flowBody) {
        ReadWriteTransaction addFlowTransaction = dataBrokerService.newReadWriteTransaction();
        addFlowTransaction.mergeParentStructurePut(LogicalDatastoreType.CONFIGURATION, flowPath, flowBody);
        return addFlowTransaction.commit();
    }
}
