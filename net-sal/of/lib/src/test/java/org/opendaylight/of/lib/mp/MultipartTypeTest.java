/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.mp.MultipartType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MultipartType.
 *
 * @author Simon Hunt
 */
public class MultipartTypeTest extends AbstractCodeBasedEnumTest<MultipartType> {

    @Override
    protected MultipartType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return MultipartType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (MultipartType t: MultipartType.values())
            print(t);
        assertEquals(AM_UXCC, 15, MultipartType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, DESC);
        check(V_1_0, 1, FLOW);
        check(V_1_0, 2, AGGREGATE);
        check(V_1_0, 3, TABLE);
        check(V_1_0, 4, PORT_STATS);
        check(V_1_0, 5, QUEUE);
        check(V_1_0, 6, null, true);
        check(V_1_0, 7, null, true);
        check(V_1_0, 8, null, true);
        check(V_1_0, 9, null, true);
        check(V_1_0, 10, null, true);
        check(V_1_0, 11, null, true);
        check(V_1_0, 12, null, true);
        check(V_1_0, 13, null, true);
        check(V_1_0, 14, null);
        check(V_1_0, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, DESC);
        check(V_1_1, 1, FLOW);
        check(V_1_1, 2, AGGREGATE);
        check(V_1_1, 3, TABLE);
        check(V_1_1, 4, PORT_STATS);
        check(V_1_1, 5, QUEUE);
        check(V_1_1, 6, GROUP);
        check(V_1_1, 7, GROUP_DESC);
        check(V_1_1, 8, null, true);
        check(V_1_1, 9, null, true);
        check(V_1_1, 10, null, true);
        check(V_1_1, 11, null, true);
        check(V_1_1, 12, null, true);
        check(V_1_1, 13, null, true);
        check(V_1_1, 14, null);
        check(V_1_1, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, DESC);
        check(V_1_2, 1, FLOW);
        check(V_1_2, 2, AGGREGATE);
        check(V_1_2, 3, TABLE);
        check(V_1_2, 4, PORT_STATS);
        check(V_1_2, 5, QUEUE);
        check(V_1_2, 6, GROUP);
        check(V_1_2, 7, GROUP_DESC);
        check(V_1_2, 8, GROUP_FEATURES);
        check(V_1_2, 9, null, true);
        check(V_1_2, 10, null, true);
        check(V_1_2, 11, null, true);
        check(V_1_2, 12, null, true);
        check(V_1_2, 13, null, true);
        check(V_1_2, 14, null);
        check(V_1_2, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, DESC);
        check(V_1_3, 1, FLOW);
        check(V_1_3, 2, AGGREGATE);
        check(V_1_3, 3, TABLE);
        check(V_1_3, 4, PORT_STATS);
        check(V_1_3, 5, QUEUE);
        check(V_1_3, 6, GROUP);
        check(V_1_3, 7, GROUP_DESC);
        check(V_1_3, 8, GROUP_FEATURES);
        check(V_1_3, 9, METER);
        check(V_1_3, 10, METER_CONFIG);
        check(V_1_3, 11, METER_FEATURES);
        check(V_1_3, 12, TABLE_FEATURES);
        check(V_1_3, 13, PORT_DESC);
        check(V_1_3, 14, null);
        check(V_1_3, 0xffff, EXPERIMENTER);
    }

}
