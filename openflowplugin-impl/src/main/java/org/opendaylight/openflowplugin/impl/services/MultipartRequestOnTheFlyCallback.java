/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import com.google.common.base.Optional;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.SinglePurposeMultipartReplyTranslator;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsGatheringUtils;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class MultipartRequestOnTheFlyCallback extends AbstractRequestCallback<List<MultipartReply>> {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartRequestOnTheFlyCallback.class);
    private final SinglePurposeMultipartReplyTranslator multipartReplyTranslator;
    private final DeviceInfo deviceInfo;
    private final DeviceFlowRegistry registry;
    private final EventIdentifier doneEventIdentifier;
    private final TxFacade txFacade;

    private Optional<FlowCapableNode> fcNodeOpt;
    private AtomicBoolean virgin = new AtomicBoolean(true);
    private AtomicBoolean finished = new AtomicBoolean(false);

    public MultipartRequestOnTheFlyCallback(final RequestContext<List<MultipartReply>> context,
                                            final Class<?> requestType,
                                            final MessageSpy messageSpy,
                                            final EventIdentifier eventIdentifier,
                                            final DeviceInfo deviceInfo,
                                            final DeviceFlowRegistry registry,
                                            final TxFacade txFacade,
                                            final ConvertorExecutor convertorExecutor) {
        super(context, requestType, messageSpy, eventIdentifier);

        this.deviceInfo = deviceInfo;
        this.registry = registry;
        this.txFacade = txFacade;

        multipartReplyTranslator = new SinglePurposeMultipartReplyTranslator(convertorExecutor);

        //TODO: this is focused on flow stats only - need more general approach if used for more than flow stats
        doneEventIdentifier = new EventIdentifier(MultipartType.OFPMPFLOW.name(), deviceInfo.getNodeId().toString());
    }

    public EventIdentifier getDoneEventIdentifier() {
        return doneEventIdentifier;
    }

    @Override
    public void onSuccess(final OfHeader result) {

        if (result == null) {
            LOG.info("Ofheader was null.");
            if (!finished.getAndSet(true)) {
                endCollecting();
                return;
            }
        } else if (finished.get()) {
            LOG.debug("Unexpected multipart response received: xid={}, {}", result.getXid(), result.getImplementedInterface());
            return;
        }

        if (!(result instanceof MultipartReply)) {
            if(!finished.getAndSet(true)) {
                LOG.info("Unexpected response type received {}.", result.getClass());
                final RpcResultBuilder<List<MultipartReply>> rpcResultBuilder =
                        RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.APPLICATION,
                                String.format("Unexpected response type received %s.", result.getClass()));
                setResult(rpcResultBuilder.build());
                endCollecting();
            }
        } else {
            final MultipartReply multipartReply = (MultipartReply) result;
            if (virgin.get()) {
                synchronized (this) {
                    if (virgin.get()) {
                        fcNodeOpt = StatisticsGatheringUtils.deleteAllKnownFlows(deviceInfo, txFacade);
                        virgin.set(false);
                    }
                }
            }

            final MultipartReply singleReply = multipartReply;
            final List<? extends DataObject> multipartDataList = multipartReplyTranslator.translate(
                    deviceInfo.getDatapathId(), deviceInfo.getVersion(), singleReply);
            final Iterable<FlowsStatisticsUpdate> allMultipartData = (Iterable<FlowsStatisticsUpdate>) multipartDataList;

            StatisticsGatheringUtils.writeFlowStatistics(allMultipartData, deviceInfo, registry, txFacade);
            if (!multipartReply.getFlags().isOFPMPFREQMORE()) {
                endCollecting();
            }
        }
    }

    private void endCollecting() {
        finished.set(true);
        EventsTimeCounter.markEnd(getDoneEventIdentifier());
        EventsTimeCounter.markEnd(getEventIdentifier());
        final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build();
        spyMessage(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);
        setResult(rpcResult);
        txFacade.submitTransaction();
    }
}
