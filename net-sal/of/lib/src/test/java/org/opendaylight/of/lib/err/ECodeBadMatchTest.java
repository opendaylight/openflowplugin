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
import static org.opendaylight.of.lib.err.ECodeBadMatch.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeBadMatch}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeBadMatchTest extends AbstractCodeBasedEnumTest<ECodeBadMatch> {

    @Override
    protected ECodeBadMatch decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeBadMatch.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeBadMatch ec: ECodeBadMatch.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.BAD_MATCH, parent);
        }
        assertEquals(AM_UXCC, 12, ECodeBadMatch.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, BAD_TYPE);
        check(V_1_1, 1, BAD_LEN);
        check(V_1_1, 2, BAD_TAG);
        check(V_1_1, 3, BAD_DL_ADDR_MASK);
        check(V_1_1, 4, BAD_NW_ADDR_MASK);
        check(V_1_1, 5, BAD_WILDCARDS);
        check(V_1_1, 6, BAD_FIELD);
        check(V_1_1, 7, BAD_VALUE);
        check(V_1_1, 8, null, true);
        check(V_1_1, 9, null, true);
        check(V_1_1, 10, null, true);
        check(V_1_1, 11, null, true);
        check(V_1_1, 12, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, BAD_TYPE);
        check(V_1_2, 1, BAD_LEN);
        check(V_1_2, 2, BAD_TAG);
        check(V_1_2, 3, BAD_DL_ADDR_MASK);
        check(V_1_2, 4, BAD_NW_ADDR_MASK);
        check(V_1_2, 5, BAD_WILDCARDS);
        check(V_1_2, 6, BAD_FIELD);
        check(V_1_2, 7, BAD_VALUE);
        check(V_1_2, 8, BAD_MASK);
        check(V_1_2, 9, BAD_PREREQ);
        check(V_1_2, 10, DUP_FIELD);
        check(V_1_2, 11, EPERM);
        check(V_1_2, 12, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, BAD_TYPE);
        check(V_1_3, 1, BAD_LEN);
        check(V_1_3, 2, BAD_TAG);
        check(V_1_3, 3, BAD_DL_ADDR_MASK);
        check(V_1_3, 4, BAD_NW_ADDR_MASK);
        check(V_1_3, 5, BAD_WILDCARDS);
        check(V_1_3, 6, BAD_FIELD);
        check(V_1_3, 7, BAD_VALUE);
        check(V_1_3, 8, BAD_MASK);
        check(V_1_3, 9, BAD_PREREQ);
        check(V_1_3, 10, DUP_FIELD);
        check(V_1_3, 11, EPERM);
        check(V_1_3, 12, null);
    }
}
