/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;

/**
 * Manager to start or stop scheduling statistics.
 */
public interface StatisticsManager extends OFPManager {

    /**
     * Start scheduling statistic gathering for given device info.
     * @param deviceInfo for this device should be running statistics gathering
     */
    void startScheduling(DeviceInfo deviceInfo);

    /**
     * Stop scheduling statistic gathering for given device info.
     * @param deviceInfo for this device should be stopped statistics gathering
     */
    void stopScheduling(DeviceInfo deviceInfo);

    /**
     * If plugin uses reconciliation framework. This is shortcut to
     * {@link OpenflowProviderConfig#isUsingReconciliationFramework()}
     * to avoid push {@link OpenflowProviderConfig} to each context.
     * @return {@link ConfigurationProperty#USING_RECONCILIATION_FRAMEWORK}
     * @since 0.5.0 Nitrogen
     */
    boolean isUsingReconciliationFramework();

    @Override
    void close();

    StatisticsContext createContext(@Nonnull DeviceContext deviceContext);

}
