/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.connection.ThrottledNotificationsOfferer;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.5.2015.
 */
public class ThrottledNotificationsOffererImpl<T extends Notification> implements ThrottledNotificationsOfferer<T>, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ThrottledNotificationsOffererImpl.class);
    private final Map<Queue<T>, SettableFuture<Void>> throttledQueues = new ConcurrentHashMap<>();
    private final ThreadPoolLoggingExecutor throttleWorkerPool;
    private final NotificationPublishService notificationPublishService;
    private final MessageSpy<Class<?>> messageIntelligenceAgency;
    private boolean finishing = false;
    private CountDownLatch sleeperLatch = new CountDownLatch(0);

    /**
     * @param notificationPublishService
     * @param messageIntelligenceAgency
     */
    public ThrottledNotificationsOffererImpl(final NotificationPublishService notificationPublishService, final MessageSpy<Class<?>> messageIntelligenceAgency) {
        this.notificationPublishService = notificationPublishService;
        this.messageIntelligenceAgency = messageIntelligenceAgency;
        throttleWorkerPool = new ThreadPoolLoggingExecutor(
                1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1), "throttleWorkerPool");
        throttleWorkerPool.execute(this);
        LOG.info("throttled worker started");
    }

    @Override
    public ListenableFuture<Void> applyThrottlingOnConnection(final Queue<T> notificationsQueue) {
        SettableFuture<Void> throttleWatching = SettableFuture.create();
        throttledQueues.put(notificationsQueue, throttleWatching);
        synchronized (throttledQueues) {
            sleeperLatch.countDown();
        }
        return throttleWatching;
    }


    @Override
    public void run() {
        while (!finishing) {
            if (throttledQueues.isEmpty()) {
                // do some sleeping
                synchronized (throttledQueues) {
                    if (throttledQueues.isEmpty()) {
                        sleeperLatch = new CountDownLatch(1);
                    }
                }
                try {
                    sleeperLatch.await();
                } catch (InterruptedException e) {
                    // NOOP
                }
            } else {
                for (Map.Entry<Queue<T>, SettableFuture<Void>> throttledTuple : throttledQueues.entrySet()) {
                    Queue<T> key = throttledTuple.getKey();
                    T notification = key.poll();
                    if (notification == null) {
                        synchronized (key) {
                            // free throttling and announce via future
                            throttledTuple.getValue().set(null);
                            throttledQueues.remove(key);
                        }
                    } else {
                        try {
                            notificationPublishService.putNotification(notification);
                            messageIntelligenceAgency.spyMessage(notification.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
                        } catch (InterruptedException e) {
                            LOG.trace("putNotification failed.. ", e);
                            messageIntelligenceAgency.spyMessage(notification.getImplementedInterface(), MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
                        }
                    }
                    if (finishing) {
                        break;
                    }
                }
            }
        }

        LOG.info("throttled worker finishing");
    }

    @Override
    public boolean isThrottlingEffective(final Queue<T> notificationsQueue) {
        return throttledQueues.containsKey(notificationsQueue);
    }

    @Override
    public void close() throws SecurityException {
        finishing = true;
        throttleWorkerPool.shutdown();
        if (!throttleWorkerPool.isTerminated()) {
            try {
                throttleWorkerPool.awaitTermination(2L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.debug("Exception during pool shutdown: {}", e.getMessage());
            } finally {
                if (!throttleWorkerPool.isTerminated()) {
                    throttleWorkerPool.shutdownNow();
                }
            }
        }
    }
}
