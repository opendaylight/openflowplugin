/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

public interface FlowCommitWrapper {
    /**
     * Starts and commits data change transaction which  modifies provided flow path with supplied body.
     *
     * @param flowPath the flow path
     * @param flowBody the flow body
     * @return transaction commit
     */
    ListenableFuture<?> writeFlowToConfig(DataObjectIdentifier<Flow> flowPath, Flow flowBody);
}
