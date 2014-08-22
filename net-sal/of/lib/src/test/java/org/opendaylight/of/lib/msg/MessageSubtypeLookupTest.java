/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.err.ErrorType;
import org.opendaylight.of.lib.mp.MultipartType;

import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.msg.MessageSubtypeLookup.combo;
import static org.opendaylight.of.lib.msg.MessageType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MessageSubtypeLookup.
 *
 * @author Simon Hunt
 */
public class MessageSubtypeLookupTest extends AbstractTest {

    private static enum Foo { FOO }

    private void good(MessageType type, Class<? extends Enum<?>> subtype) {
        for (ProtocolVersion pv: PV_0123)
            for (Enum<?> st: subtype.getEnumConstants()) {
                try {
                    MessageSubtypeLookup.validate(pv, type, st);
                    print("Validated: {} {}", pv, combo(type, st));
                } catch (IllegalArgumentException e) {
                    print(e);
                    fail(AM_UNEX);
                }
            }
    }

    private void verifyBad(MessageType type, Class<? extends Enum<?>> subtype) {
        for (ProtocolVersion pv: PV_0123)
            for (Enum<?> st: subtype.getEnumConstants()) {
                try {
                    MessageSubtypeLookup.validate(pv, type, st);
                    fail(AM_NOEX);
                } catch (IllegalArgumentException e) {
                    print(FMT_EX, e);
                }
            }
    }

    private void bad(MessageType type) {
        // use some random enum class (with few constants)
        verifyBad(type, Foo.class);
    }

    @Test
    public void hello() {
        print(EOL + "hello()");
        bad(HELLO);
    }

    @Test
    public void error() {
        print(EOL + "error()");
        bad(ERROR);
        good(ERROR, ErrorType.class);
    }

    @Test
    public void echoRequest() {
        print(EOL + "echoRequest()");
        bad(ECHO_REQUEST);
    }

    @Test
    public void echoReply() {
        print(EOL + "echoReply()");
        bad(ECHO_REPLY);
    }

    @Test
    public void experimenter() {
        print(EOL + "experimenter()");
        bad(EXPERIMENTER);
        good(EXPERIMENTER, ExperimenterId.class);
    }

    @Test
    public void featuresRequest() {
        print(EOL + "featuresRequest()");
        bad(FEATURES_REQUEST);
    }

    @Test
    public void featuresReply() {
        print(EOL + "featuresReply()");
        bad(FEATURES_REPLY);
    }

    @Test
    public void getConfigRequest() {
        print(EOL + "getConfigRequest()");
        bad(GET_CONFIG_REQUEST);
    }

    @Test
    public void getConfigReply() {
        print(EOL + "getConfigReply()");
        bad(GET_CONFIG_REPLY);
    }

    @Test
    public void setConfig() {
        print(EOL + "setConfig()");
        bad(SET_CONFIG);
    }

    @Test
    public void packetIn() {
        print(EOL + "packetIn()");
        bad(PACKET_IN);
        good(PACKET_IN, PacketInReason.class);
    }

    @Test
    public void flowRemoved() {
        print(EOL + "flowRemoved()");
        bad(FLOW_REMOVED);
        good(FLOW_REMOVED, FlowRemovedReason.class);
    }

    @Test
    public void portStatus() {
        print(EOL + "portStatus()");
        bad(PORT_STATUS);
        good(PORT_STATUS, PortReason.class);
    }

    @Test
    public void packetOut() {
        print(EOL + "packetOut()");
        bad(PACKET_OUT);
    }

    @Test
    public void flowMod() {
        print(EOL + "flowMod()");
        bad(FLOW_MOD);
        good(FLOW_MOD, FlowModCommand.class);
    }

    @Test
    public void groupMod() {
        print(EOL + "groupMod()");
        bad(GROUP_MOD);
        good(GROUP_MOD, GroupModCommand.class);
    }

    @Test
    public void portMod() {
        print(EOL + "portMod()");
        bad(PORT_MOD);
    }

    @Test
    public void tableMod() {
        print(EOL + "tableMod()");
        bad(TABLE_MOD);
    }

    @Test
    public void multipartRequest() {
        print(EOL + "multipartRequest()");
        bad(MULTIPART_REQUEST);
        good(MULTIPART_REQUEST, MultipartType.class);
    }

    @Test
    public void multipartReply() {
        print(EOL + "multipartReply()");
        bad(MULTIPART_REPLY);
        good(MULTIPART_REPLY, MultipartType.class);
    }

    @Test
    public void barrierRequest() {
        print(EOL + "barrierRequest()");
        bad(BARRIER_REQUEST);
    }

    @Test
    public void barrierReply() {
        print(EOL + "barrierReply()");
        bad(BARRIER_REPLY);
    }

    @Test
    public void queueGetConfigRequest() {
        print(EOL + "queueGetConfigRequest()");
        bad(QUEUE_GET_CONFIG_REQUEST);
    }

    @Test
    public void queueGetConfigReply() {
        print(EOL + "queueGetConfigReply()");
        bad(QUEUE_GET_CONFIG_REPLY);
    }

    @Test
    public void roleRequest() {
        print(EOL + "roleRequest()");
        bad(ROLE_REQUEST);
        good(ROLE_REQUEST, ControllerRole.class);
    }

    @Test
    public void roleReply() {
        print(EOL + "roleReply()");
        bad(ROLE_REPLY);
        good(ROLE_REPLY, ControllerRole.class);
    }

    @Test
    public void getAsyncRequest() {
        print(EOL + "getAsyncRequest()");
        bad(GET_ASYNC_REQUEST);
    }

    @Test
    public void getAsyncReply() {
        print(EOL + "getAsyncReply()");
        bad(GET_ASYNC_REPLY);
    }

    @Test
    public void setAsync() {
        print(EOL + "setAsync()");
        bad(SET_ASYNC);
    }

    @Test
    public void meterMod() {
        print(EOL + "meterMod()");
        bad(METER_MOD);
        good(METER_MOD, MeterModCommand.class);
    }

}
