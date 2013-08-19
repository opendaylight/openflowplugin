package org.openflow.codec.protocol;

import org.junit.Test;
import org.openflow.codec.protocol.action.OFPActionType;

import junit.framework.TestCase;

public class OFPPActionTypeTest extends TestCase {
    @Test
    public void testMapping() throws Exception {
        TestCase.assertEquals(OFPActionType.OUTPUT, OFPActionType.valueOf((short) 0));
        TestCase.assertEquals(OFPActionType.COPY_TTL_OUT, OFPActionType.valueOf((short) 11));
        TestCase.assertEquals(OFPActionType.EXPERIMENTER, OFPActionType.valueOf((short) 0xffff));
    }
}
