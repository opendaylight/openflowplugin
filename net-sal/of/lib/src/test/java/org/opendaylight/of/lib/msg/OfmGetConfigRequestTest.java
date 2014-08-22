/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionNotSupportedException;

import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.GET_CONFIG_REQUEST;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit test for the OfmGetConfigRequest message.
 *
 * @author Simon Hunt
 */
public class OfmGetConfigRequestTest extends OfmTest {

    private static final String TF_GCR_10 = "v10/getConfigRequest";
    private static final String TF_GCR_11 = "v11/getConfigRequest";
    private static final String TF_GCR_12 = "v12/getConfigRequest";
    private static final String TF_GCR_13 = "v13/getConfigRequest";

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void getConfigRequest13() {
        print(EOL + "getConfigRequest13()");
        verifyMsgHeader(TF_GCR_13, V_1_3, GET_CONFIG_REQUEST, 8);
    }

    @Test
    public void getConfigRequest12() {
        print(EOL + "getConfigRequest12()");
        verifyNotSupported(TF_GCR_12);
//        verifyMsgHeader(TF_GCR_12, V_1_2, GET_CONFIG_REQUEST, 8);
    }

    @Test
    public void getConfigRequest11() {
        print(EOL + "getConfigRequest11()");
        verifyNotSupported(TF_GCR_11);
//        verifyMsgHeader(TF_GCR_11, V_1_1, GET_CONFIG_REQUEST, 8);
    }

    @Test
    public void getConfigRequest10() {
        print(EOL + "getConfigRequest10()");
        verifyMsgHeader(TF_GCR_10, V_1_0, GET_CONFIG_REQUEST, 8);
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeConfigRequest13() {
        print(EOL + "encodeConfigRequest13()");
        mm = MessageFactory.create(V_1_3, GET_CONFIG_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, GET_CONFIG_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_GCR_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeConfigRequest12() {
        mm = MessageFactory.create(V_1_2, GET_CONFIG_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeConfigRequest11() {
        mm = MessageFactory.create(V_1_1, GET_CONFIG_REQUEST);
    }

    @Test
    public void encodeConfigRequest10() {
        print(EOL + "encodeConfigRequest10()");
        mm = MessageFactory.create(V_1_0, GET_CONFIG_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, GET_CONFIG_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_GCR_10);
    }
}
