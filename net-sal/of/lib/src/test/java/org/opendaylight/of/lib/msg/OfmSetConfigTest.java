/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.of.lib.VersionNotSupportedException;

import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.ConfigFlag.FRAG_NORMAL;
import static org.opendaylight.of.lib.msg.ConfigFlag.FRAG_REASM;
import static org.opendaylight.of.lib.msg.MessageType.SET_CONFIG;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmSetConfig message.
 *
 * @author Simon Hunt
 */
public class OfmSetConfigTest extends OfmTest {

    // test files
    private static final String TF_SC_10 = "v10/setConfig";
    private static final String TF_SC_11 = "v11/setConfig";
    private static final String TF_SC_12 = "v12/setConfig";
    private static final String TF_SC_13 = "v13/setConfig";

    private static final int MLEN = MessageFactory.LIB_SWITCH_CONFIG;

    private static final ConfigFlag[] EXP_FLAGS_13 = {FRAG_REASM};
//    private static final ConfigFlag[] EXP_FLAGS_12 =
//            {FRAG_REASM, INV_TTL_TO_CTRLR};
//    private static final ConfigFlag[] EXP_FLAGS_11 = {FRAG_DROP};
    private static final ConfigFlag[] EXP_FLAGS_10 = {FRAG_NORMAL};

    private static final int EXP_MSL_13 = 64;
//    private static final int EXP_MSL_12 = 126;
//    private static final int EXP_MSL_11 = 127;
    private static final int EXP_MSL_10 = 128;


    private OfmSetConfig msg;
    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void setConfig13() {
        print(EOL + "setConfig13()");
        msg = (OfmSetConfig) verifyMsgHeader(TF_SC_13, V_1_3, SET_CONFIG, MLEN);
        verifyFlags(msg.getFlags(), EXP_FLAGS_13);
        Assert.assertEquals(AM_NEQ, EXP_MSL_13, msg.getMissSendLength());
    }

    @Test
    public void setConfig12() {
        print(EOL + "setConfig12()");
        verifyNotSupported(TF_SC_12);
//        msg = (OfmSetConfig) verifyMsgHeader(TF_SC_12, V_1_2, SET_CONFIG, MLEN);
//        verifyFlags(msg.getFlags(), EXP_FLAGS_12);
//        Assert.assertEquals(AM_NEQ, EXP_MSL_12, msg.getMissSendLength());
    }

    @Test
    public void setConfig11() {
        print(EOL + "setConfig11()");
        verifyNotSupported(TF_SC_11);
//        msg = (OfmSetConfig) verifyMsgHeader(TF_SC_11, V_1_1, SET_CONFIG, MLEN);
//        verifyFlags(msg.getFlags(), EXP_FLAGS_11);
//        Assert.assertEquals(AM_NEQ, EXP_MSL_11, msg.getMissSendLength());
    }

    @Test
    public void setConfig10() {
        print(EOL + "setConfig10()");
        msg = (OfmSetConfig) verifyMsgHeader(TF_SC_10, V_1_0, SET_CONFIG, MLEN);
        verifyFlags(msg.getFlags(), EXP_FLAGS_10);
        Assert.assertEquals(AM_NEQ, EXP_MSL_10, msg.getMissSendLength());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeSetConfig13() {
        print(EOL + "encodeSetConfig13()");
        mm = MessageFactory.create(V_1_3, SET_CONFIG);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, SET_CONFIG, 0);
        OfmMutableSetConfig sc = (OfmMutableSetConfig) mm;
        sc.setConfigFlags(new TreeSet<ConfigFlag>(asList(EXP_FLAGS_13)));
        sc.setMissSendLength(EXP_MSL_13);
        print(mm.toDebugString());
        encodeAndVerifyMessage(mm.toImmutable(), TF_SC_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeSetConfig12() {
        mm = MessageFactory.create(V_1_2, SET_CONFIG);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeSetConfig11() {
        mm = MessageFactory.create(V_1_1, SET_CONFIG);
    }

    @Test
    public void encodeSetConfig10() {
        print(EOL + "encodeSetConfig10()");
        mm = MessageFactory.create(V_1_0, SET_CONFIG);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, SET_CONFIG, 0);
        OfmMutableSetConfig sc = (OfmMutableSetConfig) mm;
        sc.setConfigFlags(new TreeSet<ConfigFlag>(asList(EXP_FLAGS_10)));
        sc.setMissSendLength(EXP_MSL_10);
        print(mm.toDebugString());
        encodeAndVerifyMessage(mm.toImmutable(), TF_SC_10);
    }
}
