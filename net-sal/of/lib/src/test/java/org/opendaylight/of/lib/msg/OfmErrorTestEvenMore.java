/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.err.ECodeBadInstruction;
import org.opendaylight.of.lib.err.ECodeBadMatch;
import org.opendaylight.of.lib.err.ErrorType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Tests more attributes of the OfmError class.
 *
 * @author Simon Hunt
 */
public class OfmErrorTestEvenMore extends OfmTest {

    private static final String DIR = "msg/v13/err/";

    private OfmError parseOfmError(String name) {
        String path = DIR + name + HEX;
        OfPacketReader pkt = getPacketReader(path);
        OpenflowMessage m = null;
        try {
            m = MessageFactory.parseMessage(pkt);
        } catch (MessageParseException e) {
            Assert.fail("unable to parse message");
        }
        assertTrue(AM_WRCL, m instanceof OfmError);
        return (OfmError) m;
    }

    @Test
    public void sampleBadMatch() {
        OfmError err = parseOfmError("sampleErrorBadMatch");
        print(err.toDebugString());
        assertEquals(AM_NEQ, MessageType.ERROR, err.getType());
        assertEquals(AM_NEQ, ErrorType.BAD_MATCH, err.getErrorType());
        assertEquals(AM_NEQ, ECodeBadMatch.BAD_FIELD, err.getErrorCode());
        assertEquals(AM_NEQ, "OFM-cause:[V_1_3,FLOW_MOD,104,482]",
                err.getErrorMessage());
        assertEquals(AM_NEQ, "{ofm:[V_1_3,ERROR,76,482],BAD_MATCH/BAD_FIELD," +
                "#dataBytes=64,OFM-cause:[V_1_3,FLOW_MOD,104,482]}",
                err.toString());
    }

    @Test
    public void sampleBadInstruction() {
        OfmError err = parseOfmError("sampleErrorBadInstruction");
        print(err.toDebugString());
        assertEquals(AM_NEQ, MessageType.ERROR, err.getType());
        assertEquals(AM_NEQ, ErrorType.BAD_INSTRUCTION, err.getErrorType());
        assertEquals(AM_NEQ, ECodeBadInstruction.UNSUP_INST, err.getErrorCode());
        assertEquals(AM_NEQ, "OFM-cause:[V_1_3,FLOW_MOD,104,483]",
                err.getErrorMessage());
        assertEquals(AM_NEQ, "{ofm:[V_1_3,ERROR,76,483],BAD_INSTRUCTION/" +
                "UNSUP_INST,#dataBytes=64,OFM-cause:[V_1_3,FLOW_MOD,104,483]}",
                err.toString());
    }
}
