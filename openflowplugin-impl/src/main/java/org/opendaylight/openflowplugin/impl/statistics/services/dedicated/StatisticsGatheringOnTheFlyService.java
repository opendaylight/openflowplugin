/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.dedicated;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartOnTheFlyService;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * collects statistics and processes them on the fly
 */
public class StatisticsGatheringOnTheFlyService<T extends OfHeader>
    extends AbstractMultipartOnTheFlyService<MultipartType, T>
    implements StatisticsGatherer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsGatheringOnTheFlyService.class);

    public StatisticsGatheringOnTheFlyService(final RequestContextStack requestContextStack,
                                              final DeviceContext deviceContext,
                                              final ConvertorExecutor convertorExecutor,
                                              final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    public ListenableFuture<RpcResult<List<T>>> getStatisticsOfType(final EventIdentifier eventIdentifier, final MultipartType type) {
        LOG.debug("Getting statistics (onTheFly) for node {} of type {}", getDeviceInfo().getNodeId(), type);
        EventsTimeCounter.markStart(eventIdentifier);
        setEventIdentifier(eventIdentifier);
        return handleServiceCall(type);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final MultipartType input) throws ServiceException {
        return MultipartRequestInputFactory.makeMultipartRequest(xid.getValue(), getVersion(), input, canUseSingleLayerSerialization());
    }

}
