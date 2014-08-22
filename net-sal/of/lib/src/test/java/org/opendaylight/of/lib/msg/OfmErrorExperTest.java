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
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.err.ErrorType;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.MessageType.ERROR;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link OfmErrorExper} message.
 *
 * @author Pramod Shanbhag
 */
public class OfmErrorExperTest extends OfmTest{

    // test files
    private static final String TF_EREXP_13 = "v13/errorExperimenter";
    private static final String TF_EREXP_12 = "v12/errorExperimenter";

    // Variable length data is expected to be 64.
    private static final int EXP_DATA_LEN = 64;
    private static final int EXP_LEN = 80;
    private static final int EXP_TYPE = 3;

    private static final byte[] FAKE_DATA = new byte[64];

    static {
        for (int i=0; i<FAKE_DATA.length; i++)
            FAKE_DATA[i] = 1;
        FAKE_DATA[62] = 0x77;
    }

    // ========================================================= PARSING ====

    private void verifyErrorExperimenter(String file, ProtocolVersion pv) {

        OfmErrorExper msg = (OfmErrorExper)
                verifyMsgHeader(file, pv, ERROR, EXP_LEN);

        assertEquals(AM_NEQ, ErrorType.EXPERIMENTER, msg.getErrorType());
        assertEquals(AM_NEQ, EXP_TYPE, msg.getExpType());
        assertEquals(AM_NEQ, ExperimenterId.NICIRA, msg.getExpId());
        assertEquals(AM_NEQ, EXP_DATA_LEN, msg.getData().length);
        byte[] data = msg.getData();
        for (int i=0; i<data.length; i++)
            assertEquals(AM_NEQ, i==62 ? 0x77 : 0x01, data[i]);
    }

    @Test
    public void errorExperimenter12() {
        print(EOL + "errorExperimenter12()");
        verifyErrorExperimenter(TF_EREXP_12, V_1_2);
    }

    @Test
    public void errorExperimenter13() {
        print(EOL + "errorExperimenter13()");
        verifyErrorExperimenter(TF_EREXP_13, V_1_3);
    }

    // ============================================= CREATING / ENCODING ====

    private void verifyEncodingErrorExperimenter(String file,
            ProtocolVersion pv) {
        OfmMutableErrorExper errExp = (OfmMutableErrorExper)
                MessageFactory.create(pv, ERROR, ErrorType.EXPERIMENTER);
        errExp.clearXid();
        errExp.setExpType(EXP_TYPE);
        errExp.setExpId(ExperimenterId.NICIRA);
        errExp.setData(FAKE_DATA);
        encodeAndVerifyMessage(errExp.toImmutable(), file);
    }

    @Test
    public void encodeErrorExperimenter13() {
        print(EOL + "encodeErrorExperimenter13");
        verifyEncodingErrorExperimenter(TF_EREXP_13, V_1_3);
    }

    @Test
    public void encodeErrorExperimenter12() {
        print(EOL + "encodeErrorExperimenter12");
        verifyEncodingErrorExperimenter(TF_EREXP_12, V_1_2);
    }
}
