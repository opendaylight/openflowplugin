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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.statistics.SinglePurposeMultipartReplyTranslator;
import org.opendaylight.openflowplugin.impl.statistics.StatisticsGatheringUtils;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
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

final class MultipartRequestOnTheFlyCallback extends AbstractRequestCallback<List<MultipartReply>> {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartRequestOnTheFlyCallback.class);
    private final DeviceContext deviceContext;
    private static final SinglePurposeMultipartReplyTranslator MULTIPART_REPLY_TRANSLATOR = new SinglePurposeMultipartReplyTranslator();
    private boolean virgin = true;
    private boolean finished = false;
    private final EventIdentifier doneEventIdentifier;


    public MultipartRequestOnTheFlyCallback(final RequestContext<List<MultipartReply>> context,
                                            final Class<?> requestType,
                                            final DeviceContext deviceContext,
                                            final EventIdentifier eventIdentifier) {
        super(context, requestType, deviceContext.getMessageSpy(), eventIdentifier);
        this.deviceContext = deviceContext;
        //TODO: this is focused on flow stats only - need more general approach if used for more than flow stats
        doneEventIdentifier = new EventIdentifier(MultipartType.OFPMPFLOW.name(), deviceContext.getPrimaryConnectionContext().getNodeId().toString());
    }

    public EventIdentifier getDoneEventIdentifier() {
        return doneEventIdentifier;
    }

    @Override
    public void onSuccess(final OfHeader result) {
        if (result == null) {
            LOG.info("Ofheader was null.");
            if (!finished) {
                endCollecting();
                return;
            }
        } else if (finished) {
            LOG.debug("Unexpected multipart response received: xid={}, {}", result.getXid(), result.getImplementedInterface());
            return;
        }

        if (!(result instanceof MultipartReply)) {
            LOG.info("Unexpected response type received {}.", result.getClass());
            final RpcResultBuilder<List<MultipartReply>> rpcResultBuilder =
                    RpcResultBuilder.<List<MultipartReply>>failed().withError(RpcError.ErrorType.APPLICATION,
                            String.format("Unexpected response type received %s.", result.getClass()));
            setResult(rpcResultBuilder.build());
            endCollecting();
        } else {
            final MultipartReply multipartReply = (MultipartReply) result;

            final MultipartReply singleReply = multipartReply;
            final List<? extends DataObject> multipartDataList = MULTIPART_REPLY_TRANSLATOR.translate(deviceContext, singleReply);
            final Iterable<? extends DataObject> allMultipartData = multipartDataList;

            //TODO: following part is focused on flow stats only - need more general approach if used for more than flow stats
            ListenableFuture<Void> future;
            if (virgin) {
                future = StatisticsGatheringUtils.deleteAllKnownFlows(deviceContext);
                virgin = false;
            } else {
                future = Futures.immediateFuture(null);
            }

            Futures.transform(future, new Function<Void, Void>() {

                @Override
                public Void apply(final Void input) {
                    StatisticsGatheringUtils.writeFlowStatistics((Iterable<FlowsStatisticsUpdate>) allMultipartData,deviceContext);

                    if (!multipartReply.getFlags().isOFPMPFREQMORE()) {
                        endCollecting();
                    }
                    return input;
                }
            });
        }
    }

    private void endCollecting() {
        EventsTimeCounter.markEnd(getDoneEventIdentifier());
        EventsTimeCounter.markEnd(getEventIdentifier());
        final RpcResult<List<MultipartReply>> rpcResult = RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build();
        spyMessage(MessageSpy.STATISTIC_GROUP.FROM_SWITCH_TRANSLATE_OUT_SUCCESS);
        setResult(rpcResult);
        deviceContext.submitTransaction();
        finished = true;
    }
}
