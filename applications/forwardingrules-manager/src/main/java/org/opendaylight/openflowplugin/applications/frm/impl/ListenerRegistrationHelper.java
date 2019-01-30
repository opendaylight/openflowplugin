/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getInventoryConfigDataStoreStatus;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class ListenerRegistrationHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerRegistrationHelper.class);
    private static final long INVENTORY_CHECK_TIMER = 1;
    private final String operational = "OPERATIONAL";
    private final ListeningExecutorService listeningExecutorService;
    private final DataBroker dataBroker;

    @Inject
    public ListenerRegistrationHelper(@Reference final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor(
                 new ThreadFactoryBuilder()
                .setNameFormat("frm-listener" + "%d")
                .setDaemon(false)
                .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
                .build()));
    }

    public <T extends DataObject, L extends ClusteredDataTreeChangeListener<T>>
        ListenableFuture<ListenerRegistration<L>>
        checkedRegisterListener(DataTreeIdentifier<T> treeId, L listener) {
        return listeningExecutorService.submit(() -> {
            while (! getInventoryConfigDataStoreStatus().equals(operational)) {
                try {
                    LOG.debug("Retrying for datastore to become operational for listener {}", listener);
                    Thread.sleep(INVENTORY_CHECK_TIMER * 1000);
                } catch (InterruptedException e) {
                    LOG.info("registerDataTreeChangeListener thread is interrupted");
                    Thread.currentThread().interrupt();
                }
            }
            SimpleTaskRetryLooper looper = new SimpleTaskRetryLooper(ForwardingRulesManagerImpl.STARTUP_LOOP_TICK,
                    ForwardingRulesManagerImpl.STARTUP_LOOP_MAX_RETRIES);
            return looper.loopUntilNoException(() -> dataBroker.registerDataTreeChangeListener(treeId, listener));
        });
    }

    public void close() throws Exception {
        MoreExecutors.shutdownAndAwaitTermination(listeningExecutorService, 5, TimeUnit.SECONDS);
    }
}