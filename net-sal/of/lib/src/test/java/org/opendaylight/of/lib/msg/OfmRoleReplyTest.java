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
import static org.opendaylight.of.lib.msg.MessageType.ROLE_REPLY;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmRoleReply message.
 *
 * @author Pramod Shanbhag
 */
public class OfmRoleReplyTest extends OfmTest {

    // Test files
    private static final String TF_RR_13 = "v13/roleReply";
    private static final String TF_RR_12 = "v12/roleReply";

    private static final int M_LEN = MessageFactory.LIB_ROLE;
    private static final ControllerRole EXP_ROLE = ControllerRole.EQUAL;
    private static int EXP_GEN_ID = 1;

    // ========================================================= PARSING ====
    private void verifyRoleReply(OfmRoleReply msg,
                                 ControllerRole role, long id) {
        assertEquals(AM_NEQ, role, msg.getRole());
        assertEquals(AM_NEQ, id, msg.getGenerationId());
    }

    @Test
    public void roleReply13() {
        print(EOL + "roleReply13()");
        OfmRoleReply msg = (OfmRoleReply) verifyMsgHeader(TF_RR_13, V_1_3,
                           ROLE_REPLY, M_LEN);
        verifyRoleReply(msg, EXP_ROLE, EXP_GEN_ID);
    }

    @Test
    public void roleReply12() {
        print(EOL + "roleReply12()");
        verifyNotSupported(TF_RR_12);
    }

    // ROLE_REPLY is not supported in 1.1, 1.0

    // ============================================= CREATING / ENCODING ====
    @Test
    public void encodeRoleReply13() {
        print(EOL + "encodeRoleReply13()");
        OfmMutableRoleReply req = (OfmMutableRoleReply)
                MessageFactory.create(V_1_3, ROLE_REPLY, EXP_ROLE);
        req.clearXid();
        verifyMutableHeader(req, V_1_3, ROLE_REPLY, 0);
        req.generationId(EXP_GEN_ID);
        encodeAndVerifyMessage(req.toImmutable(), TF_RR_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createRoleReply12() {
        MessageFactory.create(V_1_2, ROLE_REPLY, EXP_ROLE);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void createRoleReply11() {
        MessageFactory.create(V_1_1, ROLE_REPLY, EXP_ROLE);
    }

    @Test(expected = VersionMismatchException.class)
    public void createRoleReply10() {
        MessageFactory.create(V_1_0, ROLE_REPLY, EXP_ROLE);
    }
}
