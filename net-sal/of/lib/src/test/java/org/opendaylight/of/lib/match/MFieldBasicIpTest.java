/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.util.net.IpAddress;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MFieldBasicIp}.
 *
 * @author Simon Hunt
 */
public class MFieldBasicIpTest extends AbstractMatchTest {

    private static final IpAddress IP4_22 = ip("15.43.12.22");
    private static final IpAddress IP4_77 = ip("15.43.12.77");
    private static final IpAddress IP4_MASK = ip("255.255.0.0");
    private static final IpAddress IP6_BA4 = ip("fe00::f00:ba4");
    private static final IpAddress IP6_BA5 = ip("fe00::f00:ba5");
    private static final IpAddress IP6_MASK = ip("::ffff:ffff");

    private static final MfbIpv4Src IP4SRC_10_22 =
            (MfbIpv4Src) createBasicField(V_1_0, IPV4_SRC, IP4_22);
    private static final MfbIpv4Src IP4SRC_10_22_COPY =
            (MfbIpv4Src) createBasicField(V_1_0, IPV4_SRC, IP4_22);
    private static final MfbIpv4Src IP4SRC_13_22 =
            (MfbIpv4Src) createBasicField(V_1_3, IPV4_SRC, IP4_22);

    private static final MfbIpv4Dst IP4DST_13_22 =
            (MfbIpv4Dst) createBasicField(V_1_3, IPV4_DST, IP4_22);
    private static final MfbIpv4Dst IP4DST_13_77 =
            (MfbIpv4Dst) createBasicField(V_1_3, IPV4_DST, IP4_77);
    private static final MfbIpv4Dst IP4DST_13_77_MASKED =
            (MfbIpv4Dst) createBasicField(V_1_3, IPV4_DST, IP4_77, IP4_MASK);
    private static final MfbIpv4Dst IP4DST_13_77_MASKED_COPY =
            (MfbIpv4Dst) createBasicField(V_1_3, IPV4_DST, IP4_77, IP4_MASK);

    private static final MfbIpv6NdTarget IP6NDT_10_BA4 =
            (MfbIpv6NdTarget) createBasicField(V_1_0, IPV6_ND_TARGET, IP6_BA4);
    private static final MfbIpv6NdTarget IP6NDT_10_BA4_COPY =
            (MfbIpv6NdTarget) createBasicField(V_1_0, IPV6_ND_TARGET, IP6_BA4);
    private static final MfbIpv6NdTarget IP6NDT_13_BA4 =
            (MfbIpv6NdTarget) createBasicField(V_1_3, IPV6_ND_TARGET, IP6_BA4);
    private static final MfbIpv6NdTarget IP6NDT_13_BA5 =
            (MfbIpv6NdTarget) createBasicField(V_1_3, IPV6_ND_TARGET, IP6_BA5);
    private static final MfbIpv6NdTarget IP6NDT_13_BA5_MASKED =
            (MfbIpv6NdTarget) createBasicField(V_1_3, IPV6_ND_TARGET, IP6_BA5, IP6_MASK);
    private static final MfbIpv6NdTarget IP6NDT_13_BA5_MASKED_COPY =
            (MfbIpv6NdTarget) createBasicField(V_1_3, IPV6_ND_TARGET, IP6_BA5, IP6_MASK);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(IP4SRC_10_22, IP4SRC_10_22);
        verifyNotSameButEqual(IP4SRC_10_22, IP4SRC_10_22_COPY);
        verifyNotSameButEqual(IP4DST_13_77_MASKED, IP4DST_13_77_MASKED_COPY);

        verifyEqual(IP6NDT_10_BA4, IP6NDT_10_BA4);
        verifyNotSameButEqual(IP6NDT_10_BA4, IP6NDT_10_BA4_COPY);
        verifyNotSameButEqual(IP6NDT_13_BA5_MASKED, IP6NDT_13_BA5_MASKED_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(IP4SRC_10_22, IP4SRC_13_22);
        verifyNotSameButEqual(IP6NDT_10_BA4, IP6NDT_13_BA4);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(IP4DST_13_22, IP4DST_13_77);
        verifyNotEqual(IP6NDT_13_BA4, IP6NDT_13_BA5);
    }

    @Test
    public void maskNoMask() {
        print(EOL + "maskNoMask()");
        verifyNotEqual(IP4DST_13_77, IP4DST_13_77_MASKED);
        verifyNotEqual(IP6NDT_13_BA5, IP6NDT_13_BA5_MASKED);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(IP4DST_13_77, IP6NDT_13_BA5);
        verifyNotEqual(IP4SRC_13_22, IP4DST_13_22);
        verifyNotEqual(IP4SRC_13_22, OTHER_FIELD);
    }


    // =======
    // and just to satisfy my curiosity about the v4/v6 divide...

    @Test(expected = IllegalArgumentException.class)
    public void notV6arpSpa() {
        createBasicField(V_1_3, ARP_SPA, IP6_BA4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notV6arpTpa() {
        createBasicField(V_1_3, ARP_TPA, IP6_BA4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notV6ipv4Src() {
        createBasicField(V_1_3, IPV4_SRC, IP6_BA4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notV6ipv4Dst() {
        createBasicField(V_1_3, IPV4_DST, IP6_BA4);
    }

    // ===

    @Test(expected = IllegalArgumentException.class)
    public void notV4ipv6Src() {
        createBasicField(V_1_3, IPV6_SRC, IP4_22);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notV4ipv6Dst() {
        createBasicField(V_1_3, IPV6_DST, IP4_22);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notV4ipv6NdTarget() {
        createBasicField(V_1_3, IPV6_ND_TARGET, IP4_22);
    }
}
