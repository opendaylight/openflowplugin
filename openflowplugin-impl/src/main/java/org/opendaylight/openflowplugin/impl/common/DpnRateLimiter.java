/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import com.google.common.util.concurrent.RateLimiter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;


public class DpnRateLimiter {
    private final boolean doRateLimit;
    private final AtomicReference<RateLimiter> rateLimiter;

    public DpnRateLimiter(OpenflowProviderConfig config) {
        int dpnRateLimitPerMinute = config.getDpnRateLimitPerMin();
        double rateLimiterSize;
        if (dpnRateLimitPerMinute == 0) {
            doRateLimit = false;
            rateLimiterSize = 1;
        } else {
            doRateLimit = true;
            rateLimiterSize = 1d / (60d / dpnRateLimitPerMinute);
        }
        rateLimiter = new AtomicReference<>(RateLimiter.create(rateLimiterSize));

    }

    public boolean tryAquire() {
        if (doRateLimit) {
            return rateLimiter.get().tryAcquire(0, TimeUnit.SECONDS);
        }
        return true;
    }
}