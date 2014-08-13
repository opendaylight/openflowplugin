/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

import static org.junit.Assert.assertEquals;

public class FlowRemovedTranslatorTest extends FlowRemovedTranslator {

    @Test
    public void MatchEntryToIpv4PrefixTest() {
        Ipv4AddressMatchEntry ipv4AddressMatchEntry = new Ipv4AddressMatchEntryBuilder()
                .setIpv4Address(new Ipv4Address("10.0.0.0")).build();
        byte[] maskBytes = new byte[1];
        maskBytes[0] = (byte) 255;
        MaskMatchEntry maskMatchEntry = new MaskMatchEntryBuilder().setMask(maskBytes).build();
        MatchEntries entry = new MatchEntriesBuilder().setOxmMatchField(Ipv4Src.class)
                .addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressMatchEntry)
                .addAugmentation(MaskMatchEntry.class, maskMatchEntry).setHasMask(true).build();
        Ipv4Prefix ipv4Prefix = toIpv4Prefix(entry);
        assertEquals("10.0.0.0/8", ipv4Prefix.getValue());
    }

    @Test
    public void MatchEntryToIpv6PrefixTest() {
        Ipv6AddressMatchEntry ipv6AddressMatchEntry = new Ipv6AddressMatchEntryBuilder()
                .setIpv6Address(new Ipv6Address("1234:5678:9ABC:DEF0:FDCD:A987:6543:0")).build();
        byte[] maskBytes = new byte[1];
        maskBytes[0] = (byte) 255;
        MaskMatchEntry maskMatchEntry = new MaskMatchEntryBuilder().setMask(maskBytes).build();
        MatchEntries entry = new MatchEntriesBuilder().setOxmMatchField(Ipv6Src.class)
                .addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressMatchEntry)
                .addAugmentation(MaskMatchEntry.class, maskMatchEntry).setHasMask(true).build();
        Ipv6Prefix ipv6Prefix = toIpv6Prefix(entry);
        assertEquals("1234:5678:9ABC:DEF0:FDCD:A987:6543:0/8", ipv6Prefix.getValue());
    }
}
