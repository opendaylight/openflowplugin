/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.datastore;

import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

/**
 * Statistics writer provider factory
 */
public class StatisticsWriterProviderFactory {

    /**
     * Create default #{@link org.opendaylight.openflowplugin.impl.statistics.datastore.StatisticsWriterProvider}
     * @param txFacade transaction facade
     * @return the statistics writer provider
     */
    public static StatisticsWriterProvider createDefaultProvider(final TxFacade txFacade) {
        final StatisticsWriterProvider provider = new StatisticsWriterProvider();
        provider.register(MultipartType.OFPMPTABLE, new FlowTableStatisticsWriter(txFacade));
        provider.register(MultipartType.OFPMPGROUP, new GroupStatisticsWriter(txFacade));
        provider.register(MultipartType.OFPMPMETER, new MeterStatisticsWriter(txFacade));
        provider.register(MultipartType.OFPMPPORTSTATS, new PortStatisticsWriter(txFacade));
        provider.register(MultipartType.OFPMPQUEUE, new QueueStatisticsWriter(txFacade));
        return provider;
    }

}
