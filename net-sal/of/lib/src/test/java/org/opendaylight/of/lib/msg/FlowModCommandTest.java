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
import static org.opendaylight.of.lib.msg.FlowModCommand.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for FlowModCommand.
 *
 * @author Simon Hunt
 */
public class FlowModCommandTest extends AbstractCodeBasedEnumTest<FlowModCommand> {

    @Override
    protected FlowModCommand decode (int code, ProtocolVersion pv)
            throws DecodeException {
        return FlowModCommand.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (FlowModCommand c: FlowModCommand.values())
            print(c);
        assertEquals(AM_UXCC, 5, FlowModCommand.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, ADD);
        check(V_1_0, 1, MODIFY);
        check(V_1_0, 2, MODIFY_STRICT);
        check(V_1_0, 3, DELETE);
        check(V_1_0, 4, DELETE_STRICT);
        check(V_1_0, 5, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, ADD);
        check(V_1_1, 1, MODIFY);
        check(V_1_1, 2, MODIFY_STRICT);
        check(V_1_1, 3, DELETE);
        check(V_1_1, 4, DELETE_STRICT);
        check(V_1_1, 5, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, ADD);
        check(V_1_2, 1, MODIFY);
        check(V_1_2, 2, MODIFY_STRICT);
        check(V_1_2, 3, DELETE);
        check(V_1_2, 4, DELETE_STRICT);
        check(V_1_2, 5, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, ADD);
        check(V_1_3, 1, MODIFY);
        check(V_1_3, 2, MODIFY_STRICT);
        check(V_1_3, 3, DELETE);
        check(V_1_3, 4, DELETE_STRICT);
        check(V_1_3, 5, null);
    }

}
