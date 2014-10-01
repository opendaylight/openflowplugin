/*
 * Copyright (c) 2014 NEC Corporation and others.  All rights reserved.
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

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10Builder;
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
    private static final short  VLAN_PCP = 7;
    private static final Ipv4Address  IPV4_SRC =
        Ipv4Address.getDefaultInstance("192.168.10.254");
    private static final Ipv4Address  IPV4_DST =
        Ipv4Address.getDefaultInstance("10.1.2.3");

    private static final int  DL_VLAN_NONE = 0xffff;

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

    /**
     * Test method for {@link MatchConvertorImpl#fromOFMatchV10ToSALMatch(MatchV10, BigInteger, OpenflowVersion)}.
     */
    @Test
    public void testFromOFMatchV10ToSALMatch() {
        int[] vids = {
            // Match untagged frame.
            DL_VLAN_NONE,

            // Match packet with VLAN tag and VID equals the specified value.
            1, 20, 4095,
        };
        short[] dscps = {
            0, 1, 20, 40, 62, 63,
        };

        FlowWildcardsV10Builder wcBuilder = new FlowWildcardsV10Builder();
        for (int vid: vids) {
            for (short dscp: dscps) {
                short tos = (short)(dscp << 2);
                MatchV10Builder builder = new MatchV10Builder();
                builder.setDlSrc(MAC_SRC).setDlDst(MAC_DST).setDlVlan(vid).
                    setDlVlanPcp(VLAN_PCP).setDlType(ETHTYPE_IPV4).
                    setInPort(IN_PORT.intValue()).
                    setNwSrc(IPV4_SRC).setNwDst(IPV4_DST).setNwTos(tos);
                wcBuilder.setAll(false).setNwProto(true).setTpSrc(true).
                    setTpDst(true);
                if (vid == DL_VLAN_NONE) {
                    wcBuilder.setDlVlanPcp(true);
                }

                FlowWildcardsV10 wc = wcBuilder.build();
                MatchV10 ofMatch = builder.setWildcards(wc).build();
                Match match = MatchConvertorImpl.fromOFMatchV10ToSALMatch(
                    ofMatch, DPID, OpenflowVersion.OF10);
                checkDefaultV10(match, wc, vid);

                IpMatch ipMatch = match.getIpMatch();
                assertEquals(null, ipMatch.getIpProtocol());
                assertEquals(dscp, ipMatch.getIpDscp().getValue().shortValue());
                assertEquals(null, ipMatch.getIpEcn());

                // Set all wildcard bits.
                wc = wcBuilder.setAll(true).build();
                ofMatch = builder.setWildcards(wc).build();
                match = MatchConvertorImpl.fromOFMatchV10ToSALMatch(
                    ofMatch, DPID, OpenflowVersion.OF10);
                checkDefaultV10(match, wc, vid);
                assertEquals(null, match.getIpMatch());
            }
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

    private void checkDefaultV10(Match match, FlowWildcardsV10 wc, int vid) {
        EthernetMatch ethMatch = match.getEthernetMatch();
        if (wc.isDLSRC().booleanValue()) {
            if (ethMatch != null) {
                assertEquals(null, ethMatch.getEthernetSource());
            }
        } else {
            assertEquals(MAC_SRC, ethMatch.getEthernetSource().getAddress());
        }

        if (wc.isDLDST().booleanValue()) {
            if (ethMatch != null) {
                assertEquals(null, ethMatch.getEthernetDestination());
            }
        } else {
            assertEquals(MAC_DST,
                         ethMatch.getEthernetDestination().getAddress());
        }

        if (wc.isDLTYPE().booleanValue()) {
            if (ethMatch != null) {
                assertEquals(null, ethMatch.getEthernetType());
            }
            assertEquals(null, match.getLayer3Match());
        } else {
            assertEquals(ETHTYPE_IPV4, ethMatch.getEthernetType().getType().
                         getValue().intValue());

            Ipv4Match ipv4Match = (Ipv4Match)match.getLayer3Match();
            assertEquals(IPV4_SRC.getValue(),
                         ipv4Match.getIpv4Source().getValue());
            assertEquals(IPV4_DST.getValue(),
                         ipv4Match.getIpv4Destination().getValue());
        }

        VlanMatch vlanMatch = match.getVlanMatch();
        if (wc.isDLVLAN().booleanValue()) {
            assertEquals(null, vlanMatch);
        } else {
            int expectedVid;
            Boolean expectedCfi;
            if (vid == DL_VLAN_NONE) {
                expectedVid = 0;
                expectedCfi = Boolean.FALSE;
            } else {
                expectedVid = vid;
                expectedCfi = Boolean.TRUE;
            }
            assertEquals(expectedVid, vlanMatch.getVlanId().getVlanId().
                         getValue().intValue());
            assertEquals(expectedCfi, vlanMatch.getVlanId().isVlanIdPresent());

            if (wc.isDLVLANPCP().booleanValue()) {
                assertEquals(null, vlanMatch.getVlanPcp());
            } else {
                assertEquals(VLAN_PCP,
                             vlanMatch.getVlanPcp().getValue().shortValue());
            }
        }
    }
}
