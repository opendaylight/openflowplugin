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
import org.opendaylight.of.lib.dt.TableId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.TABLE_MOD;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit test for the OfmTableMod message.
 *
 * @author Simon Hunt
 */
public class OfmTableModTest extends OfmTest {

    // test files
    private static final String TF_SC_13 = "v13/tableMod";
    private static final String TF_SC_12 = "v12/tableMod";
    private static final String TF_SC_11 = "v11/tableMod";

    private static final int MLEN = MessageFactory.LIB_TABLE_MOD;

    private static final TableId EXP_TID_13 = TableId.valueOf(3);
//    private static final TableId EXP_TID_12 = TableId.valueOf(129);
//    private static final TableId EXP_TID_11 = TableId.ALL;

    // note table config deprecated in 1.3 - value should be null
//    private static final TableConfig[] EXP_TCFG_12 = {TABLE_MISS_CONTROLLER};
//    private static final TableConfig[] EXP_TCFG_11 = {TABLE_MISS_DROP};

    private OfmTableMod msg;
    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void tableMod13() {
        print(EOL + "tableMod13");
        msg = (OfmTableMod) verifyMsgHeader(TF_SC_13, V_1_3, TABLE_MOD, MLEN);
        assertEquals(AM_NEQ, EXP_TID_13, msg.getTableId());
        assertNull(AM_HUH, msg.getConfig());
    }

    @Test
    public void tableMod12() {
        print(EOL + "tableMod12");
        verifyNotSupported(TF_SC_12);
//        msg = (OfmTableMod) verifyMsgHeader(TF_SC_12, V_1_2, TABLE_MOD, MLEN);
//        assertEquals(AM_NEQ, EXP_TID_12, msg.getTableId());
//        verifyFlags(msg.getConfig(), EXP_TCFG_12);
    }

    @Test
    public void tableMod11() {
        print(EOL + "tableMod11");
        verifyNotSupported(TF_SC_11);
//        msg = (OfmTableMod) verifyMsgHeader(TF_SC_11, V_1_1, TABLE_MOD, MLEN);
//        assertEquals(AM_NEQ, EXP_TID_11, msg.getTableId());
//        verifyFlags(msg.getConfig(), EXP_TCFG_11);
    }

    // NOTE: TABLE_MOD not supported in 1.0

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeTableMod13() {
        print(EOL + "encodeTableMod13()");
        mm = MessageFactory.create(V_1_3, TABLE_MOD);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, TABLE_MOD, 0);
        OfmMutableTableMod tm = (OfmMutableTableMod) mm;
        tm.tableId(EXP_TID_13);
        print(mm.toDebugString());
        encodeAndVerifyMessage(mm.toImmutable(), TF_SC_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeTableMod12() {
        mm = MessageFactory.create(V_1_2, TABLE_MOD);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeTableMod11() {
        mm = MessageFactory.create(V_1_1, TABLE_MOD);
    }

    // NOTE: TABLE_MOD not supported in 1.0

}
