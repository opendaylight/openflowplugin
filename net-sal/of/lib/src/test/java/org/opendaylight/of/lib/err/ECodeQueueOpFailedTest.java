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
import static org.opendaylight.of.lib.err.ECodeQueueOpFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeQueueOpFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeQueueOpFailedTest extends AbstractCodeBasedEnumTest<ECodeQueueOpFailed> {

    @Override
    protected ECodeQueueOpFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeQueueOpFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeQueueOpFailed ec: ECodeQueueOpFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.QUEUE_OP_FAILED, parent);
        }
        assertEquals(AM_UXCC, 3, ECodeQueueOpFailed.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, BAD_PORT);
        check(V_1_0, 1, BAD_QUEUE);
        check(V_1_0, 2, EPERM);
        check(V_1_0, 3, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, BAD_PORT);
        check(V_1_1, 1, BAD_QUEUE);
        check(V_1_1, 2, EPERM);
        check(V_1_1, 3, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, BAD_PORT);
        check(V_1_2, 1, BAD_QUEUE);
        check(V_1_2, 2, EPERM);
        check(V_1_2, 3, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, BAD_PORT);
        check(V_1_3, 1, BAD_QUEUE);
        check(V_1_3, 2, EPERM);
        check(V_1_3, 3, null);
    }
}
