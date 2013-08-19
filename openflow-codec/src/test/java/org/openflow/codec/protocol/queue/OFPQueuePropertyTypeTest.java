package org.openflow.codec.protocol.queue;

import junit.framework.TestCase;

import org.junit.Test;
import org.openflow.codec.protocol.queue.OFPQueuePropertyType;

public class OFPQueuePropertyTypeTest extends TestCase {
    @Test
    public void testMapping() throws Exception {
        TestCase.assertEquals(OFPQueuePropertyType.MIN_RATE, OFPQueuePropertyType.valueOf((short) 1));
        TestCase.assertEquals(OFPQueuePropertyType.MAX_RATE, OFPQueuePropertyType.valueOf((short) 2));
        TestCase.assertEquals(OFPQueuePropertyType.EXPERIMENTER, OFPQueuePropertyType.valueOf((short) 0xffff));

    }
}
