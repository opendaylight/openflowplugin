/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.PollableQueuesZipper;

import com.google.common.collect.Lists;

/**
 * test for {@link PollableQueuesZipper}
 */
public class PollableQueuesZipperTest {

    /**
     * Test method for {@link org.opendaylight.openflowplugin.api.openflow.md.util.PollableQueuesZipper#poll()}.
     */
    @Test
    public void testPoll() {
        Queue<String> l1 = new LinkedBlockingQueue<String>(Lists.newArrayList("1", "2", "3"));
        Queue<String> l2 = new LinkedBlockingQueue<String>(Lists.newArrayList("a", "b", "c", "d"));
        Queue<String> l3 = new LinkedBlockingQueue<String>(Lists.newArrayList("A", "B"));

        PollableQueuesZipper<String> zipper = new PollableQueuesZipper<>();
        zipper.addSource(l1);
        zipper.addSource(l2);
        zipper.addSource(l3);

        String[] expected = new String[] {
                "1", "a", "A", "2", "b", "B", "3", "c", "d", null, "XXX"
        };
        List<String> result = new ArrayList<>();
        while (true) {
            String data = zipper.poll();
            result.add(data);
            if (data == null) {
                break;
            }
        }
        l1.offer("XXX");
        result.add(zipper.poll());
        Assert.assertArrayEquals(expected, result.toArray());
    }

}
