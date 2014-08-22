/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.util.net.VlanId;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.IPV6_EXTHDR;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.VLAN_VID;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MfbIpv6Exthdr}.
 *
 * @author Simon Hunt
 */
public class MfbIpv6ExthdrTest extends AbstractMatchTest {

    private static MfbIpv6Exthdr create(Map<IPv6ExtHdr, Boolean> flags) {
        return (MfbIpv6Exthdr) createBasicField(V_1_3, IPV6_EXTHDR, flags);
    }

    private static final Set<IPv6ExtHdr> EH_TRUE_FLAGS =
            EnumSet.of(IPv6ExtHdr.AUTH, IPv6ExtHdr.FRAG, IPv6ExtHdr.HOP);
    private static final Map<IPv6ExtHdr, Boolean> EH_FLAGS_ONE = new HashMap<>();
    private static final Map<IPv6ExtHdr, Boolean> EH_FLAGS_TWO = new HashMap<>();
    private static final Map<IPv6ExtHdr, Boolean> EH_FLAGS_THREE = new HashMap<>();
    static {
        // all 9 flags (explicitly defined) in map ONE
        for (IPv6ExtHdr eh: IPv6ExtHdr.values())
            EH_FLAGS_ONE.put(eh, EH_TRUE_FLAGS.contains(eh));

        // just 3 flags, all true, in map TWO
        for (IPv6ExtHdr eh: EH_TRUE_FLAGS)
            EH_FLAGS_TWO.put(eh, true);

        // same 3 flags, but only HOP true, in map THREE
        for (IPv6ExtHdr eh: EH_TRUE_FLAGS)
            EH_FLAGS_THREE.put(eh, eh.equals(IPv6ExtHdr.HOP));
    }



    private static final MfbIpv6Exthdr EH_AFH_ALL_FLAGS = create(EH_FLAGS_ONE);

    private static final MfbIpv6Exthdr EH_AFH_TRUE = create(EH_FLAGS_TWO);
    private static final MfbIpv6Exthdr EH_AFH_TRUE_COPY = create(EH_FLAGS_TWO);

    private static final MfbIpv6Exthdr EH_AFH_MIXED = create(EH_FLAGS_THREE);
    private static final MfbIpv6Exthdr EH_AFH_MIXED_COPY = create(EH_FLAGS_THREE);

    private static final MfbVlanVid VLAN_27 = (MfbVlanVid)
            createBasicField(V_1_3, VLAN_VID, VlanId.valueOf(27));

    @Test
    public void basic() {
        print(EOL + "basic()");
        verifyNotSameButEqual(EH_AFH_MIXED, EH_AFH_MIXED_COPY);
        verifyNotSameButEqual(EH_AFH_TRUE, EH_AFH_TRUE_COPY);
    }

    @Test
    public void sameValueButWithMasks() {
        print(EOL + "sameValueButWithMasks()");
        verifyNotEqual(EH_AFH_TRUE, EH_AFH_MIXED);
    }

    @Test
    public void sameFieldDiffValues() {
        print(EOL + "sameFieldDiffValues()");
        verifyNotEqual(EH_AFH_MIXED, EH_AFH_ALL_FLAGS);
    }

    @Test
    public void differentFields() {
        print(EOL + "differentFields()");
        verifyNotEqual(EH_AFH_MIXED, VLAN_27);
        verifyNotEqual(EH_AFH_ALL_FLAGS, VLAN_27);
    }

    private static final int ALL_BITS_ENCODED = 0x1ff;
    private static final int ZERO = 0;

    @Test
    public void allFlagsPresent() {
        print(EOL + "allFlagsPresent()");
        Map<IPv6ExtHdr, Boolean> inputMap = new HashMap<>();
        for (IPv6ExtHdr eh: IPv6ExtHdr.values())
            inputMap.put(eh, true);

        MfbIpv6Exthdr mf = (MfbIpv6Exthdr)
                createBasicField(V_1_3, IPV6_EXTHDR, inputMap);
        print(mf);
        // value should contain all flags; mask should be dropped
        assertEquals(AM_NEQ, ALL_BITS_ENCODED, mf.rawBits);
        assertFalse(AM_HUH, mf.hasMask());
        assertEquals(AM_NEQ, ZERO, mf.mask);

        Map<IPv6ExtHdr, Boolean> flags = mf.getFlags();
        print(flags);
        assertEquals(AM_NEQ, inputMap, flags);
    }

    @Test
    public void allFlagsAbsent() {
        print(EOL + "allFlagsAbsent()");
        Map<IPv6ExtHdr, Boolean> map = new HashMap<>();
        for (IPv6ExtHdr eh: IPv6ExtHdr.values())
            map.put(eh, false);

        MfbIpv6Exthdr mf = (MfbIpv6Exthdr)
                createBasicField(V_1_3, IPV6_EXTHDR, map);
        print(mf);
        // value should contain no flags; mask should be dropped
        assertEquals(AM_NEQ, ZERO, mf.rawBits);
        assertFalse(AM_HUH, mf.hasMask());
        assertEquals(AM_NEQ, ZERO, mf.mask);

        Map<IPv6ExtHdr, Boolean> flags = mf.getFlags();
        print(flags);
        assertEquals(AM_NEQ, map, flags);
    }

    /*                                  Mask    Value
    AUTH   0x04          Present         X       X
    FRAG   0x10          Absent          X
    ROUTER 0x20          Present         X       X
    HOP    0x40          Absent          X
    */
    private static final int MASK_AUTH_FRAG_ROUTER_HOP = 0x74;
    private static final int VALUE_AUTH_FRAG_ROUTER_HOP = 0x24;

    @Test
    public void presentAbsentSample() {
        print(EOL + "presentAbsentSample()");
        Map<IPv6ExtHdr, Boolean> map = new HashMap<>();
        map.put(IPv6ExtHdr.AUTH, true);
        map.put(IPv6ExtHdr.FRAG, false);
        map.put(IPv6ExtHdr.ROUTER, true);
        map.put(IPv6ExtHdr.HOP, false);

        MfbIpv6Exthdr mf = (MfbIpv6Exthdr)
                createBasicField(V_1_3, IPV6_EXTHDR, map);
        print(mf);
        // mask should contain all flags; value should contain no flags
        assertEquals(AM_NEQ, MASK_AUTH_FRAG_ROUTER_HOP, mf.mask);
        assertEquals(AM_NEQ, VALUE_AUTH_FRAG_ROUTER_HOP, mf.rawBits);

        Map<IPv6ExtHdr, Boolean> flags = mf.getFlags();
        print(flags);
        assertEquals(AM_NEQ, map, flags);
    }
}
