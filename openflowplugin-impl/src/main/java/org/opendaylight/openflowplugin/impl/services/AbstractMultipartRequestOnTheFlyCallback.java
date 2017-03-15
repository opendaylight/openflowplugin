/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.OFPContext.CONTEXT_STATE;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultipartRequestOnTheFlyCallback<T extends OfHeader> extends AbstractMultipartRequestCallback<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMultipartRequestOnTheFlyCallback.class);
    private final DeviceInfo deviceInfo;
    private final EventIdentifier doneEventIdentifier;
    private final TxFacade txFacade;
    private final MultipartWriterProvider statisticsWriterProvider;
    private CONTEXT_STATE gatheringState = CONTEXT_STATE.INITIALIZATION;

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
            LOG.warn("OfHeader was null.");

            if (!CONTEXT_STATE.TERMINATION.equals(gatheringState)) {
                endCollecting();
                return;
            }
        } else if (CONTEXT_STATE.TERMINATION.equals(gatheringState)) {
            LOG.warn("Unexpected multipart response received: xid={}, {}", result.getXid(), result.getImplementedInterface());
            return;
        }

        if (!isMultipart(result)) {
            LOG.warn("Unexpected response type received {}.", result.getClass());
            setResult(RpcResultBuilder.<List<T>>failed().withError(RpcError.ErrorType.APPLICATION,
                String.format("Unexpected response type received %s.", result.getClass())).build());
            endCollecting();
        } else {
            final T resultCast = (T) result;

            if (CONTEXT_STATE.INITIALIZATION.equals(gatheringState)) {
                startCollecting();
            }

            Optional.ofNullable(processStatistics(resultCast)).flatMap(i -> i).ifPresent(reply -> {
                try {
                    statisticsWriterProvider
                        .lookup(getMultipartType())
                        .ifPresent(writer -> writer.write(reply, false));
                } catch (final Exception ex) {
                    LOG.warn("Stats processing of type {} for node {} failed during write-to-tx step",
                        getMultipartType(), deviceInfo.getLOGValue(), ex);
                }
            });

            if (!isReqMore(resultCast)) {
                endCollecting();
            }
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
     * Starts collecting of multipart data
     */
    private void startCollecting() {
        EventsTimeCounter.markStart(doneEventIdentifier);
        gatheringState = CONTEXT_STATE.WORKING;
        onStartCollecting();
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
        gatheringState = CONTEXT_STATE.TERMINATION;
        onFinishedCollecting();
    }

    /**
     * Process statistics.
     *
     * @param result result
     */
    protected abstract Optional<? extends MultipartReplyBody> processStatistics(final T result);

    /**
     * Get multipart type
     * @return multipart type
     */
    protected abstract MultipartType getMultipartType();

    /**
     * On start collection event
     */
    protected abstract void onStartCollecting();

    /**
     * On finished collection event
     */
    protected abstract void onFinishedCollecting();


}
