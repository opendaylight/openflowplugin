/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;

import java.util.Random;

public class QueueMockGenerator {
    private static final Random rnd = new Random();
    private static final QueueBuilder queueBuilder = new QueueBuilder();

    public static Queue getRandomQueue() {
        queueBuilder.setKey(new QueueKey(new QueueId(TestUtils.nextLong(0, 4294967295L))));
        queueBuilder.setPort(TestUtils.nextLong(0, 4294967295L));
        queueBuilder.setProperty(rnd.nextInt(65535));
        return queueBuilder.build();
    }

    public static Queue getRandomQueueWithPortNum(long portNum) {
        queueBuilder.setKey(new QueueKey(new QueueId(TestUtils.nextLong(0, 4294967295L))));
        queueBuilder.setPort(portNum);
        queueBuilder.setProperty(rnd.nextInt(65535));
        return queueBuilder.build();
    }
}
