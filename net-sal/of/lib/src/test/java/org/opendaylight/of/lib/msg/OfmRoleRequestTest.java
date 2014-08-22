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

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.ROLE_REQUEST;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmRoleRequest message.
 *
 * @author Pramod Shanbhag
 */
public class OfmRoleRequestTest extends OfmTest {

    // Test files
    private static final String TF_RR_13 = "v13/roleRequest";
    private static final String TF_RR_12 = "v12/roleRequest";

    private static final int M_LEN = MessageFactory.LIB_ROLE;
    private static final ControllerRole EXP_ROLE = ControllerRole.EQUAL;
    private static int EXP_GEN_ID = 1;

    // ========================================================= PARSING ====
    private void verifyRoleRequest(OfmRoleRequest msg,
                                   ControllerRole role, long id) {
        assertEquals(AM_NEQ, role, msg.getRole());
        assertEquals(AM_NEQ, id, msg.getGenerationId());
    }

    @Test
    public void roleRequest13() {
        print(EOL + "roleRequest13()");
        OfmRoleRequest msg = (OfmRoleRequest) verifyMsgHeader(TF_RR_13, V_1_3,
                              ROLE_REQUEST, M_LEN);
        verifyRoleRequest(msg, EXP_ROLE, EXP_GEN_ID);
    }

    @Test
    public void roleRequest12() {
        print(EOL + "roleRequest12()");
        verifyNotSupported(TF_RR_12);
    }

    // ROLE_REQUEST is not supported in 1.1, 1.0

    // ============================================= CREATING / ENCODING ====
    @Test
    public void encodeRoleRequest13() {
        print(EOL + "encodeRoleRequest13()");
        OfmMutableRoleRequest req = (OfmMutableRoleRequest)
                MessageFactory.create(V_1_3, ROLE_REQUEST, EXP_ROLE);
        req.clearXid();
        verifyMutableHeader(req, V_1_3, ROLE_REQUEST, 0);
        req.generationId(EXP_GEN_ID);
        encodeAndVerifyMessage(req.toImmutable(), TF_RR_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createRoleRequest12() {
        MessageFactory.create(V_1_2, ROLE_REQUEST, EXP_ROLE);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createRoleRequest11() {
        MessageFactory.create(V_1_1, ROLE_REQUEST, EXP_ROLE);
    }

    @Test(expected = VersionMismatchException.class)
    public void createRoleRequest10() {
        MessageFactory.create(V_1_0, ROLE_REQUEST, EXP_ROLE);
    }
}
