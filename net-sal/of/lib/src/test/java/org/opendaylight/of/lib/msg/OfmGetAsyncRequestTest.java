/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.VersionNotSupportedException;

import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.GET_ASYNC_REQUEST;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;

/**
 * Unit tests for the {@link OfmGetAsyncRequest} message.
 *
 * @author Scott Simes
 */
public class OfmGetAsyncRequestTest extends OfmTest {

    // test files
    private static final String TF_REQ_13 = "v13/getAsyncRequest";

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void getAsyncRequest13() {
        print(EOL + "getAsyncConfigRequest13()");
        verifyMsgHeader(TF_REQ_13, V_1_3, GET_ASYNC_REQUEST, 8);
    }

    // NOTE: GET_ASYNC_REQUEST not supported in 1.0, 1,1 or 1.2

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeAsyncRequest13() {
        print(EOL + "encodeAsyncRequest13()");
        mm = MessageFactory.create(V_1_3, GET_ASYNC_REQUEST);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, GET_ASYNC_REQUEST, 0);
        encodeAndVerifyMessage(mm.toImmutable(), TF_REQ_13);
    }

    // NOTE: GET_ASYNC_REQUEST not supported in 1.0, 1,1 or 1.2

    @Test(expected = VersionNotSupportedException.class)
    public void encodeAsyncRequest12() {
        mm = MessageFactory.create(V_1_2, GET_ASYNC_REQUEST);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeAsyncRequest11() {
        mm = MessageFactory.create(V_1_1, GET_ASYNC_REQUEST);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeAsyncRequest10() {
        mm = MessageFactory.create(V_1_0, GET_ASYNC_REQUEST);
    }
}
