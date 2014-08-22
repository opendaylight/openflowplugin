/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractCodeBasedEnumTest;
import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.msg.HelloElementType.UNKNOWN;
import static org.opendaylight.of.lib.msg.HelloElementType.VERSION_BITMAP;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for HelloElementType.
 *
 * @author Simon Hunt
 */
public class HelloElementTypeTest extends AbstractCodeBasedEnumTest<HelloElementType> {
    @Override
    protected HelloElementType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return HelloElementType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (HelloElementType t: HelloElementType.values())
            print(t);
        assertEquals(AM_UXCC, 2, HelloElementType.values().length);
    }

    private void checkUnk(ProtocolVersion pv, int code,
                          HelloElementType expType) {
        HelloElementType type;
        try {
            type = decode(code, pv);
            int t = expType.getCode(pv);
            int expCode = expType == UNKNOWN ? UNKNOWN.getCode(pv) : code;
            print("{} -> {} -> {}", code, type, t);
            assertEquals(AM_NEQ, expType, type);
            assertEquals(AM_NEQ, expCode, t);
        } catch (DecodeException e) {
            fail(AM_UNEX);
        }

    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        checkUnk(V_1_3, -1, UNKNOWN);
        checkUnk(V_1_3, 0, UNKNOWN);
        checkUnk(V_1_3, 1, VERSION_BITMAP);
        checkUnk(V_1_3, 2, UNKNOWN);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        checkUnk(V_1_3, -1, UNKNOWN);
        checkUnk(V_1_3, 0, UNKNOWN);
        checkUnk(V_1_3, 1, VERSION_BITMAP);
        checkUnk(V_1_3, 2, UNKNOWN);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        checkUnk(V_1_3, -1, UNKNOWN);
        checkUnk(V_1_3, 0, UNKNOWN);
        checkUnk(V_1_3, 1, VERSION_BITMAP);
        checkUnk(V_1_3, 2, UNKNOWN);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        checkUnk(V_1_3, -1, UNKNOWN);
        checkUnk(V_1_3, 0, UNKNOWN);
        checkUnk(V_1_3, 1, VERSION_BITMAP);
        checkUnk(V_1_3, 2, UNKNOWN);
    }

}
