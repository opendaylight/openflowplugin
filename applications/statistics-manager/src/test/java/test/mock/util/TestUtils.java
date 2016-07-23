/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TestUtils {

    private static AtomicLong transId = new AtomicLong();

    private static Random rnd = new Random();

    public static long nextLong(long RangeBottom, long rangeTop) {
        return RangeBottom + ((long)(rnd.nextDouble()*(rangeTop - RangeBottom)));
    }

    public static long getNewTransactionId() {
        return transId.incrementAndGet();
    }
}
