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
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.ICMPv4Type;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ETH_TYPE;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.ICMPV4_TYPE;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MfbEthType}.
 *
 * @author Simon Hunt
 */
public class MfbEthTypeTest extends AbstractMatchTest {

    private static MfbEthType create(ProtocolVersion pv, EthernetType et) {
        return (MfbEthType) createBasicField(pv, ETH_TYPE, et);
    }

    private static final MfbEthType ET_10_ARP = create(V_1_0, EthernetType.ARP);
    private static final MfbEthType ET_10_ARP_COPY = create(V_1_0, EthernetType.ARP);
    private static final MfbEthType ET_13_ARP = create(V_1_3, EthernetType.ARP);
    private static final MfbEthType ET_13_ARP_COPY = create(V_1_3, EthernetType.ARP);

    private static final MfbEthType ET_10_LLDP = create(V_1_0, EthernetType.LLDP);
    private static final MfbEthType ET_10_LLDP_COPY = create(V_1_0, EthernetType.LLDP);
    private static final MfbEthType ET_13_LLDP = create(V_1_3, EthernetType.LLDP);
    private static final MfbEthType ET_13_LLDP_COPY = create(V_1_3, EthernetType.LLDP);

    private static final MfbIcmpv4Type ITYPE = (MfbIcmpv4Type)
            createBasicField(V_1_3, ICMPV4_TYPE, ICMPv4Type.ECHO_REP);


    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyNotSameButEqual(ET_10_ARP, ET_10_ARP_COPY);
        verifyNotSameButEqual(ET_13_ARP, ET_13_ARP_COPY);
        verifyNotSameButEqual(ET_10_LLDP, ET_10_LLDP_COPY);
        verifyNotSameButEqual(ET_13_LLDP, ET_13_LLDP_COPY);
    }

    @Test
    public void diffVersSameFieldValue() {
        print(EOL + "diffVersSameFieldValue()");
        verifyNotSameButEqual(ET_10_LLDP, ET_13_LLDP);
        verifyNotSameButEqual(ET_10_ARP, ET_13_ARP);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(ET_10_ARP, ET_10_LLDP);
        verifyNotEqual(ET_10_ARP, ET_13_LLDP);
        verifyNotEqual(ET_13_ARP, ET_10_LLDP);
        verifyNotEqual(ET_13_ARP, ET_13_LLDP);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(ET_10_ARP, ITYPE);
        verifyNotEqual(ET_13_ARP, ITYPE);
        verifyNotEqual(ET_10_LLDP, ITYPE);
        verifyNotEqual(ET_13_LLDP, ITYPE);
    }
}
