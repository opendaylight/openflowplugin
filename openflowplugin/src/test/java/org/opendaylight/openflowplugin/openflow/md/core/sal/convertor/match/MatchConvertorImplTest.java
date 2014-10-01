/*
 * Copyright (c) 2014 NEC Corporation, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026
.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

/**
 * Unit test for {@link MatchConvertorImpl}.
 */
public class MatchConvertorImplTest {
    private static final BigInteger  DPID = BigInteger.TEN;
    private static final Long  IN_PORT = Long.valueOf(6);
    private static final String  URI_IN_PORT =
        "openflow:" + DPID + ":" + IN_PORT;
    private static final MacAddress MAC_SRC =
        MacAddress.getDefaultInstance("00:11:22:33:44:55");
    private static final MacAddress MAC_DST =
        MacAddress.getDefaultInstance("fa:fb:fc:fd:fe:ff");
    private static final int  ETHTYPE_IPV4 = 0x800;

    @BeforeClass
    public static void setUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test method for {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match, BigInteger, OpenflowVersion)}.
     */
    @Test
    public void testFromOFMatchToSALMatch() {
        List<MatchEntries> entries = createDefaultMatchEntries();

        int[] vids = {
            // Match packet with VLAN tag regardless of its value.
            -1,

            // Match untagged frame.
            0,

            // Match packet with VLAN tag and VID equals the specified value.
            1, 20, 4095,
        };

        for (int vid: vids) {
            List<MatchEntries> matchEntries =
                new ArrayList<MatchEntries>(entries);
            matchEntries.add(toOfVlanVid(vid));
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match ofMatch =
                createOFMatch(matchEntries);

            MatchBuilder builder = MatchConvertorImpl.
                fromOFMatchToSALMatch(ofMatch, DPID, OpenflowVersion.OF13);
            checkDefault(builder);
            VlanMatch vlanMatch = builder.getVlanMatch();
            int expectedVid = (vid < 0) ? 0 : vid;
            Boolean expectedCfi = Boolean.valueOf(vid != 0);
            assertEquals(expectedVid, vlanMatch.getVlanId().getVlanId().
                         getValue().intValue());
            assertEquals(expectedCfi, vlanMatch.getVlanId().isVlanIdPresent());
            assertEquals(null, vlanMatch.getVlanPcp());
        }
    }

    private void checkDefault(MatchBuilder builder) {
        EthernetMatch ethMatch = builder.getEthernetMatch();
        assertEquals(MAC_SRC, ethMatch.getEthernetSource().getAddress());
        assertEquals(null, ethMatch.getEthernetSource().getMask());
        assertEquals(MAC_DST, ethMatch.getEthernetDestination().getAddress());
        assertEquals(null, ethMatch.getEthernetDestination().getMask());
        assertEquals(ETHTYPE_IPV4, ethMatch.getEthernetType().getType().
                     getValue().intValue());

        NodeConnectorId inPort = builder.getInPort();
        assertEquals(URI_IN_PORT, inPort.getValue());
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match createOFMatch(List<MatchEntries> entries) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder builder =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder();
        return builder.setType(OxmMatchType.class).setMatchEntries(entries).
            build();
    }

    private List<MatchEntries> createDefaultMatchEntries() {
        List<MatchEntries> entries = new ArrayList<MatchEntries>();
        entries.add(toOfPort(InPort.class, IN_PORT));
        entries.add(MatchConvertorImpl.toOfMacAddress(
                        EthSrc.class, MAC_SRC, null));
        entries.add(MatchConvertorImpl.toOfMacAddress(
                        EthDst.class, MAC_DST, null));
        entries.add(toOfEthernetType(ETHTYPE_IPV4));
        return entries;
    }

    private MatchEntries toOfEthernetType(int ethType) {
        MatchEntriesBuilder builder = new MatchEntriesBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setHasMask(false);
        builder.setOxmMatchField(EthType.class);
        EthTypeMatchEntryBuilder ethTypeBuilder =
            new EthTypeMatchEntryBuilder();
        ethTypeBuilder.setEthType(new EtherType(ethType));
        builder.addAugmentation(EthTypeMatchEntry.class,
                                ethTypeBuilder.build());
        return builder.build();
    }

    private MatchEntries toOfPort(Class<? extends MatchField> field,
                                  Long portNumber) {
        MatchEntriesBuilder builder = new MatchEntriesBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setHasMask(false);
        builder.setOxmMatchField(field);
        PortNumberMatchEntryBuilder portBuilder =
            new PortNumberMatchEntryBuilder();
        portBuilder.setPortNumber(new PortNumber(portNumber));
        builder.addAugmentation(PortNumberMatchEntry.class,
                                            portBuilder.build());
        return builder.build();
    }

    private MatchEntries toOfVlanVid(int vid) {
        MatchEntriesBuilder builder = new MatchEntriesBuilder();
        boolean cfi = true;
        Integer vidValue = Integer.valueOf(vid);
        byte[] mask = null;
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(VlanVid.class);
        VlanVidMatchEntryBuilder vidBuilder = new VlanVidMatchEntryBuilder();
        if (vid == 0) {
            // Match untagged frame.
            cfi = false;
        } else if (vid < 0) {
            // Match packet with VLAN tag regardless of its value.
            mask = new byte[]{0x10, 0x00};
            vidValue = Integer.valueOf(0);
        }

        vidBuilder.setCfiBit(cfi);
        vidBuilder.setVlanVid(vidValue);
        builder.addAugmentation(VlanVidMatchEntry.class, vidBuilder.build());
        boolean hasMask = mask != null;
        if (hasMask) {
            addMaskAugmentation(builder, mask);
        }
        builder.setHasMask(hasMask);
        return builder.build();
    }

    private void addMaskAugmentation(MatchEntriesBuilder builder, byte[] mask) {
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(mask);
        builder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
    }
}
