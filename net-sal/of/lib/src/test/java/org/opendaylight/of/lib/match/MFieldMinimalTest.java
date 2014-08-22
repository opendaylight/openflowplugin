/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createMinimalField;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldMinimal}.
 *
 * @author Simon Hunt
 */
public class MFieldMinimalTest extends AbstractMatchTest {

    private static final byte[] DATA_A = { 1, 2, 3, 4 };
    private static final byte[] DATA_B = { 5, 6, 7, 8 };

    private static final MFieldMinimal MIN_10_BS_A =
            createMinimalField(V_1_0, OxmClass.BIG_SWITCH, 7, DATA_A);
    private static final MFieldMinimal MIN_10_BS_A_COPY =
            createMinimalField(V_1_0, OxmClass.BIG_SWITCH, 7, DATA_A);
    private static final MFieldMinimal MIN_13_BS_A =
            createMinimalField(V_1_3, OxmClass.BIG_SWITCH, 7, DATA_A);
    private static final MFieldMinimal MIN_13_BS_B =
            createMinimalField(V_1_3, OxmClass.BIG_SWITCH, 7, DATA_B);

    private static final MFieldMinimal MIN_13_HP_B =
            createMinimalField(V_1_3, OxmClass.HP, 7, DATA_B);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(MIN_10_BS_A, MIN_10_BS_A);
        verifyNotSameButEqual(MIN_10_BS_A, MIN_10_BS_A_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(MIN_10_BS_A, MIN_13_BS_A);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(MIN_13_BS_A, MIN_13_BS_B);
        verifyNotEqual(MIN_13_BS_B, MIN_13_HP_B);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(MIN_13_BS_B, OTHER_FIELD);
    }

}
