package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.junit.Test;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.statistics.OFPMultipartTypes;

public class OFPMultipartTypeTest extends TestCase {
    @Test
    public void testMapping() throws Exception {
        TestCase.assertEquals(OFPMultipartTypes.DESC, OFPMultipartTypes.valueOf((short) 0, OFPType.MULTIPART_REQUEST));
        TestCase.assertEquals(OFPMultipartTypes.QUEUE, OFPMultipartTypes.valueOf((short) 5, OFPType.MULTIPART_REQUEST));
        TestCase.assertEquals(OFPMultipartTypes.EXPERIMENTER,
                OFPMultipartTypes.valueOf((short) 0xffff, OFPType.MULTIPART_REQUEST));
        TestCase.assertEquals(OFPMultipartTypes.EXPERIMENTER,
                OFPMultipartTypes.valueOf((short) 0xffff, OFPType.MULTIPART_REPLY));
        TestCase.assertEquals(OFPMultipartTypes.PORT_STATS,
                OFPMultipartTypes.valueOf((short) 4, OFPType.MULTIPART_REQUEST));
        TestCase.assertEquals(OFPMultipartTypes.PORT_DESC,
                OFPMultipartTypes.valueOf((short) 13, OFPType.MULTIPART_REQUEST));
    }
}
