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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;

public class ContextLifecycleGuard implements OFPContext {
    static ContextLifecycleGuard guard(final OFPContext delegate) {
        return new ContextLifecycleGuard(delegate);
    }

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

    private ContextLifecycleGuard(final OFPContext delegate) {
        this.delegate = delegate;
    }

    public Service.State state() {
        monitor.enter();
        final Service.State stateSnapshot = state;
        monitor.leave();
        return stateSnapshot;
    }

    @Override
    public void instantiateServiceInstance() {
        if (monitor.enterIf(isStartable)) {
            try {
                state = STARTING;
                delegate.instantiateServiceInstance();
                state = RUNNING;
            } finally {
                monitor.leave();
            }
        } else {
            throw new RuntimeException("Service " + this + " has already been started");
        }
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        if (monitor.enterIf(isStoppable)) {
            try {
                state = STOPPING;
                final ListenableFuture<Void> resultFuture = delegate.closeServiceInstance();

                Futures.addCallback(resultFuture, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(@Nullable final Void result) {
                        state = TERMINATED;
                    }

                    @Override
                    public void onFailure(@Nonnull final Throwable t) {
                        state = FAILED;
                    }
                }, MoreExecutors.directExecutor());

                return resultFuture;
            } finally {
                monitor.leave();
            }
        }

        state = TERMINATED;
        return Futures.immediateFuture(null);
    }

    @Nonnull
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
    public void registerMastershipWatcher(@Nonnull final ContextChainMastershipWatcher contextChainMastershipWatcher) {
        delegate.registerMastershipWatcher(contextChainMastershipWatcher);
    }

    @Override
    public void onStateAcquired(final ContextChainState state) {
        delegate.onStateAcquired(state);
    }

    @Override
    public void close() {
        if (monitor.enterIf(isStoppable)) {
            try {
                state = STOPPING;
                delegate.close();
                state = TERMINATED;
            } finally {
                monitor.leave();
            }
        }
    }
}