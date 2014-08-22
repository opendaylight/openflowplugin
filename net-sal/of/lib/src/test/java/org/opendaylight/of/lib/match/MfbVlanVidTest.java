/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.net.IpProtocol;
import org.opendaylight.util.net.VlanId;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IP_PROTO;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.VLAN_VID;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit Tests for {@link MfbVlanVid}.
 *
 * @author Simon Hunt
 */
public class MfbVlanVidTest extends AbstractMatchTest {

    private static MfbVlanVid create(ProtocolVersion pv, int vid) {
        return (MfbVlanVid) createBasicField(pv, VLAN_VID, VlanId.valueOf(vid));
    }

    private static MfbVlanVid createNone(ProtocolVersion pv) {
        return (MfbVlanVid) createBasicField(pv, VLAN_VID, VlanId.NONE);
    }

    private static MfbVlanVid createPresent(ProtocolVersion pv) {
        return (MfbVlanVid) createBasicField(pv, VLAN_VID, VlanId.PRESENT);
    }

    private static final MfbVlanVid V_10_X42 = create(V_1_0, 42);
    private static final MfbVlanVid V_10_X42_COPY = create(V_1_0, 42);
    private static final MfbVlanVid V_13_X42 = create(V_1_3, 42);
    private static final MfbVlanVid V_13_X42_COPY = create(V_1_3, 42);

    private static final MfbVlanVid V_10_X37 = create(V_1_0, 37);
    private static final MfbVlanVid V_13_X37 = create(V_1_3, 37);

    private static final MfbVlanVid V_10_NONE = createNone(V_1_0);
    private static final MfbVlanVid V_10_NONE_COPY = createNone(V_1_0);
    private static final MfbVlanVid V_13_NONE = createNone(V_1_3);

    private static final MfbVlanVid V_10_PRES = createPresent(V_1_0);
    private static final MfbVlanVid V_13_PRES = createPresent(V_1_3);
    private static final MfbVlanVid V_13_PRES_COPY = createPresent(V_1_3);

    private static final MfbIpProto TCP = (MfbIpProto)
            createBasicField(V_1_3, IP_PROTO, IpProtocol.TCP);

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyEqual(V_13_X37, V_13_X37);
        verifyNotSameButEqual(V_10_X42, V_10_X42_COPY);
        verifyNotSameButEqual(V_13_X42, V_13_X42_COPY);
        verifyNotSameButEqual(V_10_NONE, V_10_NONE_COPY);
        verifyNotSameButEqual(V_13_PRES, V_13_PRES_COPY);
    }

    @Test
    public void diffVersSameValue() {
        print(EOL + "diffVersSameValue()");
        verifyNotSameButEqual(V_10_X42, V_13_X42);
        verifyNotSameButEqual(V_10_X37, V_13_X37);
        verifyNotSameButEqual(V_10_NONE, V_13_NONE);
        verifyNotSameButEqual(V_10_PRES, V_13_PRES);
    }

    @Test
    public void differentFieldValues() {
        print(EOL + "differentFieldValues()");
        verifyNotEqual(V_13_X42, V_13_X37);
        verifyNotEqual(V_13_X42, V_13_PRES);
        verifyNotEqual(V_13_X42, V_13_NONE);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(V_13_X37, TCP);
        verifyNotEqual(V_13_X42, TCP);
    }
}
