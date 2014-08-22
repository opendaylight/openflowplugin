/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.VersionNotSupportedException;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ExperimenterId.BUDAPEST_U;
import static org.opendaylight.of.lib.ExperimenterId.HP;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MessageType.EXPERIMENTER;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link OfmExperimenter} message.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfmExperimenterTest extends OfmTest {
    private static final String TF_ECEXPER_13 = "v13/experimenter";
    private static final String TF_ECEXPER_12 = "v12/experimenter";
    private static final String TF_ECEXPER_11 = "v11/experimenter";

    private static final String TF_ECVENDOR_10 = "v10/vendor";

    private static final String TF_ECUNREQ_ID_13 = "v13/experimenter2";

    private static final int EXP_TYPE_13 = 42;

    private static final byte [] EXP_DATA = {
            0x45, 0x78, 0x70, 0x65, 0x72, 0x69, 0x6d, 0x65, 0x6e,
            0x74, 0x61, 0x6c, 0, 0, 0, 0,
    };

    private static final byte [] EXP_DATA_10 = {
            0x56, 0x65, 0x6e, 0x64, 0x6f, 0x72,
            0x20, 0x53, 0x74, 0x75, 0x66, 0x66
    };

    private static final int UNRECOGNIZED_ID_INT = 0x4c8093;

    private MutableMessage mm;

    // ========================================================= PARSING ====

    @Test
    public void experimenter13() {
        print(EOL + "experimenter13()");
        OfmExperimenter msg = (OfmExperimenter)
                verifyMsgHeader(TF_ECEXPER_13, V_1_3, EXPERIMENTER, 32);
        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
        assertEquals(AM_NEQ, EXP_TYPE_13, msg.getExpType());
        assertEquals(AM_NEQ, HP, msg.getExpId());

        // verifyString
        // ByteUtils.getStringField(mm.getData());
    }

    @Test
    public void experimenter12() {
        print(EOL + "experimenter12()");
        verifyNotSupported(TF_ECEXPER_12);
//        OfmExperimenter msg = (OfmExperimenter)
//                verifyMsgHeader(TF_ECEXPER_12, V_1_2, EXPERIMENTER, 32);
//        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
//        assertEquals(AM_NEQ, EXP_TYPE_12, msg.getExpType());
//        assertEquals(AM_NEQ, ExperimenterId.NICIRA, msg.getExpId());
    }

    @Test
    public void experimenter11() {
        print(EOL + "experimenter11()");
        verifyNotSupported(TF_ECEXPER_11);
//        OfmExperimenter msg = (OfmExperimenter)
//              verifyMsgHeader(TF_ECEXPER_11, V_1_1, EXPERIMENTER, 32);
//        assertArrayEquals(AM_NEQ, EXP_DATA, msg.getData());
//        assertEquals(AM_NEQ, ExperimenterId.BIG_SWITCH, msg.getExpId());
    }

    @Test
    public void vendor10() {
        print(EOL + "vendor10()");
        OfmExperimenter msg = (OfmExperimenter)
                verifyMsgHeader(TF_ECVENDOR_10, V_1_0, EXPERIMENTER, 24);
        assertArrayEquals(AM_NEQ, EXP_DATA_10, msg.getData());
        assertEquals(AM_NEQ, BUDAPEST_U, msg.getExpId());
    }

    @Test
    public void decodeUnRecognizedId() {
        print(EOL + "decodeUnRecognizedId()");
        OfmExperimenter msg = (OfmExperimenter)
                verifyMsgHeader(TF_ECUNREQ_ID_13, V_1_3, EXPERIMENTER, 32);
        assertNull(msg.getExpId());
        assertEquals(AM_NEQ, UNRECOGNIZED_ID_INT, msg.getId());
    }

    // ============================================= CREATING / ENCODING ====

    @Test
    public void encodeExperimenter13() {
        print(EOL + "encodeExperimenter13()");
        mm = MessageFactory.create(V_1_3, EXPERIMENTER);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, EXPERIMENTER, 0);

        // add in the experimenter data
        OfmMutableExperimenter rep = (OfmMutableExperimenter) mm;
        rep.expId(HP).expType(EXP_TYPE_13).data(EXP_DATA);

        encodeAndVerifyMessage(mm.toImmutable(), TF_ECEXPER_13);
    }

    @Test
    public void encodeExperimenter13WithId() {
        print(EOL + "encodeExperimenter13WithId()");
        mm = MessageFactory.create(V_1_3, EXPERIMENTER, HP);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_3, EXPERIMENTER, 0);

        // add in the experimenter data
        OfmMutableExperimenter rep = (OfmMutableExperimenter) mm;
        rep.expType(EXP_TYPE_13).data(EXP_DATA);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECEXPER_13);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeExperimenter12() {
        mm = MessageFactory.create(V_1_2, EXPERIMENTER);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void encodeExperimenter11() {
        mm = MessageFactory.create(V_1_1, EXPERIMENTER);
    }

    @Test
    public void encodeVendor10() {
        print(EOL + "encodeVendor10()");
        mm = MessageFactory.create(V_1_0, EXPERIMENTER);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, EXPERIMENTER, 0);

        // add in the experimenter data
        OfmMutableExperimenter exp = (OfmMutableExperimenter) mm;
        exp.expId(BUDAPEST_U).data(EXP_DATA_10);

        encodeAndVerifyMessage(mm.toImmutable(), TF_ECVENDOR_10);
    }

    @Test
    public void encodeVendor10WithId() {
        print(EOL + "encodeVendor10WithId()");
        mm = MessageFactory.create(V_1_0, EXPERIMENTER, BUDAPEST_U);
        mm.clearXid();
        verifyMutableHeader(mm, V_1_0, EXPERIMENTER, 0);

        // add in the experimenter data
        OfmMutableExperimenter exp = (OfmMutableExperimenter) mm;
        exp.data(EXP_DATA_10);
        encodeAndVerifyMessage(mm.toImmutable(), TF_ECVENDOR_10);
    }

    @Test
    public void invalidSetType() {
        print(EOL + "invalidSetType()");
        int expType = 100;
        mm = MessageFactory.create(V_1_0, EXPERIMENTER);
        OfmMutableExperimenter exp = (OfmMutableExperimenter) mm;
        try {
            exp.expType(expType);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print("EX> " + e);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void createWithId() {
        print(EOL + "createWithId()");
        OfmMutableExperimenter m = (OfmMutableExperimenter)
                MessageFactory.create(V_1_3, EXPERIMENTER, ExperimenterId.HP);
        m.clearXid();
        verifyMutableHeader(m, V_1_3, EXPERIMENTER, 0);
        assertEquals(AM_NEQ, ExperimenterId.HP, m.getExpId());
    }

}
