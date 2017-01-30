/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartRequestOnTheFlyCallback;
import org.opendaylight.openflowplugin.impl.statistics.SinglePurposeMultipartReplyTranslator;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsGatheringUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class MultiLayerFlowMultipartRequestOnTheFlyCallback<T extends OfHeader> extends AbstractMultipartRequestOnTheFlyCallback<T> {

    private final SinglePurposeMultipartReplyTranslator multipartReplyTranslator;
    private final DeviceFlowRegistry deviceFlowRegistry;
    private final DeviceInfo deviceInfo;
    private boolean virgin = true;

    public MultiLayerFlowMultipartRequestOnTheFlyCallback(RequestContext<List<T>> context, Class<?> requestType,
                                                          DeviceContext deviceContext,
                                                          EventIdentifier eventIdentifier,
                                                          ConvertorExecutor convertorExecutor) {
        super(context, requestType, deviceContext, eventIdentifier);
        multipartReplyTranslator = new SinglePurposeMultipartReplyTranslator(convertorExecutor);
        deviceFlowRegistry = deviceContext.getDeviceFlowRegistry();
        deviceInfo = deviceContext.getDeviceInfo();
    }

    @Override
    protected boolean isMultipart(OfHeader result) {
        return result instanceof MultipartReply
            && MultipartReply.class.cast(result).getType().equals(getMultipartType());
    }

    @Override
    protected boolean isReqMore(T result) {
        return MultipartReply.class.cast(result).getFlags().isOFPMPFREQMORE();
    }

    @Override
    protected MultipartType getMultipartType() {
        return MultipartType.OFPMPFLOW;
    }

    @Override
    protected ListenableFuture<Void> processStatistics(T result) {
        final Set<FlowsStatisticsUpdate> multiparts = multipartReplyTranslator
            .translate(deviceInfo.getDatapathId(), deviceInfo.getVersion(), result)
            .stream()
            .filter(FlowsStatisticsUpdate.class::isInstance)
            .map(FlowsStatisticsUpdate.class::cast)
            .collect(Collectors.toSet());

        final ListenableFuture<Void> future;

        if (virgin) {
            future = StatisticsGatheringUtils.deleteAllKnownFlows(deviceInfo, deviceFlowRegistry, getTxFacade());
            virgin = false;
        } else {
            future = Futures.immediateFuture(null);
        }

        return Futures.transform(future, (Function<Void, Void>) input -> {
            StatisticsGatheringUtils.writeFlowStatistics(multiparts, deviceInfo, deviceFlowRegistry, getTxFacade());
            return input;
        });
    }

}
