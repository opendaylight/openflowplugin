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
import static org.opendaylight.of.lib.err.ECodeGroupModFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeGroupModFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeGroupModFailedTest extends AbstractCodeBasedEnumTest<ECodeGroupModFailed> {

    @Override
    protected ECodeGroupModFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeGroupModFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeGroupModFailed ec: ECodeGroupModFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.GROUP_MOD_FAILED, parent);
        }
        assertEquals(AM_UXCC, 15, ECodeGroupModFailed.values().length);
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
        check(V_1_1, 0, GROUP_EXISTS);
        check(V_1_1, 1, INVALID_GROUP);
        check(V_1_1, 2, WEIGHT_UNSUPPORTED);
        check(V_1_1, 3, OUT_OF_GROUPS);
        check(V_1_1, 4, OUT_OF_BUCKETS);
        check(V_1_1, 5, CHAINING_UNSUPPORTED);
        check(V_1_1, 6, WATCH_UNSUPPORTED);
        check(V_1_1, 7, LOOP);
        check(V_1_1, 8, UNKNOWN_GROUP);
        check(V_1_1, 9, null, true);
        check(V_1_1, 10, null, true);
        check(V_1_1, 11, null, true);
        check(V_1_1, 12, null, true);
        check(V_1_1, 13, null, true);
        check(V_1_1, 14, null, true);
        check(V_1_1, 15, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, GROUP_EXISTS);
        check(V_1_2, 1, INVALID_GROUP);
        check(V_1_2, 2, WEIGHT_UNSUPPORTED);
        check(V_1_2, 3, OUT_OF_GROUPS);
        check(V_1_2, 4, OUT_OF_BUCKETS);
        check(V_1_2, 5, CHAINING_UNSUPPORTED);
        check(V_1_2, 6, WATCH_UNSUPPORTED);
        check(V_1_2, 7, LOOP);
        check(V_1_2, 8, UNKNOWN_GROUP);
        check(V_1_2, 9, CHAINED_GROUP);
        check(V_1_2, 10, BAD_TYPE);
        check(V_1_2, 11, BAD_COMMAND);
        check(V_1_2, 12, BAD_BUCKET);
        check(V_1_2, 13, BAD_WATCH);
        check(V_1_2, 14, EPERM);
        check(V_1_2, 15, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, GROUP_EXISTS);
        check(V_1_3, 1, INVALID_GROUP);
        check(V_1_3, 2, WEIGHT_UNSUPPORTED);
        check(V_1_3, 3, OUT_OF_GROUPS);
        check(V_1_3, 4, OUT_OF_BUCKETS);
        check(V_1_3, 5, CHAINING_UNSUPPORTED);
        check(V_1_3, 6, WATCH_UNSUPPORTED);
        check(V_1_3, 7, LOOP);
        check(V_1_3, 8, UNKNOWN_GROUP);
        check(V_1_3, 9, CHAINED_GROUP);
        check(V_1_3, 10, BAD_TYPE);
        check(V_1_3, 11, BAD_COMMAND);
        check(V_1_3, 12, BAD_BUCKET);
        check(V_1_3, 13, BAD_WATCH);
        check(V_1_3, 14, EPERM);
        check(V_1_3, 15, null);
    }
}
