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
import org.opendaylight.util.net.IpProtocol;

import java.util.HashMap;
import java.util.Map;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV6_EXTHDR;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IP_PROTO;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.IpProtocol.TCP;
import static org.opendaylight.util.net.IpProtocol.UDP;

/**
 * Unit tests for {@link MfbIpProto}.
 *
 * @author Simon Hunt
 */
public class MfbIpProtoTest extends AbstractMatchTest {

    private static MfbIpProto create(ProtocolVersion pv, IpProtocol ipp) {
        return (MfbIpProto) createBasicField(pv, IP_PROTO, ipp);
    }

    private static final MfbIpProto P_10_TCP = create(V_1_0, TCP);
    private static final MfbIpProto P_10_TCP_COPY = create(V_1_0, TCP);
    private static final MfbIpProto P_13_TCP = create(V_1_3, TCP);
    private static final MfbIpProto P_13_TCP_COPY = create(V_1_3, TCP);

    private static final MfbIpProto P_10_UDP = create(V_1_0, UDP);
    private static final MfbIpProto P_10_UDP_COPY = create(V_1_0, UDP);
    private static final MfbIpProto P_13_UDP = create(V_1_3, UDP);
    private static final MfbIpProto P_13_UDP_COPY = create(V_1_3, UDP);

    private static final Map<IPv6ExtHdr, Boolean> EH_FLAGS =
            new HashMap<IPv6ExtHdr, Boolean>();
    static {
        EH_FLAGS.put(IPv6ExtHdr.NO_NEXT, true);
        EH_FLAGS.put(IPv6ExtHdr.AUTH, false);
    }
    private static final MfbIpv6Exthdr EH = (MfbIpv6Exthdr)
            createBasicField(V_1_3, IPV6_EXTHDR, EH_FLAGS);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyNotSameButEqual(P_10_TCP, P_10_TCP_COPY);
        verifyNotSameButEqual(P_13_TCP, P_13_TCP_COPY);
        verifyNotSameButEqual(P_10_UDP, P_10_UDP_COPY);
        verifyNotSameButEqual(P_13_UDP, P_13_UDP_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(P_10_TCP, P_13_TCP);
        verifyNotSameButEqual(P_10_UDP, P_13_UDP);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(P_10_TCP, P_10_UDP);
        verifyNotEqual(P_10_TCP, P_13_UDP);
        verifyNotEqual(P_13_TCP, P_10_UDP);
        verifyNotEqual(P_13_TCP, P_13_UDP);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(P_10_TCP, EH);
        verifyNotEqual(P_13_TCP, EH);
        verifyNotEqual(P_10_UDP, EH);
        verifyNotEqual(P_13_UDP, EH);
    }
}
