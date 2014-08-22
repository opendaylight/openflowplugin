/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IN_PHY_PORT;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IN_PORT;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldBasicBigPort}.
 *
 * @author Simon Hunt
 */
public class MFieldBasicBigPortTest extends AbstractMatchTest {

    private static MfbInPort createInPort(ProtocolVersion pv, int num) {
        return (MfbInPort) createBasicField(pv, IN_PORT, bpn(num));
    }

    private static MfbInPhyPort createInPhyPort(ProtocolVersion pv, int num) {
        return (MfbInPhyPort) createBasicField(pv, IN_PHY_PORT, bpn(num));
    }

    private static final MfbInPort P10_3 = createInPort(V_1_0, 3);
    private static final MfbInPort P10_3_COPY = createInPort(V_1_0, 3);
    private static final MfbInPort P13_3 = createInPort(V_1_3, 3);

    private static final MfbInPort P10_5 = createInPort(V_1_0, 5);
    private static final MfbInPort P13_5 = createInPort(V_1_3, 5);

    private static final MfbInPhyPort PP10_3 = createInPhyPort(V_1_0, 3);
    private static final MfbInPhyPort PP10_3_COPY = createInPhyPort(V_1_0, 3);
    private static final MfbInPhyPort PP13_3 = createInPhyPort(V_1_3, 3);

    private static final MfbInPhyPort PP10_5 = createInPhyPort(V_1_0, 5);
    private static final MfbInPhyPort PP13_5 = createInPhyPort(V_1_3, 5);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(P10_3, P10_3);
        verifyNotSameButEqual(P10_3, P10_3_COPY);

        verifyEqual(PP13_3, PP13_3);
        verifyNotSameButEqual(PP10_3, PP10_3_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(P10_3, P13_3);
        verifyNotSameButEqual(P10_5, P13_5);
        verifyNotSameButEqual(PP10_3, PP13_3);
        verifyNotSameButEqual(PP10_5, PP13_5);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(P10_3, P10_5);
        verifyNotEqual(P13_3, P13_5);
        verifyNotEqual(PP10_3, PP10_5);
        verifyNotEqual(PP13_3, PP13_5);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(P10_3, PP10_3);
        verifyNotEqual(P13_3, PP13_5);
        verifyNotEqual(P10_3, OTHER_FIELD);
    }
}
