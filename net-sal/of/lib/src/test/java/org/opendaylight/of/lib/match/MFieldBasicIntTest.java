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
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ICMPV4_CODE;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IP_DSCP;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldBasicInt}.
 *
 * @author Simon Hunt
 */
public class MFieldBasicIntTest extends AbstractMatchTest {

    private static final MfbIpDscp DSCP_10_22 =
            (MfbIpDscp) createBasicField(V_1_0, IP_DSCP, 22);
    private static final MfbIpDscp DSCP_10_22_COPY =
            (MfbIpDscp) createBasicField(V_1_0, IP_DSCP, 22);
    private static final MfbIpDscp DSCP_13_22 =
            (MfbIpDscp) createBasicField(V_1_3, IP_DSCP, 22);
    private static final MfbIpDscp DSCP_13_44 =
            (MfbIpDscp) createBasicField(V_1_3, IP_DSCP, 44);

    private static final MfbIcmpv4Code ICMP_13_44 =
            (MfbIcmpv4Code) createBasicField(V_1_3, ICMPV4_CODE, 44);


    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(DSCP_10_22, DSCP_10_22);
        verifyNotSameButEqual(DSCP_10_22, DSCP_10_22_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(DSCP_10_22, DSCP_13_22);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(DSCP_13_22, DSCP_13_44);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(DSCP_13_44, ICMP_13_44);
        verifyNotEqual(ICMP_13_44, OTHER_FIELD);
    }
}
