/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Implementation provider of this interface will implement arbitrator reconciliation functionality. In
 * arbitrator reconciliation, the orchestrator is to perform the bundle-based reconciliation including any
 * error-handling rather than the Openflowplugin performing reconciliation.
 */
public interface ArbitratorReconciliationManager extends ReconciliationNotificationListener, ConfigurationListener,
        AutoCloseable {

    /**
     * Starts this manager.
     */
    void start();

    /**
     * Method checks if arbitrator reconciliation is enabled.
     *
     * @return true if arbitrator reconciliation is enabled, else false
     */
    boolean isArbitratorReconciliationEnabled();

    /**
     * Method returns the bundleId active for a node.
     *
     * @param node - node instance identifier
     *
     * @return BundleId - the bundleId active for the node
     */
    BundleId getActiveBundle(InstanceIdentifier<FlowCapableNode> node);

    /**
     * Commits the active bundle open for a node.
     *
     * @param node - node instance identifier
     *
     * @return RpcResult of action
     */
    ListenableFuture<RpcResult<Void>> commitActiveBundle(InstanceIdentifier<FlowCapableNode> node);

}
