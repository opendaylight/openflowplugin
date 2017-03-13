/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;

public abstract class AbstractAggregateFlowMultipartService<T extends OfHeader>
    extends AbstractMultipartService<GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput, T> {

    public AbstractAggregateFlowMultipartService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    /**
     * Process input and return reply
     * @param input input
     * @return reply
     */
    public abstract Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> handleAndReply(
        final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input);

}
