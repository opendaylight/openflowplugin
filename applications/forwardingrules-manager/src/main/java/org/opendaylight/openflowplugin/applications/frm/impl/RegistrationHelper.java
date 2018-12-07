/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import static org.opendaylight.openflowplugin.applications.frm.util.FrmUtil.getInventoryConfigDataStoreStatus;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.opendaylight.controller.md.sal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.openflowplugin.common.wait.SimpleTaskRetryLooper;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationHelper {
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationHelper.class);
    private final String executorPrefix = "FRMListener-";
    private final long inventoryCheckTimer = Long.getLong("openflow.inventorycheck.timer", 1);

    private volatile ExecutorService service = null;
    private final DataBroker dataBroker;

    public RegistrationHelper(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public <T extends DataObject, L extends ClusteredDataTreeChangeListener<T>> Future<ListenerRegistration<L>>
        checkedRegisterListener(DataTreeIdentifier<T> treeId, L listener) {
        return service.submit(() -> {
            while (! getInventoryConfigDataStoreStatus().equals("OPERATIONAL")) {
                try {
                    LOG.debug("Retrying for datastore to become operational for listener {}", listener);
                    Thread.sleep(inventoryCheckTimer * 1000);
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

    public void start() {
        service = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat(executorPrefix + "%d")
                .setDaemon(false)
                .setUncaughtExceptionHandler((thread, ex) -> LOG.error("Uncaught exception {}", thread, ex))
                .build());
    }

    public void close() throws Exception {
        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);
    }
}
