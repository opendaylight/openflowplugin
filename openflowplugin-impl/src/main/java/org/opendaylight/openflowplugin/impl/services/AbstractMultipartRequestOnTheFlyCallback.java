/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsGatheringUtils;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultipartRequestOnTheFlyCallback<T extends OfHeader> extends AbstractMultipartRequestCallback<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMultipartRequestOnTheFlyCallback.class);
    private final DeviceInfo deviceInfo;
    private boolean finished = false;
    private final EventIdentifier doneEventIdentifier;
    private final TxFacade txFacade;
    private final MultipartWriterProvider statisticsWriterProvider;

    public AbstractMultipartRequestOnTheFlyCallback(final RequestContext<List<T>> context, Class<?> requestType,
                                                    final DeviceContext deviceContext,
                                                    final EventIdentifier eventIdentifier,
                                                    final MultipartWriterProvider statisticsWriterProvider) {
        super(context, requestType, deviceContext, eventIdentifier);
        deviceInfo = deviceContext.getDeviceInfo();
        doneEventIdentifier = new EventIdentifier(getMultipartType().name(), deviceContext.getDeviceInfo().getNodeId().toString());
        txFacade = deviceContext;
        this.statisticsWriterProvider = statisticsWriterProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(final OfHeader result) {
        if (Objects.isNull(result)) {
            LOG.info("OfHeader was null.");
            if (!finished) {
                endCollecting();
                return;
            }
        } else if (finished) {
            LOG.debug("Unexpected multipart response received: xid={}, {}", result.getXid(), result.getImplementedInterface());
            return;
        }

        if (!isMultipart(result)) {
            LOG.info("Unexpected response type received {}.", result.getClass());
            setResult(RpcResultBuilder.<List<T>>failed().withError(RpcError.ErrorType.APPLICATION,
                String.format("Unexpected response type received %s.", result.getClass())).build());
            endCollecting();
        } else {
            final T resultCast = (T) result;

            Futures.transform(processStatistics(resultCast), (Function<List<? extends DataContainer>, Void>) input -> {
                StatisticsGatheringUtils.writeStatistics(getMultipartType(), input, deviceInfo, statisticsWriterProvider);

                if (!isReqMore(resultCast)) {
                    endCollecting();
                }

                return null;
            });
        }
    }

    /**
     * Get tx facade
     * @return tx facade
     */
    protected TxFacade getTxFacade() {
        return txFacade;
    }

    /**
     * Ends collecting of multipart data
     */
    private void endCollecting() {
        EventsTimeCounter.markEnd(doneEventIdentifier);
        EventsTimeCounter.markEnd(getEventIdentifier());
        spyMessage(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);
        txFacade.submitTransaction();
        setResult(RpcResultBuilder.success(Collections.<T>emptyList()).build());
        finished = true;
    }

    /**
     * Process statistics.
     *
     * @param result result
     */
    protected abstract ListenableFuture<List<? extends DataContainer>> processStatistics(final T result);

    /**
     * Get multipart type
     * @return multipart type
     */
    protected abstract MultipartType getMultipartType();


}
