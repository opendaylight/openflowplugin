/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;

import static org.opendaylight.of.lib.ExperimenterId.HP;
import static org.opendaylight.of.lib.ExperimenterId.NICIRA;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createExperimenterField;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldExperimenter}.
 *
 * @author Simon Hunt
 */
public class MFieldExperimenterTest extends AbstractMatchTest {

    private static final byte[] DATA_A = { 1, 1, 1, 1 };
    private static final byte[] DATA_B = { 1, 1, 1, 1, 2 };

    private static final MFieldExperimenter HP_10_FT1_A =
            createExperimenterField(V_1_0, 1, HP, DATA_A);
    private static final MFieldExperimenter HP_10_FT1_A_COPY =
            createExperimenterField(V_1_0, 1, HP, DATA_A);
    private static final MFieldExperimenter HP_13_FT1_A =
            createExperimenterField(V_1_3, 1, HP, DATA_A);
    private static final MFieldExperimenter HP_13_FT2_A =
            createExperimenterField(V_1_3, 2, HP, DATA_A);
    private static final MFieldExperimenter HP_13_FT1_B =
            createExperimenterField(V_1_3, 1, HP, DATA_B);

    private static final MFieldExperimenter NICIRA_13_FT1_B =
            createExperimenterField(V_1_3, 1, NICIRA, DATA_B);


    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(HP_10_FT1_A, HP_10_FT1_A);
        verifyNotSameButEqual(HP_10_FT1_A, HP_10_FT1_A_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(HP_10_FT1_A, HP_13_FT1_A);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(HP_13_FT1_A, HP_13_FT1_B);
        verifyNotEqual(HP_13_FT1_A, HP_13_FT2_A);
        verifyNotEqual(HP_13_FT1_B, NICIRA_13_FT1_B);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(HP_10_FT1_A, OTHER_FIELD);
    }

}
