/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.of.lib.msg.MessageType.decode;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MessageType enumeration.
 *
 * @author Simon Hunt
 */
public class MessageTypeTest extends AbstractTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (MessageType t: MessageType.values()) {
            print(t);
        }
        assertEquals(AM_UXCC, 30, MessageType.values().length);
    }


    private void verify(int code, MessageType expType, ProtocolVersion pv) {
        // if expected type is null, we are really expecting an exception
        try {
            MessageType mt = decode(code, pv);
            print(FMT_PV_CODE_ENUM, pv, code, mt);
            assertEquals("unexpected decode " + pv, expType, mt);
        } catch (DecodeException e) {
            fail("Unexpected Exception " + pv + " : " + e);
        }
    }

    private void verifyNa(int lower, int upper, ProtocolVersion pv) {
        for (int code=lower; code<upper; code++) {
            try {
                decode(code, pv);
                fail(AM_NOEX);
            } catch (DecodeException e) {
                print(FMT_EX, e);
            }
        }
    }

    private void verify10(int code, MessageType expType) {
        verify(code, expType, V_1_0);
    }

    private void verify11(int code, MessageType expType) {
        verify(code, expType, V_1_1);
    }

    private void verify12(int code, MessageType expType) {
        verify(code, expType, V_1_2);
    }

    private void verify13(int code, MessageType expType) {
        verify(code, expType, V_1_3);
    }


    @Test
    public void verifyVersion10Encodings() {
        print(EOL + "verifyVersion10Encodings()");
        verify10(0, HELLO);
        verify10(1, ERROR);
        verify10(2, ECHO_REQUEST);
        verify10(3, ECHO_REPLY);
        verify10(4, EXPERIMENTER);
        verify10(5, FEATURES_REQUEST);
        verify10(6, FEATURES_REPLY);
        verify10(7, GET_CONFIG_REQUEST);
        verify10(8, GET_CONFIG_REPLY);
        verify10(9, SET_CONFIG);
        verify10(10, PACKET_IN);
        verify10(11, FLOW_REMOVED);
        verify10(12, PORT_STATUS);
        verify10(13, PACKET_OUT);
        verify10(14, FLOW_MOD);
        verify10(15, PORT_MOD);
        verify10(16, MULTIPART_REQUEST); // NOTE: Replaces STATS_REQUEST
        verify10(17, MULTIPART_REPLY);   // NOTE: Replaces STATS_REPLY
        verify10(18, BARRIER_REQUEST);
        verify10(19, BARRIER_REPLY);
        verify10(20, QUEUE_GET_CONFIG_REQUEST);
        verify10(21, QUEUE_GET_CONFIG_REPLY);
        verifyNa(22, 32, V_1_0);
    }

    @Test
    public void verifyVersion11Encodings() {
        print(EOL + "verifyVersion11Encodings()");
        verify11(0, HELLO);
        verify11(1, ERROR);
        verify11(2, ECHO_REQUEST);
        verify11(3, ECHO_REPLY);
        verify11(4, EXPERIMENTER);
        verify11(5, FEATURES_REQUEST);
        verify11(6, FEATURES_REPLY);
        verify11(7, GET_CONFIG_REQUEST);
        verify11(8, GET_CONFIG_REPLY);
        verify11(9, SET_CONFIG);
        verify11(10, PACKET_IN);
        verify11(11, FLOW_REMOVED);
        verify11(12, PORT_STATUS);
        verify11(13, PACKET_OUT);
        verify11(14, FLOW_MOD);
        verify11(15, GROUP_MOD);
        verify11(16, PORT_MOD);
        verify11(17, TABLE_MOD);
        verify11(18, MULTIPART_REQUEST); // NOTE: Replaces STATS_REQUEST
        verify11(19, MULTIPART_REPLY);   // NOTE: Replaces STATS_REPLY
        verify11(20, BARRIER_REQUEST);
        verify11(21, BARRIER_REPLY);
        verify11(22, QUEUE_GET_CONFIG_REQUEST);
        verify11(23, QUEUE_GET_CONFIG_REPLY);
        verifyNa(24, 32, V_1_1);
    }

    @Test
    public void verifyVersion12Encodings() {
        print(EOL + "verifyVersion12Encodings()");
        verify12(0, HELLO);
        verify12(1, ERROR);
        verify12(2, ECHO_REQUEST);
        verify12(3, ECHO_REPLY);
        verify12(4, EXPERIMENTER);
        verify12(5, FEATURES_REQUEST);
        verify12(6, FEATURES_REPLY);
        verify12(7, GET_CONFIG_REQUEST);
        verify12(8, GET_CONFIG_REPLY);
        verify12(9, SET_CONFIG);
        verify12(10, PACKET_IN);
        verify12(11, FLOW_REMOVED);
        verify12(12, PORT_STATUS);
        verify12(13, PACKET_OUT);
        verify12(14, FLOW_MOD);
        verify12(15, GROUP_MOD);
        verify12(16, PORT_MOD);
        verify12(17, TABLE_MOD);
        verify12(18, MULTIPART_REQUEST); // NOTE: Replaces STATS_REQUEST
        verify12(19, MULTIPART_REPLY);   // NOTE: Replaces STATS_REPLY
        verify12(20, BARRIER_REQUEST);
        verify12(21, BARRIER_REPLY);
        verify12(22, QUEUE_GET_CONFIG_REQUEST);
        verify12(23, QUEUE_GET_CONFIG_REPLY);
        verify12(24, ROLE_REQUEST);
        verify12(25, ROLE_REPLY);
        verifyNa(26, 32, V_1_2);
    }

    @Test
    public void verifyVersion13Encodings() {
        print(EOL + "verifyVersion13Encodings()");
        verify13(0, HELLO);
        verify13(1, ERROR);
        verify13(2, ECHO_REQUEST);
        verify13(3, ECHO_REPLY);
        verify13(4, EXPERIMENTER);
        verify13(5, FEATURES_REQUEST);
        verify13(6, FEATURES_REPLY);
        verify13(7, GET_CONFIG_REQUEST);
        verify13(8, GET_CONFIG_REPLY);
        verify13(9, SET_CONFIG);
        verify13(10, PACKET_IN);
        verify13(11, FLOW_REMOVED);
        verify13(12, PORT_STATUS);
        verify13(13, PACKET_OUT);
        verify13(14, FLOW_MOD);
        verify13(15, GROUP_MOD);
        verify13(16, PORT_MOD);
        verify13(17, TABLE_MOD);
        verify13(18, MULTIPART_REQUEST);
        verify13(19, MULTIPART_REPLY);
        verify13(20, BARRIER_REQUEST);
        verify13(21, BARRIER_REPLY);
        verify13(22, QUEUE_GET_CONFIG_REQUEST);
        verify13(23, QUEUE_GET_CONFIG_REPLY);
        verify13(24, ROLE_REQUEST);
        verify13(25, ROLE_REPLY);
        verify13(26, GET_ASYNC_REQUEST);
        verify13(27, GET_ASYNC_REPLY);
        verify13(28, SET_ASYNC);
        verify13(29, METER_MOD);
        verifyNa(30, 32, V_1_3);
    }

}
