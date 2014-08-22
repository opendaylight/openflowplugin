/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MatchType.
 *
 * @author Simon Hunt
 */
public class MatchTypeTest extends AbstractCodeBasedEnumTest<MatchType> {

    @Override
    protected MatchType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return MatchType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (MatchType t: MatchType.values())
            print(t);
        assertEquals(AM_UXCC, 2, MatchType.values().length);
    }


    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, MatchType.STANDARD);
        check(V_1_0, 1, null, true);
        check(V_1_0, 2, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, MatchType.STANDARD);
        check(V_1_1, 1, null, true);
        check(V_1_1, 2, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, MatchType.STANDARD);
        check(V_1_2, 1, MatchType.OXM);
        check(V_1_2, 2, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, MatchType.STANDARD);
        check(V_1_3, 1, MatchType.OXM);
        check(V_1_3, 2, null);
    }

}
