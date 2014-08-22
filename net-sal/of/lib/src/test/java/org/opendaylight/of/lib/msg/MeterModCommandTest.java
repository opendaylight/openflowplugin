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
import static org.opendaylight.of.lib.msg.MeterModCommand.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MeterModCommand.
 *
 * @author Simon Hunt
 */
public class MeterModCommandTest extends AbstractCodeBasedEnumTest<MeterModCommand> {

    @Override
    protected MeterModCommand decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return MeterModCommand.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (MeterModCommand c : MeterModCommand.values())
            print(c);
        assertEquals(AM_UXCC, 3, MeterModCommand.values().length);
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
        check(V_1_3, 0, ADD);
        check(V_1_3, 1, MODIFY);
        check(V_1_3, 2, DELETE);
        check(V_1_3, 3, null);
    }

}
