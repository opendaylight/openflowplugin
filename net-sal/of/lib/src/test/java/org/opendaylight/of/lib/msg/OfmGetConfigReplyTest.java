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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.ConfigFlag.FRAG_NORMAL;
import static org.opendaylight.of.lib.msg.ConfigFlag.FRAG_REASM;
import static org.opendaylight.of.lib.msg.MessageType.GET_CONFIG_REPLY;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmGetConfigReply message.
 *
 * @author Simon Hunt
 */
public class OfmGetConfigReplyTest extends OfmTest {

    // test files
    private static final String TF_GCR_10 = "v10/getConfigReply";
    private static final String TF_GCR_11 = "v11/getConfigReply";
    private static final String TF_GCR_12 = "v12/getConfigReply";
    private static final String TF_GCR_13 = "v13/getConfigReply";

    private static final int MLEN = MessageFactory.LIB_SWITCH_CONFIG;

    private static final ConfigFlag[] EXP_FLAGS_13 = {FRAG_REASM};
    private static final Set<ConfigFlag> EXP_FLAGS_13_SET =
            new HashSet<ConfigFlag>(Arrays.asList(EXP_FLAGS_13));
//    private static final ConfigFlag[] EXP_FLAGS_12 = {FRAG_REASM, INV_TTL_TO_CTRLR};
//    private static final Set<ConfigFlag> EXP_FLAGS_12_SET =
//            new HashSet<ConfigFlag>(Arrays.asList(EXP_FLAGS_12));
//    private static final ConfigFlag[] EXP_FLAGS_11 = {FRAG_DROP};
//    private static final Set<ConfigFlag> EXP_FLAGS_11_SET =
//            new HashSet<ConfigFlag>(Arrays.asList(EXP_FLAGS_11));
    private static final ConfigFlag[] EXP_FLAGS_10 = {FRAG_NORMAL};
    private static final Set<ConfigFlag> EXP_FLAGS_10_SET =
            new HashSet<ConfigFlag>(Arrays.asList(EXP_FLAGS_10));

    private static final int EXP_MSL_13 = 64;
//    private static final int EXP_MSL_12 = 126;
//    private static final int EXP_MSL_11 = 127;
    private static final int EXP_MSL_10 = 128;


    private OfmGetConfigReply msg;
    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void getConfigReply13() {
        print(EOL + "getConfigReply13()");
        msg = (OfmGetConfigReply)
                verifyMsgHeader(TF_GCR_13, V_1_3, GET_CONFIG_REPLY, MLEN);
        verifyFlags(msg.getFlags(), EXP_FLAGS_13);
        assertEquals(AM_NEQ, EXP_MSL_13, msg.getMissSendLength());
    }

    @Test
    public void getConfigReply12() {
        print(EOL + "getConfigReply12()");
        verifyNotSupported(TF_GCR_12);
//        msg = (OfmGetConfigReply)
//                verifyMsgHeader(TF_GCR_12, V_1_2, GET_CONFIG_REPLY, MLEN);
//        verifyFlags(msg.getFlags(), EXP_FLAGS_12);
//        assertEquals(AM_NEQ, EXP_MSL_12, msg.getMissSendLength());
    }

    @Test
    public void getConfigReply11() {
        print(EOL + "getConfigReply11()");
        verifyNotSupported(TF_GCR_11);
//        msg = (OfmGetConfigReply)
//                verifyMsgHeader(TF_GCR_11, V_1_1, GET_CONFIG_REPLY, MLEN);
//        verifyFlags(msg.getFlags(), EXP_FLAGS_11);
//        assertEquals(AM_NEQ, EXP_MSL_11, msg.getMissSendLength());
    }

    @Test
    public void getConfigReply10() {
        print(EOL + "getConfigReply10()");
        msg = (OfmGetConfigReply)
                verifyMsgHeader(TF_GCR_10, V_1_0, GET_CONFIG_REPLY, MLEN);
        verifyFlags(msg.getFlags(), EXP_FLAGS_10);
        assertEquals(AM_NEQ, EXP_MSL_10, msg.getMissSendLength());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeGetConfigReply13() {
        print(EOL + "encodeGetConfigReply13()");
        mm = MessageFactory.create(V_1_3, GET_CONFIG_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, GET_CONFIG_REPLY, 0);
        OfmMutableGetConfigReply gcr = (OfmMutableGetConfigReply) mm;
        gcr.configFlags(EXP_FLAGS_13_SET).missSendLength(EXP_MSL_13);
        encodeAndVerifyMessage(mm.toImmutable(), TF_GCR_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeGetConfigReply12() {
        mm = MessageFactory.create(V_1_2, GET_CONFIG_REPLY);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeGetConfigReply11() {
        mm = MessageFactory.create(V_1_1, GET_CONFIG_REPLY);
    }

    @Test
    public void encodeGetConfigReply10() {
        print(EOL + "encodeGetConfigReply10()");
        mm = MessageFactory.create(V_1_0, GET_CONFIG_REPLY);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, GET_CONFIG_REPLY, 0);
        OfmMutableGetConfigReply gcr = (OfmMutableGetConfigReply) mm;
        gcr.configFlags(EXP_FLAGS_10_SET).missSendLength(EXP_MSL_10);
        encodeAndVerifyMessage(mm.toImmutable(), TF_GCR_10);
    }
}
