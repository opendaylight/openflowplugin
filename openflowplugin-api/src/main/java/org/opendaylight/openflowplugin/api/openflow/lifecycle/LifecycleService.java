/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import javax.annotation.CheckForNull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

/**
 * Service for starting or stopping all services in plugin in cluster
 */
public interface LifecycleService extends ClusterSingletonService, AutoCloseable {

    /**
     * This method registers lifecycle service to the given provider
     * @param singletonServiceProvider from md-sal binding
     * @param initializationPhaseHandler MASTER services initialization handler
     */
    void registerService(@CheckForNull final ClusterSingletonServiceProvider singletonServiceProvider,
                         @CheckForNull final ClusterInitializationPhaseHandler initializationPhaseHandler,
                         @CheckForNull final ServiceGroupIdentifier serviceGroupIdentifier,
                         @CheckForNull final DeviceInfo deviceInfo);

    /**
     * This method registers device removed handler what will be executed when device should be removed
     * from managers,
     * @param deviceRemovedHandler device removed handler
     */
    void registerDeviceRemovedHandler(final @CheckForNull DeviceRemovedHandler deviceRemovedHandler);

    @Override
    void close();
}
