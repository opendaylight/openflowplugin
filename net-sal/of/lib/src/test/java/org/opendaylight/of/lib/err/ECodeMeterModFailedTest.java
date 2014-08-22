/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.err;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.err.ECodeMeterModFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeMeterModFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeMeterModFailedTest extends AbstractCodeBasedEnumTest<ECodeMeterModFailed> {
    @Override
    protected ECodeMeterModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeMeterModFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeMeterModFailed ec: ECodeMeterModFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.METER_MOD_FAILED, parent);
        }
        assertEquals(AM_UXCC, 12, ECodeMeterModFailed.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        notSup(V_1_1);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        notSup(V_1_2);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, UNKNOWN);
        check(V_1_3, 1, METER_EXISTS);
        check(V_1_3, 2, INVALID_METER);
        check(V_1_3, 3, UNKNOWN_METER);
        check(V_1_3, 4, BAD_COMMAND);
        check(V_1_3, 5, BAD_FLAGS);
        check(V_1_3, 6, BAD_RATE);
        check(V_1_3, 7, BAD_BURST);
        check(V_1_3, 8, BAD_BAND);
        check(V_1_3, 9, BAD_BAND_VALUE);
        check(V_1_3, 10, OUT_OF_METERS);
        check(V_1_3, 11, OUT_OF_BANDS);
        check(V_1_3, 12, null);
    }
}
