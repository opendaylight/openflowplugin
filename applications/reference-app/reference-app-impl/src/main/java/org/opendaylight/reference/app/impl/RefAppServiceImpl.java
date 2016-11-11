/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.reference.app.impl;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.reference.app.subscriber.SubscriberHandler;
import org.opendaylight.reference.app.subscriber.SubscriberTask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ref.app.rev160504.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * RefAppService class provides implementation for addSubcriber,
 * removeSubscriber and getSubscriberCount RPC.
 */
public class RefAppServiceImpl implements RefAppModelService {

    private static final Logger LOG = LoggerFactory.getLogger(RefAppServiceImpl.class);
    private final DataBroker dataBroker;
    // threadpool size 1 to execute either add or remove subscriber at a time
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public RefAppServiceImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public Future<RpcResult<Void>> addSubscribers(AddSubscribersInput input) {
        int batchSize = 1;
        if (input.getBatchSize() != null) {
            batchSize = input.getBatchSize().intValue();
        }
        LOG.info("add subscribers : start-index {}, start-ip {}, count {}, batchsize {} ", input.getStartingIndex(),
                input.getStartingIp(), input.getSubscriberCount(), batchSize);
        SubscriberTask task = new SubscriberTask(true, batchSize, input.getStartingIndex().intValue(),
                input.getSubscriberCount().intValue(), input.getStartingIp(), dataBroker);
        executorService.execute(task);
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<Void>> removeSubscribers(RemoveSubscribersInput input) {
        int batchSize = 1;
        if (input.getBatchSize() != null) {
            batchSize = input.getBatchSize().intValue();
        }
        LOG.info("remove subscribers : start-index {}, count {}, batchsize {} ", input.getStartingIndex(),
                input.getSubscriberCount(), batchSize);
        SubscriberTask task = new SubscriberTask(false, batchSize, input.getStartingIndex().intValue(),
                input.getSubscriberCount().intValue(), null, dataBroker);
        executorService.execute(task);
        RpcResultBuilder<Void> rpcResultBuilder = RpcResultBuilder.success();
        return Futures.immediateFuture(rpcResultBuilder.build());
    }

    @Override
    public Future<RpcResult<GetSubscriberCountOutput>> getSubscriberCount() {
        LOG.info("get subscriber count : ");
        RpcResultBuilder<GetSubscriberCountOutput> rpcResultBuilder = RpcResultBuilder.success();
        GetSubscriberCountOutputBuilder builder = new GetSubscriberCountOutputBuilder();
        int size = SubscriberHandler.getListOfHash().size();
        int subscriberCount = 0;
        for (int i = 0; i < size; i++) {
            subscriberCount += SubscriberHandler.getListOfHash().get(i).size();
        }
        builder.setSubscriberCount((long) subscriberCount);
        builder.setOperationalStatus(OperStatus.SUCCESS);
        rpcResultBuilder.withResult(builder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}