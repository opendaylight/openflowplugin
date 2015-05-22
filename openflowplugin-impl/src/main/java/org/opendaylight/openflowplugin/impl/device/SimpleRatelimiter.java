/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.GuardedBy;

abstract class SimpleRatelimiter {
    private final AtomicInteger counter = new AtomicInteger();
    private final int lowWatermark;
    private final int highWatermark;
    @GuardedBy("counter")
    private volatile boolean limited;

    SimpleRatelimiter(final int lowWatermark, final int highWatermark) {
        Preconditions.checkArgument(lowWatermark >= 0);
        Preconditions.checkArgument(highWatermark >= 0);
        Preconditions.checkArgument(lowWatermark <= highWatermark);

        this.lowWatermark = lowWatermark;
        this.highWatermark = highWatermark;
    }

    protected final boolean isLimited() {
        return limited;
    }

    protected abstract void disableFlow();
    protected abstract void enableFlow();

    boolean acquirePermit() {
        final int cnt = counter.incrementAndGet();
        if (cnt > highWatermark) {
            synchronized (counter) {
                final int recheck = counter.decrementAndGet();
                if (recheck >= highWatermark && !limited) {
                    disableFlow();
                    limited = true;
                }
            }
            return false;
        }

        return true;
    }

    void releasePermit() {
        final int cnt = counter.decrementAndGet();
        if (cnt <= lowWatermark) {
            synchronized (counter) {
                final int recheck = counter.get();
                if (recheck <= lowWatermark && limited) {
                    enableFlow();
                    limited = false;
                }
            }
        }
    }
}
