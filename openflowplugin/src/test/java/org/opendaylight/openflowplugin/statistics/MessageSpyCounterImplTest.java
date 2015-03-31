/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.statistics;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/10/14.
 */
public class MessageSpyCounterImplTest {

    private static final MessageSpyCounterImpl messageSpyCounter = new MessageSpyCounterImpl();
    private static final int EXPECTED_MSG_COUNT = 10;

    @Test
    public void testDumpMessageCounts() {
        DataContainer msg = new MockDataContainer();
        List<String> messageCounts = messageSpyCounter.dumpMessageCounts();
        assertEquals(EXPECTED_MSG_COUNT,messageCounts.size());

        assertEquals("FROM_SWITCH_ENQUEUED: no activity detected", messageCounts.get(0));
        messageSpyCounter.spyMessage(msg, MessageSpy.STATISTIC_GROUP.FROM_SWITCH_ENQUEUED);
        messageCounts = messageSpyCounter.dumpMessageCounts();
        assertEquals("FROM_SWITCH_ENQUEUED: MSG[Action] -> +1 | 1", messageCounts.get(0));

    }


    private static class MockDataContainer implements DataContainer {

        @Override
        public Class<? extends DataContainer> getImplementedInterface() {
            return Action.class;
        }
    }
}
