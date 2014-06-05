/*
 * Copyright (C) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import com.google.common.base.Splitter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcpFlagMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcpFlagMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

import java.util.Iterator;

/**
 * Temporary extension conversions until extensions are supported in MD_SAL OpenFlow v1.3 implementations
 */
public class NxmExtensionsConvertor {
    static final Splitter PREFIX_SPLITTER = Splitter.on('/');

    static MatchEntries toNxmTcpFlag(Integer tcpFlag) {
        MatchEntriesBuilder matchBuilder = new MatchEntriesBuilder();
        matchBuilder.setOxmClass(Nxm1Class.class);
        matchBuilder.setHasMask(false);
        matchBuilder.setOxmMatchField(TcpFlag.class);
        TcpFlagMatchEntryBuilder tcpFlagBuilder = new TcpFlagMatchEntryBuilder();
        tcpFlagBuilder.setTcpFlag(tcpFlag);
        matchBuilder.addAugmentation(TcpFlagMatchEntry.class, tcpFlagBuilder.build());
        return matchBuilder.build();
    }

    public static MatchEntries toNxmIpv4Tunnel(Class<? extends MatchField> field, Ipv4Prefix ipv4Prefix) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(Nxm1Class.class);
        matchEntriesBuilder.setOxmMatchField(field);
        boolean hasMask = addNxmIpv4PrefixAugmentation(matchEntriesBuilder, ipv4Prefix);
        matchEntriesBuilder.setHasMask(hasMask);
        return matchEntriesBuilder.build();
    }

    static void addNxmMaskAugmentation(final MatchEntriesBuilder builder, final byte[] mask) {
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(mask);
        builder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
    }

    /**
     * @return true if Ipv4Prefix contains prefix (and it is used in mask),
     *         false otherwise
     */
    static boolean addNxmIpv4PrefixAugmentation(final MatchEntriesBuilder builder, final Ipv4Prefix address) {
        boolean hasMask = false;
        Iterator<String> addressParts = PREFIX_SPLITTER.split(address.getValue()).iterator();
        Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());

        final int prefix;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        } else {
            prefix = 0;
        }

        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(ipv4Address);
        builder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        if (prefix != 0) {
            int mask = 0xffffffff << (32 - prefix);
            byte[] maskBytes = new byte[] { (byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8),
                    (byte) mask };
            addNxmMaskAugmentation(builder, maskBytes);
            hasMask = true;
        }
        return hasMask;
    }
}
