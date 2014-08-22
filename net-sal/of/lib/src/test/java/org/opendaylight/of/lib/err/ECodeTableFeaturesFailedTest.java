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
import static org.opendaylight.of.lib.err.ECodeTableFeaturesFailed.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ECodeTableFeaturesFailed}.
 *
 * @author Pramod Shanbhag
 */
public class ECodeTableFeaturesFailedTest
    extends AbstractCodeBasedEnumTest<ECodeTableFeaturesFailed> {

    @Override
    protected ECodeTableFeaturesFailed decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ECodeTableFeaturesFailed.decode(code, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ECodeTableFeaturesFailed ec: ECodeTableFeaturesFailed.values()) {
            ErrorType parent = ec.parentType();
            print("{} :: {}", parent, ec);
            assertEquals(AM_NEQ, ErrorType.TABLE_FEATURES_FAILED, parent);
        }
        assertEquals(AM_UXCC, 6, ECodeTableFeaturesFailed.values().length);
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
        check(V_1_3, 0, BAD_TABLE);
        check(V_1_3, 1, BAD_METADATA);
        check(V_1_3, 2, BAD_TYPE);
        check(V_1_3, 3, BAD_LEN);
        check(V_1_3, 4, BAD_ARGUMENT);
        check(V_1_3, 5, EPERM);
        check(V_1_3, 6, null);
    }
}
