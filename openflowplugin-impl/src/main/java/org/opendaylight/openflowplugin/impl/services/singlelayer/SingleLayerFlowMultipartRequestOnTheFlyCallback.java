/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartRequestOnTheFlyCallback;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsGatheringUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class SingleLayerFlowMultipartRequestOnTheFlyCallback<T extends OfHeader> extends AbstractMultipartRequestOnTheFlyCallback<T> {

    private final DeviceFlowRegistry deviceFlowRegistry;
    private final DeviceInfo deviceInfo;
    private boolean virgin = true;

    public SingleLayerFlowMultipartRequestOnTheFlyCallback(RequestContext<List<T>> context, Class<?> requestType,
                                                           DeviceContext deviceContext,
                                                           EventIdentifier eventIdentifier) {
        super(context, requestType, deviceContext, eventIdentifier);
        deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();
        deviceInfo = deviceContext.getDeviceInfo();
    }

    @Override
    protected boolean isMultipart(OfHeader result) {
        return result instanceof MultipartReply
            && MultipartReply.class.cast(result).getMultipartReplyBody() instanceof MultipartReplyFlowStats;
    }

    @Override
    protected boolean isReqMore(T result) {
        return MultipartReply.class.cast(result).isRequestMore();
    }

    @Override
    protected MultipartType getMultipartType() {
        return MultipartType.OFPMPFLOW;
    }

    @Override
    protected ListenableFuture<Void> processStatistics(T result) {
        final Set<FlowsStatisticsUpdate> statisticsUpdate = Collections
            .singleton(new FlowsStatisticsUpdateBuilder()
                .setId(new NodeId(OFConstants.OF_URI_PREFIX + deviceInfo.getDatapathId().toString()))
                .setMoreReplies(isReqMore(result))
                .setTransactionId(new TransactionId(BigInteger.valueOf(result.getXid())))
                .setFlowAndStatisticsMapList(MultipartReplyFlowStats.class
                    .cast(MultipartReply.class
                        .cast(result)
                        .getMultipartReplyBody())
                    .getFlowAndStatisticsMapList())
                .build());

        final ListenableFuture<Void> future;

        if (virgin) {
            future = StatisticsGatheringUtils.deleteAllKnownFlows(deviceInfo, deviceFlowRegistry, getTxFacade());
            virgin = false;
        } else {
            future = Futures.immediateFuture(null);
        }

        return Futures.transform(future, (Function<Void, Void>) input -> {
            StatisticsGatheringUtils.writeFlowStatistics(statisticsUpdate, deviceInfo, deviceFlowRegistry, getTxFacade());
            return input;
        });
    }

}
