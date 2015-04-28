/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.common.wait;

import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mirehak on 4/28/15.
 */
public class SimpleTaskRetryLooperTest {

    private static Logger LOG = LoggerFactory.getLogger(SimpleTaskRetryLooperTest.class);

    @Test
    public void testLoopUntilNoException() throws Exception {
        // stubborn service invoker
        final int retryCountLimit = 5;

        String output;
        SimpleTaskRetryLooper looper4 = new SimpleTaskRetryLooper(100, retryCountLimit - 1);
        try {
            output = looper4.loopUntilNoException(createStubbornService(retryCountLimit));
            Assert.fail("looper should have thrown exception");
        } catch (Exception e) {
            // expected
            LOG.debug("looper 4 failed - expected");
        }

        SimpleTaskRetryLooper looper5 = new SimpleTaskRetryLooper(100, retryCountLimit);
        output = looper5.loopUntilNoException(createStubbornService(retryCountLimit));
        Assert.assertNotNull(output);
    }

    private Callable<String> createStubbornService(final int retryCountToSucceed) {
        return new Callable<String>() {
            private int counter = 0;

            @Override
            public String call() throws Exception {
                counter++;
                if (counter < retryCountToSucceed) {
                    throw new IllegalStateException("service is not ready");
                } else {
                    LOG.info("service reached");
                    return "service is available";
                }
            }
        };
    }
}