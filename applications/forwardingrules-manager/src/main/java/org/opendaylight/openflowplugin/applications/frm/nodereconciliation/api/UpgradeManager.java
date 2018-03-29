/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.nodereconciliation.api;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Provider of the APIs getActiveBundle() and getUpgradeMode()
 * Implementation found in UpgradeManagerImpl.
 */
public interface UpgradeManager extends ReconciliationNotificationListener, AutoCloseable {

    void start();

    /**
     * Method returns the bundleId active,ie,open for a node.
     *
     * @param node - node instance identifier
     *
     * @return BundleId - the bundleId active for the node
     */
    BundleId getActiveBundle(InstanceIdentifier<FlowCapableNode> node);

    /**
     * Method removes the bundleId from the bundleIdMap.
     *
     * @param node - node instance identifier
     *
     * @return BundleId - the closed bundleId
     */
    BundleId closeActiveBundle(InstanceIdentifier<FlowCapableNode> node);

    /**
     * Commits the active bundle open for a node.
     *
     * @param node - node instance identifier
     *
     * @return RpcResult of action
     */
    ListenableFuture<RpcResult<Void>> commitActiveBundle(InstanceIdentifier<FlowCapableNode> node);

}
