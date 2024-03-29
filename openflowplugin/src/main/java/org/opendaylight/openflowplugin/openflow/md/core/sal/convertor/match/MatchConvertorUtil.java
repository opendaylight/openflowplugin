/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Iterator;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.dscp._case.IpDscpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.pcp._case.VlanPcpBuilder;

/**
 * Match related tools.
 */
public final class MatchConvertorUtil {
    // Pre-calculated masks for the 33 possible values. Do not give them out, but clone() them as they may
    // end up being leaked and vulnerable.
    private static final byte[][] IPV4_MASKS;

    static {
        final byte[][] tmp = new byte[33][];
        for (int i = 0; i <= 32; ++i) {
            final int mask = 0xffffffff << 32 - i;
            tmp[i] = new byte[]{(byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8), (byte) mask};
        }

        IPV4_MASKS = tmp;
    }

    private MatchConvertorUtil() {
    }

    /**
     * Ipv 6 exthdr flags to int integer.
     *
     * @param flags ipv6 external header flag
     * @return integer containing lower 9 bits filled with corresponding flags
     */
    public static int ipv6ExthdrFlagsToInt(final Ipv6ExthdrFlags flags) {
        // FIXME: this should be 'int'
        int bitmap = 0;
        // FIXME: use explicit if (flags) { bitmap |= 1 << 2; }
        bitmap |= flags.getNonext() ? 1 : 0;
        bitmap |= flags.getEsp() ? 1 << 1 : 0;
        bitmap |= flags.getAuth() ? 1 << 2 : 0;
        bitmap |= flags.getDest() ? 1 << 3 : 0;
        bitmap |= flags.getFrag() ? 1 << 4 : 0;
        bitmap |= flags.getRouter() ? 1 << 5 : 0;
        bitmap |= flags.getHop() ? 1 << 6 : 0;
        bitmap |= flags.getUnrep() ? 1 << 7 : 0;
        bitmap |= flags.getUnseq() ? 1 << 8 : 0;
        return bitmap;
    }

    /**
     * Extract ipv 4 mask byte [ ].
     *
     * @param addressParts the address parts
     * @return the byte [ ]
     */
    @SuppressFBWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    public static byte @Nullable[] extractIpv4Mask(final Iterator<String> addressParts) {
        final int prefix;
        if (addressParts.hasNext()) {
            int potentionalPrefix = Integer.parseInt(addressParts.next());
            prefix = potentionalPrefix < 32 ? potentionalPrefix : 0;
        } else {
            prefix = 0;
        }

        if (prefix != 0) {
            // clone() is necessary to protect our constants
            return IPV4_MASKS[prefix].clone();
        }

        return null;
    }

    /**
     * To of ip dscp match entry.
     *
     * @param ipDscp the ip dscp
     * @return the match entry
     */
    public static MatchEntry toOfIpDscp(final Dscp ipDscp) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.VALUE);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(IpDscp.VALUE);

        IpDscpCaseBuilder ipDscpCaseBuilder = new IpDscpCaseBuilder();
        IpDscpBuilder ipDscpBuilder = new IpDscpBuilder();
        ipDscpBuilder.setDscp(ipDscp);
        ipDscpCaseBuilder.setIpDscp(ipDscpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipDscpCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    /**
     * To of vlan pcp match entry.
     *
     * @param vlanPcp the vlan pcp
     * @return the match entry
     */
    public static MatchEntry toOfVlanPcp(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp vlanPcp) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.VALUE);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(VlanPcp.VALUE);
        VlanPcpCaseBuilder vlanPcpCaseBuilder = new VlanPcpCaseBuilder();
        VlanPcpBuilder vlanPcpBuilder = new VlanPcpBuilder();
        vlanPcpBuilder.setVlanPcp(vlanPcp.getValue());
        vlanPcpCaseBuilder.setVlanPcp(vlanPcpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(vlanPcpCaseBuilder.build());
        return matchEntryBuilder.build();
    }
}
