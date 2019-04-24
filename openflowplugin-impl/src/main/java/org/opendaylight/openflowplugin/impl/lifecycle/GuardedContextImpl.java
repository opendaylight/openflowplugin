/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import static com.google.common.util.concurrent.Service.State.FAILED;
import static com.google.common.util.concurrent.Service.State.NEW;
import static com.google.common.util.concurrent.Service.State.RUNNING;
import static com.google.common.util.concurrent.Service.State.STARTING;
import static com.google.common.util.concurrent.Service.State.STOPPING;
import static com.google.common.util.concurrent.Service.State.TERMINATED;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Monitor;
import com.google.common.util.concurrent.Monitor.Guard;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.GuardedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuardedContextImpl implements GuardedContext {
    private static final Logger LOG = LoggerFactory.getLogger(GuardedContextImpl.class);

    private final Monitor monitor = new Monitor();
    private final OFPContext delegate;
    private Service.State state = NEW;

    private final Guard isStartable = new Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return state == NEW || state == TERMINATED;
        }
    };

    private final Guard isStoppable = new Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return state.compareTo(RUNNING) <= 0;
        }
    };

    private final Guard isCloseable = new Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return state != FAILED;
        }
    };

    GuardedContextImpl(final OFPContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public Service.State state() {
        monitor.enter();
        final Service.State stateSnapshot = state;
        monitor.leave();
        return stateSnapshot;
    }

    @Override
    public <T> T map(final Function<OFPContext, T> transformer) {
        return transformer.apply(delegate);
    }

    @Override
    public void instantiateServiceInstance() {
        if (monitor.enterIf(isStartable)) {
            try {
                LOG.info("Starting {} service for node {}", this, getDeviceInfo());
                state = STARTING;
                delegate.instantiateServiceInstance();
                state = RUNNING;
            } finally {
                monitor.leave();
            }
        } else {
            throw new IllegalStateException("Service " + this + " has already been started");
        }
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public ListenableFuture<?> closeServiceInstance() {
        ListenableFuture<?> result = Futures.immediateFuture(null);

        if (monitor.enterIf(isStoppable)) {
            try {
                LOG.info("Stopping {} service for node {}", this, getDeviceInfo());
                state = STOPPING;
                final ListenableFuture<?> resultFuture = delegate.closeServiceInstance();

                Futures.addCallback(resultFuture, new FutureCallback<Object>() {
                    @Override
                    public void onSuccess(final Object result) {
                        state = TERMINATED;
                    }

                    @Override
                    public void onFailure(final Throwable throwable) {
                        state = TERMINATED;
                    }
                }, MoreExecutors.directExecutor());

                result = resultFuture;
            } catch (final Exception e) {
                result = Futures.immediateFailedFuture(e);
            } finally {
                monitor.leave();
            }
        }

        state = TERMINATED;
        return result;
    }

    @NonNull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public String toString() {
        return delegate.getClass().getSimpleName() + "[" + state + "]";
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return delegate.getDeviceInfo();
    }

    @Override
    public void registerMastershipWatcher(@NonNull final ContextChainMastershipWatcher contextChainMastershipWatcher) {
        delegate.registerMastershipWatcher(contextChainMastershipWatcher);
    }

    @Override
    public void close() {
        if (monitor.enterIf(isCloseable)) {
            try {
                LOG.info("Terminating {} service for node {}", this, getDeviceInfo());
                state = FAILED;
                delegate.close();
            } finally {
                monitor.leave();
            }
        }
    }
}
