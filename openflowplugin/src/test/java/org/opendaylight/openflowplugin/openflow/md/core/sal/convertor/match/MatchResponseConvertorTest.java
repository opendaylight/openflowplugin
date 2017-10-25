/*
 * Copyright (c) 2014 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.type._case.EthTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;

/**
 * Unit test for {@link MatchConvertorImpl}.
 */
public class MatchResponseConvertorTest {
    private static final BigInteger DPID = BigInteger.TEN;
    private static final Long IN_PORT = 6L;
    private static final String URI_IN_PORT =
            "openflow:" + DPID + ":" + IN_PORT;
    private static final MacAddress MAC_SRC =
            MacAddress.getDefaultInstance("00:11:22:33:44:55");
    private static final MacAddress MAC_DST =
            MacAddress.getDefaultInstance("fa:fb:fc:fd:fe:ff");
    private static final int ETHTYPE_IPV4 = 0x800;
    private static final short VLAN_PCP = 7;
    private static final Ipv4Address IPV4_SRC =
            Ipv4Address.getDefaultInstance("192.168.10.254");
    private static final Ipv4Address IPV4_DST =
            Ipv4Address.getDefaultInstance("10.1.2.3");

    private static final int DL_VLAN_NONE = 0xffff;
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchEntriesGrouping, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testFromOFMatchToSALMatch() {
        List<MatchEntry> entries = createDefaultMatchEntry();

        int[] vids = {
                // Match packet with VLAN tag regardless of its value.
                -1,

                // Match untagged frame.
                0,

                // Match packet with VLAN tag and VID equals the specified value.
                1, 20, 4095,
        };

        for (int vid : vids) {
            List<MatchEntry> MatchEntry =
                    new ArrayList<MatchEntry>(entries);
            MatchEntry.add(toOfVlanVid(vid));
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match ofMatch =
                    createOFMatch(MatchEntry);

            final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
            data.setDatapathId(DPID);
            final MatchBuilder builder = convert(ofMatch, data);
            checkDefault(builder);
            VlanMatch vlanMatch = builder.getVlanMatch();
            int expectedVid = (vid < 0) ? 0 : vid;
            Boolean expectedCfi = vid != 0;
            assertEquals(expectedVid, vlanMatch.getVlanId().getVlanId().
                    getValue().intValue());
            assertEquals(expectedCfi, vlanMatch.getVlanId().isVlanIdPresent());
            assertEquals(null, vlanMatch.getVlanPcp());
        }
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)} }.
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
        for (int vid : vids) {
            for (short dscp : dscps) {
                short tos = (short) (dscp << 2);
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
                final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
                data.setDatapathId(DPID);
                Match match = convert(ofMatch, data).build();
                checkDefaultV10(match, wc, vid);

                IpMatch ipMatch = match.getIpMatch();
                assertEquals(null, ipMatch.getIpProtocol());
                assertEquals(dscp, ipMatch.getIpDscp().getValue().shortValue());
                assertEquals(null, ipMatch.getIpEcn());

                // Set all wildcard bits.
                wc = wcBuilder.setAll(true).build();
                ofMatch = builder.setWildcards(wc).build();
                match = convert(ofMatch, data).build();
                checkDefaultV10(match, wc, vid);
                assertEquals(null, match.getIpMatch());
            }
        }
    }

    private static void checkDefault(final MatchBuilder builder) {
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

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match createOFMatch(final List<MatchEntry> entries) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder builder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder();
        return builder.setType(OxmMatchType.class).setMatchEntry(entries).
                build();
    }

    private static List<MatchEntry> createDefaultMatchEntry() {
        List<MatchEntry> entries = new ArrayList<>();
        entries.add(toOfPort(InPort.class, IN_PORT));

        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();

        EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
        ethSrcBuilder.setMacAddress(MAC_SRC);
        ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(EthSrc.class);
        entries.add(matchEntryBuilder.build());


        EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
        EthDstBuilder ethDstBuilder = new EthDstBuilder();
        ethDstBuilder.setMacAddress(MAC_DST);
        ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(EthDst.class);
        entries.add(matchEntryBuilder.build());

        entries.add(toOfEthernetType(ETHTYPE_IPV4));
        return entries;
    }

    private static MatchEntry toOfEthernetType(final int ethType) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setHasMask(false);
        builder.setOxmMatchField(EthType.class);
        EthTypeCaseBuilder ethTypeCaseBuilder = new EthTypeCaseBuilder();
        EthTypeBuilder ethTypeBuilder = new EthTypeBuilder();
        EtherType etherType = new EtherType(ethType);
        ethTypeBuilder.setEthType(etherType);
        ethTypeCaseBuilder.setEthType(ethTypeBuilder.build());
        builder.setMatchEntryValue(ethTypeCaseBuilder.build());
        return builder.build();
    }

    private static MatchEntry toOfPort(final Class<? extends MatchField> field,
                                final Long portNumber) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setHasMask(false);
        builder.setOxmMatchField(field);
        InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
        InPortBuilder portBuilder = new InPortBuilder();
        portBuilder.setPortNumber(new PortNumber(portNumber));
        inPortCaseBuilder.setInPort(portBuilder.build());
        builder.setMatchEntryValue(inPortCaseBuilder.build());
        return builder.build();
    }

    private static MatchEntry toOfVlanVid(final int vid) {
        MatchEntryBuilder builder = new MatchEntryBuilder();
        boolean cfi = true;
        Integer vidValue = vid;
        byte[] mask = null;
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(VlanVid.class);
        VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
        if (vid == 0) {
            // Match untagged frame.
            cfi = false;
        } else if (vid < 0) {
            // Match packet with VLAN tag regardless of its value.
            mask = new byte[]{0x10, 0x00};
            vidValue = 0;
        }

        VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
        vlanVidBuilder.setCfiBit(cfi);
        vlanVidBuilder.setVlanVid(vidValue);
        boolean hasMask = mask != null;
        if (hasMask) {
            vlanVidBuilder.setMask(mask);
        }
        vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
        builder.setHasMask(hasMask);
        builder.setMatchEntryValue(vlanVidCaseBuilder.build());
        return builder.build();
    }

    private static void checkDefaultV10(final Match match, final FlowWildcardsV10 wc, final int vid) {
        EthernetMatch ethMatch = match.getEthernetMatch();
        if (wc.isDLSRC()) {
            if (ethMatch != null) {
                assertEquals(null, ethMatch.getEthernetSource());
            }
        } else {
            assertEquals(MAC_SRC, ethMatch.getEthernetSource().getAddress());
        }

        if (ethMatch != null) {
            if (wc.isDLDST()) {
                assertEquals(null, ethMatch.getEthernetDestination());
            } else {
                assertNotEquals(null, ethMatch.getEthernetDestination());
                assertEquals(MAC_DST,
                        ethMatch.getEthernetDestination().getAddress());
            }
        }

        if (wc.isDLTYPE()) {
            if (ethMatch != null) {
                assertEquals(null, ethMatch.getEthernetType());
            }
            assertEquals(null, match.getLayer3Match());
        } else {
            assert ethMatch != null;
            assertEquals(ETHTYPE_IPV4, ethMatch.getEthernetType().getType().
                    getValue().intValue());

            Ipv4Match ipv4Match = (Ipv4Match) match.getLayer3Match();
            assertEquals(IPV4_SRC.getValue() + "/32",
                    ipv4Match.getIpv4Source().getValue());
            assertEquals(IPV4_DST.getValue() + "/32",
                    ipv4Match.getIpv4Destination().getValue());
        }

        VlanMatch vlanMatch = match.getVlanMatch();
        if (wc.isDLVLAN()) {
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

            if (wc.isDLVLANPCP()) {
                assertEquals(null, vlanMatch.getVlanPcp());
            } else {
                assertEquals(VLAN_PCP,
                        vlanMatch.getVlanPcp().getValue().shortValue());
            }
        }
    }

    private MatchBuilder convert(MatchV10 match, VersionDatapathIdConvertorData data) {
        final Optional<MatchBuilder> salMatchOptional = convertorManager.convert(match, data);

        return salMatchOptional.orElse(new MatchBuilder());
    }

    private MatchBuilder convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match match, VersionDatapathIdConvertorData data) {
        final Optional<MatchBuilder> salMatchOptional = convertorManager.convert(match, data);

        return salMatchOptional.orElse(new MatchBuilder());
    }
}
