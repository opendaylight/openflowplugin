/*
 * Copyright (c) 2020 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frm.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getInventoryConfigDataStoreStatus;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = ListenerRegistrationHelper.class)
public final class ListenerRegistrationHelperImpl implements AutoCloseable, ListenerRegistrationHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerRegistrationHelperImpl.class);
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
        .setNameFormat("frm-listener" + "%d")
        .setDaemon(false)
        .setUncaughtExceptionHandler((thread, ex) -> {
            LOG.error("Uncaught exception in {}", thread, ex);
            Throwables.throwIfInstanceOf(ex, Error.class);
        })
        .build();

    // FIXME: make this tuneable
    private static final long INVENTORY_CHECK_TIMER = 1;

    private final ExecutorService executorService;
    private final DataBroker dataBroker;

    @Inject
    @Activate
    public ListenerRegistrationHelperImpl(@Reference final DataBroker dataBroker) {
        this.dataBroker = requireNonNull(dataBroker);
        executorService = Executors.newSingleThreadExecutor(THREAD_FACTORY);
    }

    @Override
    public <T extends DataObject> ListenableFuture<ObjectRegistration<?>> checkedRegisterListener(
            final DataTreeIdentifier<T> treeId, final ClusteredDataTreeChangeListener<T> listener) {
        return Futures.submit(() -> {
            while (!getInventoryConfigDataStoreStatus().equals("OPERATIONAL")) {
                try {
                    LOG.debug("Retrying for datastore to become operational for listener {}", listener);
                    Thread.sleep(INVENTORY_CHECK_TIMER * 1000);
                } catch (InterruptedException e) {
                    LOG.info("registerDataTreeChangeListener thread is interrupted");
                    Thread.currentThread().interrupt();
                }
            }
            return dataBroker.registerDataTreeChangeListener(treeId, listener);
        }, executorService);
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        MoreExecutors.shutdownAndAwaitTermination(executorService, 5, TimeUnit.SECONDS);
    }
}
