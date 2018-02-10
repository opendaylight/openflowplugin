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
import org.opendaylight.openflowplugin.api.openflow.OFPContext.CONTEXT_STATE;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.common.MultipartReplyTranslatorUtil;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsGatheringUtils;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMultipartRequestOnTheFlyCallback<T extends OfHeader>
        extends AbstractMultipartRequestCallback<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMultipartRequestOnTheFlyCallback.class);
    private final DeviceInfo deviceInfo;
    private final EventIdentifier doneEventIdentifier;
    private final TxFacade txFacade;
    private final MultipartWriterProvider statisticsWriterProvider;
    private final DeviceRegistry deviceRegistry;
    private volatile CONTEXT_STATE gatheringState = CONTEXT_STATE.INITIALIZATION;
    private ConvertorExecutor convertorExecutor;

    public AbstractMultipartRequestOnTheFlyCallback(final RequestContext<List<T>> context, Class<?> requestType,
                                                    final DeviceContext deviceContext,
                                                    final EventIdentifier eventIdentifier,
                                                    final MultipartWriterProvider statisticsWriterProvider,
                                                    final ConvertorExecutor convertorExecutor) {
        super(context, requestType, deviceContext, eventIdentifier);
        deviceInfo = deviceContext.getDeviceInfo();
        doneEventIdentifier = new EventIdentifier(getMultipartType().name(),
                deviceContext.getDeviceInfo().getNodeId().toString());
        txFacade = deviceContext;
        deviceRegistry = deviceContext;
        this.statisticsWriterProvider = statisticsWriterProvider;
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    @SuppressWarnings({"unchecked", "checkstyle:IllegalCatch"})
    public void onSuccess(final OfHeader result) {
        if (Objects.isNull(result)) {
            LOG.warn("Response received was null.");

            if (!CONTEXT_STATE.TERMINATION.equals(gatheringState)) {
                endCollecting(true);
            }

            return;
        } else if (CONTEXT_STATE.TERMINATION.equals(gatheringState)) {
            LOG.warn("Unexpected response received: xid={}, {}", result.getXid(), result.getImplementedInterface());
            return;
        }

        if (!isMultipart(result)) {
            LOG.warn("Unexpected response type received: {}.", result.getClass());
            setResult(RpcResultBuilder.<List<T>>failed().withError(RpcError.ErrorType.APPLICATION,
                    String.format("Unexpected response type received: %s.", result.getClass())).build());
            endCollecting(false);
        } else {
            final T resultCast = (T) result;

            if (CONTEXT_STATE.INITIALIZATION.equals(gatheringState)) {
                startCollecting();
            }

            try {
                MultipartReplyTranslatorUtil
                        .translate(resultCast, deviceInfo, convertorExecutor, null)
                        .ifPresent(reply -> {
                            try {
                                statisticsWriterProvider
                                        .lookup(getMultipartType())
                                        .ifPresent(writer -> writer.write(reply, false));
                            } catch (final Exception ex) {
                                LOG.warn("Stats processing of type {} for node {} failed during write-to-tx step",
                                        getMultipartType(), deviceInfo.getLOGValue(), ex);
                            }
                        });
            } catch (final Exception ex) {
                LOG.warn("Unexpected exception occurred while translating response: {}.", result.getClass(), ex);
                setResult(RpcResultBuilder.<List<T>>failed().withError(RpcError.ErrorType.APPLICATION,
                        String.format("Unexpected exception occurred while "
                                + "translating response: %s. %s", result.getClass(), ex)).build());
                endCollecting(false);
                return;
            }

            if (!isReqMore(resultCast)) {
                endCollecting(true);
            }
        }
    }

    /**
     * Get tx facade.
     * @return tx facade
     */
    protected TxFacade getTxFacade() {
        return txFacade;
    }

    /**
     * Starts collecting of multipart data.
     */
    private synchronized void startCollecting() {
        EventsTimeCounter.markStart(doneEventIdentifier);
        gatheringState = CONTEXT_STATE.WORKING;

        final InstanceIdentifier<FlowCapableNode> instanceIdentifier = deviceInfo
                .getNodeInstanceIdentifier()
                .augmentation(FlowCapableNode.class);

        switch (getMultipartType()) {
            case OFPMPFLOW:
                StatisticsGatheringUtils.deleteAllKnownFlows(
                        getTxFacade(),
                        instanceIdentifier,
                        deviceRegistry.getDeviceFlowRegistry());
                break;
            case OFPMPMETERCONFIG:
                deviceRegistry.getDeviceMeterRegistry().processMarks();
                StatisticsGatheringUtils.deleteAllKnownMeters(
                        getTxFacade(),
                        instanceIdentifier,
                        deviceRegistry.getDeviceMeterRegistry());
                deviceRegistry.getDeviceMeterRegistry().processMarks();
                break;
            case OFPMPGROUPDESC:
                StatisticsGatheringUtils.deleteAllKnownGroups(
                        getTxFacade(),
                        instanceIdentifier,
                        deviceRegistry.getDeviceGroupRegistry());
                deviceRegistry.getDeviceGroupRegistry().processMarks();
                break;
            default:
                //no op
        }
    }

    /**
     * Ends collecting of multipart data.
     * @param setResult set empty success result
     */
    private void endCollecting(final boolean setResult) {
        gatheringState = CONTEXT_STATE.TERMINATION;
        EventsTimeCounter.markEnd(doneEventIdentifier);
        EventsTimeCounter.markEnd(getEventIdentifier());
        spyMessage(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);

        if (setResult) {
            setResult(RpcResultBuilder.success(Collections.<T>emptyList()).build());
        }

        txFacade.submitTransaction();

        switch (getMultipartType()) {
            case OFPMPFLOW:
                deviceRegistry.getDeviceFlowRegistry().processMarks();
                break;
            case OFPMPMETERCONFIG:
                deviceRegistry.getDeviceMeterRegistry().processMarks();
                break;
            case OFPMPGROUPDESC:
                deviceRegistry.getDeviceGroupRegistry().processMarks();
                break;
            default:
                //no op
        }
    }

    /**
     * Get multipart type.
     * @return multipart type
     */
    protected abstract MultipartType getMultipartType();

}
