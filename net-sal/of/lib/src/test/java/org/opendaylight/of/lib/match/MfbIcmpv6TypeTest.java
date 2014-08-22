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
import org.opendaylight.util.net.ICMPv6Type;
import org.opendaylight.util.net.IpProtocol;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ICMPV6_TYPE;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IP_PROTO;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.ICMPv6Type.NBR_ADV;
import static org.opendaylight.util.net.ICMPv6Type.NBR_SOL;

/**
 * Unit tests for {@link org.opendaylight.of.lib.match.MfbIcmpv4Type}.
 *
 * @author Simon Hunt
 */
public class MfbIcmpv6TypeTest extends AbstractMatchTest {

    private static MfbIcmpv6Type create(ProtocolVersion pv, ICMPv6Type type) {
        return (MfbIcmpv6Type) createBasicField(pv, ICMPV6_TYPE, type);
    }

    private static final MfbIcmpv6Type I_10_NSOL = create(V_1_0, NBR_SOL);
    private static final MfbIcmpv6Type I_10_NSOL_COPY = create(V_1_0, NBR_SOL);
    private static final MfbIcmpv6Type I_13_NSOL = create(V_1_3, NBR_SOL);
    private static final MfbIcmpv6Type I_13_NSOL_COPY = create(V_1_3, NBR_SOL);

    private static final MfbIcmpv6Type I_10_NADV = create(V_1_0, NBR_ADV);
    private static final MfbIcmpv6Type I_10_NADV_COPY = create(V_1_0, NBR_ADV);
    private static final MfbIcmpv6Type I_13_NADV = create(V_1_3, NBR_ADV);
    private static final MfbIcmpv6Type I_13_NADV_COPY = create(V_1_3, NBR_ADV);

    private static final MfbIpProto IPP = (MfbIpProto)
            createBasicField(V_1_3, IP_PROTO, IpProtocol.TCP);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyNotSameButEqual(I_10_NSOL, I_10_NSOL_COPY);
        verifyNotSameButEqual(I_13_NSOL, I_13_NSOL_COPY);
        verifyNotSameButEqual(I_10_NADV, I_10_NADV_COPY);
        verifyNotSameButEqual(I_13_NADV, I_13_NADV_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(I_10_NSOL, I_13_NSOL);
        verifyNotSameButEqual(I_10_NADV, I_13_NADV);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(I_10_NSOL, I_10_NADV);
        verifyNotEqual(I_10_NSOL, I_13_NADV);
        verifyNotEqual(I_13_NSOL, I_10_NADV);
        verifyNotEqual(I_13_NSOL, I_13_NADV);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(I_10_NSOL, IPP);
        verifyNotEqual(I_13_NSOL, IPP);
        verifyNotEqual(I_10_NADV, IPP);
        verifyNotEqual(I_13_NADV, IPP);
    }
}
