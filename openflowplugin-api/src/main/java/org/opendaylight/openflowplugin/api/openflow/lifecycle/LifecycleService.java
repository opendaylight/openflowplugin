/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import javax.annotation.Nonnull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;

/**
 * Service for starting or stopping all services in plugin in cluster.
 */
public interface LifecycleService extends ClusterSingletonService, AutoCloseable {

    /**
     * This method registers lifecycle service to the given provider.
     * @param singletonServiceProvider      from md-sal binding
     * @param deviceContext                 device
     */
    void registerService(@Nonnull final ClusterSingletonServiceProvider singletonServiceProvider,
                         @Nonnull final DeviceContext deviceContext);

    /**
     * This method registers device removed handler what will be executed when device should be removed.
     * @param deviceRemovedHandler device removed handler
     */
    void registerDeviceRemovedHandler(@Nonnull final DeviceRemovedHandler deviceRemovedHandler);

    /**
     * Make device SLAVE.
     * @param deviceContext provide device context which can't be stored in lifecycle service.
     */
    void makeDeviceSlave(final DeviceContext deviceContext);

    /**
     * Mastering process running.
     * @return true if service was run the process to being MASTER
     */
    boolean tryToBeMaster();

    @Override
    void close();
}
