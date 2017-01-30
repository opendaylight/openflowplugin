/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Statistics writer provider factory
 */
public class StatisticsWriterProviderFactory {

    /**
     * Create default #{@link org.opendaylight.openflowplugin.impl.statistics.datastore.StatisticsWriterProvider}
     * @param deviceContext device context
     * @return the statistics writer provider
     */
    public static StatisticsWriterProvider createDefaultProvider(final DeviceContext deviceContext) {
        final InstanceIdentifier<Node> instanceIdentifier = deviceContext.getDeviceInfo().getNodeInstanceIdentifier();
        final StatisticsWriterProvider provider = new StatisticsWriterProvider();
        provider.register(MultipartType.OFPMPTABLE, new TableStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPGROUP, new GroupStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPMETER, new MeterStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPPORTSTATS, new PortStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPQUEUE, new QueueStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPFLOW, new FlowStatisticsWriter(deviceContext, instanceIdentifier, deviceContext));
        provider.register(MultipartType.OFPMPGROUPDESC, new GroupDescStatisticsWriter(deviceContext, instanceIdentifier, deviceContext));
        provider.register(MultipartType.OFPMPMETERCONFIG, new MeterConfigStatisticsWriter(deviceContext, instanceIdentifier, deviceContext));
        return provider;
    }

    public static StatisticsWriterProvider createInitializationProvider(final DeviceContext deviceContext) {
        final InstanceIdentifier<Node> instanceIdentifier = deviceContext.getDeviceInfo().getNodeInstanceIdentifier();
        final StatisticsWriterProvider provider = new StatisticsWriterProvider();
        provider.register(MultipartType.OFPMPDESC, new DescStatisticsWriter(deviceContext, instanceIdentifier, deviceContext.getPrimaryConnectionContext()));
        provider.register(MultipartType.OFPMPGROUPFEATURES, new GroupFeaturesStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPMETERFEATURES, new MeterFeaturesStatisticsWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPTABLEFEATURES, new TableFeaturesStatisticsWriter(deviceContext, instanceIdentifier));
        return provider;
    }

}
