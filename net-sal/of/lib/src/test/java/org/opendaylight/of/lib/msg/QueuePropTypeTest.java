/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
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
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.QueuePropType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for QueueProps.
 *
 * @author Simon Hunt
 */
public class QueuePropTypeTest extends AbstractCodeBasedEnumTest<QueuePropType> {

    @Override
    protected QueuePropType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return QueuePropType.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (QueuePropType qp: QueuePropType.values()) {
            print(qp);
        }
        assertEquals(AM_UXCC, 3, QueuePropType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 1, MIN_RATE);
        check(V_1_0, 2, null, true);
        check(V_1_0, 3, null);
        check(V_1_0, 0xffff, null, true);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 1, MIN_RATE);
        check(V_1_1, 2, null, true);
        check(V_1_1, 3, null);
        check(V_1_1, 0xffff, null, true);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, null);
        check(V_1_2, 1, MIN_RATE);
        check(V_1_2, 2, MAX_RATE);
        check(V_1_2, 3, null);
        check(V_1_2, 0xffff, EXPERIMENTER);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, null);
        check(V_1_3, 1, MIN_RATE);
        check(V_1_3, 2, MAX_RATE);
        check(V_1_3, 3, null);
        check(V_1_3, 0xffff, EXPERIMENTER);
    }

}
