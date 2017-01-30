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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final SinglePurposeMultipartReplyTranslator multipartReplyTranslator;
    private final DeviceInfo deviceInfo;
    private final DeviceFlowRegistry registry;
    private boolean virgin = true;
    private boolean finished = false;
    private final EventIdentifier doneEventIdentifier;
    private final TxFacade txFacade;
    private final AtomicInteger numberOfFutures = new AtomicInteger(0);
    private final Object lock = new Object();


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
            final List<? extends DataObject> multipartDataList = multipartReplyTranslator.translate(
                    deviceInfo.getDatapathId(), deviceInfo.getVersion(), singleReply);
            final Iterable<? extends DataObject> allMultipartData = multipartDataList;

            //TODO: following part is focused on flow stats only - need more general approach if used for more than flow stats
            ListenableFuture<Void> future;

            // there is no guarantee that this code will be executed sequentially when a switch returns multiple
            // part messages. Therefore, it is necessary to synchronize and ensure that we first delete
            // all know flows before adding the new flows stats to the table and only once.
            synchronized (lock) {
	            if (virgin) {
	                future = StatisticsGatheringUtils.deleteAllKnownFlows(deviceInfo, registry, txFacade);
	                virgin = false;
	            } else {
	                future = Futures.immediateFuture(null);
	            }
	            numberOfFutures.incrementAndGet();
            }



            Futures.transform(future, new Function<Void, Void>() {

                @Override
                public Void apply(final Void input) {
                    try {
                        StatisticsGatheringUtils.writeFlowStatistics((Iterable<FlowsStatisticsUpdate>) allMultipartData,
	                            deviceInfo, registry, txFacade);
                    }finally{

	                    // this code method will process each message in parallel and therefore it is neccessary to track
	                    // how many we are processing in order to call submit once all are finished
	                    synchronized (lock) {
	                        numberOfFutures.decrementAndGet();
	                        lock.notifyAll();
	                    }
                    }

                    if (!multipartReply.getFlags().isOFPMPFREQMORE()) {

                        // when we are receiving the last message we need to submit the transaction
                        // calling "endCollecting" method. The submit of the transaction should be only
                        // done once all multipart message has been processed to ensure all changes are
                        // committed together.
                        do {
                            synchronized (lock) {
                                if (numberOfFutures.get()> 0){
                                    try {
                                        lock.wait();
                                    } catch (InterruptedException e) {
                                    }
                                }else {
                                    break;
                                }
                            }
                        }while (true);

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
        txFacade.submitTransaction();
        setResult(rpcResult);
        finished = true;
    }
}
