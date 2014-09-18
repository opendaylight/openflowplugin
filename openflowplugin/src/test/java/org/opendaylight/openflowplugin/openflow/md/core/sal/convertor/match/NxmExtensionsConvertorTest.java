/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcpFlagMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/17/14.
 */
public class NxmExtensionsConvertorTest {


    private static final Integer TCP_FLAG = new Integer(42);
    private static final Ipv4Prefix IPV_4_PREFIX = new Ipv4Prefix("10.0.0.1/24");
    private static final Ipv4Prefix IPV_4_PREFIX_NO_MASK = new Ipv4Prefix("10.0.0.1");

    @Test
    /**
     * Trivial test method for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.NxmExtensionsConvertor#toNxmTcpFlag(Integer)}  }
     */
    public void testToNxmTcpFlag() {
        MatchEntries matchEntries = NxmExtensionsConvertor.toNxmTcpFlag(TCP_FLAG);
        assertNotNull(matchEntries.getAugmentation(TcpFlagMatchEntry.class));
        assertFalse(matchEntries.isHasMask());
        assertEquals(Nxm1Class.class, matchEntries.getOxmClass());
        assertEquals(TcpFlag.class, matchEntries.getOxmMatchField());
    }

    @Test
    /**
     * Trivial test method for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.NxmExtensionsConvertor#toNxmIpv4Tunnel(Class, org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix)}  }
     */
    public void testToNxmIpv4Tunnel() {
        MatchEntries matchEntries = NxmExtensionsConvertor.toNxmIpv4Tunnel(MockMatchField.class, IPV_4_PREFIX);
        assertNotNull(matchEntries.getAugmentation(MaskMatchEntry.class));
        assertNotNull(matchEntries.getAugmentation(Ipv4AddressMatchEntry.class));
        assertTrue(matchEntries.isHasMask());
        assertEquals(Nxm1Class.class, matchEntries.getOxmClass());
        assertEquals(MockMatchField.class, matchEntries.getOxmMatchField());
    }

    @Test
    /**
     * Trivial test method for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.NxmExtensionsConvertor#addNxmIpv4PrefixAugmentation(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder, org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix)}  }
     */
    public void testAddNxmIpv4PrefixAugmentation() {
        assertTrue(NxmExtensionsConvertor.addNxmIpv4PrefixAugmentation(new MatchEntriesBuilder(), IPV_4_PREFIX));
        assertFalse(NxmExtensionsConvertor.addNxmIpv4PrefixAugmentation(new MatchEntriesBuilder(), IPV_4_PREFIX_NO_MASK));
    }


    @Test
    /**
     * Trivial test method for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.NxmExtensionsConvertor#addNxmMaskAugmentation(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder, byte[])}  }
     */
    public void testAddNxmMaskAugmentation() {
        MatchEntriesBuilder matchEntryBuilder = new MatchEntriesBuilder();
        byte[] mask = new byte[0];
        assertNull(matchEntryBuilder.getAugmentation(MaskMatchEntry.class));
        NxmExtensionsConvertor.addNxmMaskAugmentation(matchEntryBuilder, mask);
        assertNotNull(matchEntryBuilder.getAugmentation(MaskMatchEntry.class));
    }


    private class MockMatchField extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField {

    }
}
