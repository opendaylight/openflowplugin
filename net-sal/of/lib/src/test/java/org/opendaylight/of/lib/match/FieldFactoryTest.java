/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.msg.OfmTest;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.net.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.FieldFactory.parseField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for match fields.
 *
 * @author Simon Hunt
 */
public class FieldFactoryTest extends OfmTest {
    private static final String E_PARSE_FAIL = "failed to parse match field";
    private static final String FILE_PREFIX = "match/v13/field";
    private static final String MASKED = "Masked";

    private static final String MF_EXPER_HP= "ExperHp";
    private static final String MF_CLAZZ_HP = "ClazzHp";
    private static final String MF_CLAZZ_UNKNOWN = "ClazzUnknown";

    private static final int OFBASIC_CODE = 0x8000;

    private OfPacketReader pkt;
    private MatchField mf;

    private OfPacketReader getPkt(String basename) {
        return getPacketReader(FILE_PREFIX + basename + HEX);
    }

    private OfPacketReader getPkt(OxmBasicFieldType field) {
        return getPkt(field, "");
    }

    private OfPacketReader getPkt(OxmBasicFieldType field, String suffix) {
        String basename = StringUtils.toCamelCase(FILE_PREFIX, field);
        return getPacketReader(basename + suffix + HEX);
    }

    private OfPacketReader getPktMasked(OxmBasicFieldType field) {
        String basename = StringUtils.toCamelCase(FILE_PREFIX, field);
        return getPacketReader(basename + MASKED + HEX);
    }


    private void verify(MatchField mf,
                        int rawClz, OxmClass clz,
                        int fv, OxmFieldType ft, boolean mask, int len) {

        assertEquals(AM_NEQ, rawClz, mf.getRawOxmClass());
        assertEquals(AM_NEQ, clz, mf.getOxmClass());
        assertEquals(AM_NEQ, fv, mf.getRawFieldType());
        assertEquals(AM_NEQ, ft, mf.getFieldType());
        assertEquals(AM_NEQ, mask, mf.hasMask());
        assertEquals(AM_NEQ, len, mf.getPayloadLength());

        // generate the combined OxmType code
        // <------16 bits--><---7 bits--><-1 bit->
        // [  OXM Class    ][ OXM field ][hasMask]       (hasMask == 0)
        int oxmType = ((rawClz << 8) | (fv << 1));
        assertEquals(AM_NEQ, oxmType, mf.header.rawOxmType); // no getter!
    }

    private void verify(MatchField mf, int fv, OxmBasicFieldType ft,
                        boolean mask, int len) {
        verify(mf, OFBASIC_CODE, OxmClass.OPENFLOW_BASIC, fv, ft, mask, len);
    }


    private static final BigPortNumber EXP_IN_PORT = BigPortNumber.valueOf(15);
    private static final BigPortNumber EXP_IN_PHY_PORT =
            BigPortNumber.valueOf(259);

    @Test
    public void inPort() {
        print(EOL + "inPort()");
        pkt = getPkt(IN_PORT);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldInPort.hex for expected values
            verify(mf, 0, IN_PORT, false, 4);

            assertTrue(AM_WRCL, mf instanceof MfbInPort);
            MfbInPort mfi = (MfbInPort) mf;
            assertEquals(AM_NEQ, EXP_IN_PORT, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void inPhyPort() {
        print(EOL + "inPhyPort()");
        pkt = getPkt(IN_PHY_PORT);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldInPort.hex for expected values
            verify(mf, 1, IN_PHY_PORT, false, 4);

            assertTrue(AM_WRCL, mf instanceof MfbInPhyPort);
            MfbInPhyPort mfi = (MfbInPhyPort) mf;
            assertEquals(AM_NEQ, EXP_IN_PHY_PORT, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int B = 256;

    private static final long META_LONG_V = 0xf1f2f3f4f5f6f7f8L;
    private static final long META_LONG_M = 0xff00ff00ff00ff00L;

    @Test
    public void metadata() {
        print(EOL + "metadata()");
        pkt = getPkt(METADATA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldMetadata.hex for expected values
            verify(mf, 2, METADATA, false, 8);

            assertTrue(AM_WRCL, mf instanceof MfbMetadata);
            MfbMetadata mfi = (MfbMetadata) mf;
            assertEquals(AM_NEQ, META_LONG_V, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void metadataMasked() {
        print(EOL + "metadataMasked()");
        pkt = getPktMasked(METADATA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldMetadata.hex for expected values
            verify(mf, 2, METADATA, true, 16);

            assertTrue(AM_WRCL, mf instanceof MfbMetadata);
            MfbMetadata mfi = (MfbMetadata) mf;
            assertEquals(AM_NEQ, META_LONG_V, mfi.getValue());
            assertEquals(AM_NEQ, META_LONG_M, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final MacAddress ETH_MAC = mac("66:55:44:33:22:11");
    private static final MacAddress ETH_MAC_MASK = mac("ff:ff:ff:00:00:01");

    @Test
    public void ethDst() {
        print(EOL + "ethDst()");
        pkt = getPkt(ETH_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldEthDst.hex for expected values
            verify(mf, 3, ETH_DST, false, 6);

            assertTrue(AM_WRCL, mf instanceof MfbEthDst);
            MfbEthDst mfi = (MfbEthDst) mf;
            assertEquals(AM_NEQ, ETH_MAC, mfi.getMacAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ethDstMasked() {
        print(EOL + "ethDstMasked()");
        pkt = getPktMasked(ETH_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldEthDstMasked.hex for expected values
            verify(mf, 3, ETH_DST, true, 12);

            assertTrue(AM_WRCL, mf instanceof MfbEthDst);
            MfbEthDst mfi = (MfbEthDst) mf;
            assertEquals(AM_NEQ, ETH_MAC, mfi.getMacAddress());
            assertEquals(AM_NEQ, ETH_MAC_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ethSrc() {
        print(EOL + "ethSrc()");
        pkt = getPkt(ETH_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldEthSrc.hex for expected values
            verify(mf, 4, ETH_SRC, false, 6);

            assertTrue(AM_WRCL, mf instanceof MfbEthSrc);
            MfbEthSrc mfi = (MfbEthSrc) mf;
            assertEquals(AM_NEQ, ETH_MAC, mfi.getMacAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ethSrcMasked() {
        print(EOL + "ethSrcMasked()");
        pkt = getPktMasked(ETH_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldEthDst.hex for expected values
            verify(mf, 4, ETH_SRC, true, 12);

            assertTrue(AM_WRCL, mf instanceof MfbEthSrc);
            MfbEthSrc mfi = (MfbEthSrc) mf;
            assertEquals(AM_NEQ, ETH_MAC, mfi.getMacAddress());
            assertEquals(AM_NEQ, ETH_MAC_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ethType() {
        print(EOL + "ethType()");
        pkt = getPkt(ETH_TYPE);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldEthType.hex for expected values
            verify(mf, 5, ETH_TYPE, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbEthType);
            MfbEthType mfi = (MfbEthType) mf;
            assertEquals(AM_NEQ, EthernetType.ARP, mfi.getEthernetType());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void vlanVidNone() {
        print(EOL + "vlanVidNone()");
        pkt = getPkt(VLAN_VID, "None");
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldVlanVidNone.hex for expected values
            verify(mf, 6, VLAN_VID, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbVlanVid);
            MfbVlanVid mfi = (MfbVlanVid) mf;
            assertEquals(AM_NEQ, VlanId.NONE, mfi.getVlanId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void vlanVidPresent() {
        print(EOL + "vlanVidPresent()");
        pkt = getPkt(VLAN_VID, "Present");
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldVlanVidPresent.hex for expected values
            verify(mf, 6, VLAN_VID, true, 4);

            assertTrue(AM_WRCL, mf instanceof MfbVlanVid);
            MfbVlanVid mfi = (MfbVlanVid) mf;
//            assertEquals(AM_NEQ, OxmVlanId.PRESENT, mfi.getMode());
            assertEquals(AM_NEQ, VlanId.PRESENT, mfi.getVlanId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void vlanVidExact() {
        print(EOL + "vlanVidExact()");
        pkt = getPkt(VLAN_VID, "Exact");
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldVlanVidExact.hex for expected values
            verify(mf, 6, VLAN_VID, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbVlanVid);
            MfbVlanVid mfi = (MfbVlanVid) mf;
            assertEquals(AM_NEQ, VLAN_ID_42, mfi.getVlanId());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void vlanVidExactCreateThenParse() throws MessageParseException {
        print(EOL + "vlanVidExactCreateThenParse()");
        MatchField mf = createBasicField(V_1_3, VLAN_VID, VLAN_ID_42);
        print("Orig: {}", mf);

        OfPacketWriter pkt = new OfPacketWriter(mf.getTotalLength());
        FieldFactory.encodeField(mf, pkt);
        byte[] bytes = pkt.array();
        print("Bytes: [{}]", ByteUtils.hex(bytes));

        OfPacketReader rdr = new OfPacketReader(bytes);
        MatchField mfCopy = FieldFactory.parseField(rdr, V_1_3);
        print("Copy: {}", mfCopy);
        assertEquals(AM_NEQ, mf, mfCopy);

        pkt = new OfPacketWriter(mfCopy.getTotalLength());
        FieldFactory.encodeField(mfCopy, pkt);
        byte[] copyBytes = pkt.array();
        print("Copy Bytes: [{}]", ByteUtils.hex(copyBytes));
        assertArrayEquals(AM_NEQ, bytes, copyBytes);
    }

    private static final VlanId VLAN_ID_42 = VlanId.valueOf(42);

    @Test
    public void vlanVidPresentBitNotSet() {
        print(EOL + "vlanVidPresentBitNotSet()");
        pkt = getPkt(VLAN_VID, "NoPresent");
        try {
            mf =  parseField(pkt, V_1_3);
            fail(AM_NOEX);

        } catch (MessageParseException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG,
                    e.getMessage().endsWith(FieldFactory.E_VLAN_NO_PRESENT));
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void vlanPcp() {
        print(EOL + "vlanPcp()");
        pkt = getPkt(VLAN_PCP);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldVlanPcp.hex for expected values
            verify(mf, 7, VLAN_PCP, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbVlanPcp);
            MfbVlanPcp mfi = (MfbVlanPcp) mf;
            assertEquals(AM_NEQ, EXP_VLAN_PCP, mfi.getValue());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_VLAN_PCP = 5;

    @Test
    public void ipDscp() {
        print(EOL + "ipDscp()");
        pkt = getPkt(IP_DSCP);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpDscp.hex for expected values
            verify(mf, 8, IP_DSCP, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIpDscp);
            MfbIpDscp mfi = (MfbIpDscp) mf;
            assertEquals(AM_NEQ, EXP_IP_DSCP, mfi.getValue());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_IP_DSCP = 33;

    @Test
    public void ipEcn() {
        print(EOL + "ipEcn()");
        pkt = getPkt(IP_ECN);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpEcn.hex for expected values
            verify(mf, 9, IP_ECN, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIpEcn);
            MfbIpEcn mfi = (MfbIpEcn) mf;
            assertEquals(AM_NEQ, EXP_IP_ECN, mfi.getValue());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_IP_ECN = 3;

    @Test
    public void ipProto() {
        print(EOL + "ipProto()");
        pkt = getPkt(IP_PROTO);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpProto.hex for expected values
            verify(mf, 10, IP_PROTO, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIpProto);
            MfbIpProto mfi = (MfbIpProto) mf;
            assertEquals(AM_NEQ, IpProtocol.UDP, mfi.getIpProtocol());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final IpAddress IP4 = IpAddress.valueOf("15.255.125.36");
    private static final IpAddress IP4_MASK = IpAddress.valueOf("255.255.248.0");

    @Test
    public void ipv4Src() {
        print(EOL + "ipv4Src()");
        pkt = getPkt(IPV4_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv4Src.hex for expected values
            verify(mf, 11, IPV4_SRC, false, 4);

            assertTrue(AM_WRCL, mf instanceof MfbIpv4Src);
            MfbIpv4Src mfi = (MfbIpv4Src) mf;
            assertEquals(AM_NEQ, IP4, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv4SrcMasked() {
        print(EOL + "ipv4SrcMasked()");
        pkt = getPktMasked(IPV4_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv4SrcMasked.hex for expected values
            verify(mf, 11, IPV4_SRC, true, 8);

            assertTrue(AM_WRCL, mf instanceof MfbIpv4Src);
            MfbIpv4Src mfi = (MfbIpv4Src) mf;
            assertEquals(AM_NEQ, IP4, mfi.getIpAddress());
            assertEquals(AM_NEQ, IP4_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }


    @Test
    public void ipv4Dst() {
        print(EOL + "ipv4Dst()");
        pkt = getPkt(IPV4_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv4Dst.hex for expected values
            verify(mf, 12, IPV4_DST, false, 4);

            assertTrue(AM_WRCL, mf instanceof MfbIpv4Dst);
            MfbIpv4Dst mfi = (MfbIpv4Dst) mf;
            assertEquals(AM_NEQ, IP4, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv4DstMasked() {
        print(EOL + "ipv4DstMasked()");
        pkt = getPktMasked(IPV4_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv4DstMasked.hex for expected values
            verify(mf, 12, IPV4_DST, true, 8);

            assertTrue(AM_WRCL, mf instanceof MfbIpv4Dst);
            MfbIpv4Dst mfi = (MfbIpv4Dst) mf;
            assertEquals(AM_NEQ, IP4, mfi.getIpAddress());
            assertEquals(AM_NEQ, IP4_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tcpSrc() {
        print(EOL + "tcpSrc()");
        pkt = getPkt(TCP_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldTcpSrc.hex for expected values
            verify(mf, 13, TCP_SRC, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbTcpSrc);
            MfbTcpSrc mfi = (MfbTcpSrc) mf;
            assertEquals(AM_NEQ, EXP_TCP_SRC, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final PortNumber EXP_TCP_SRC = PortNumber.valueOf(75);
    private static final PortNumber EXP_TCP_DST = PortNumber.valueOf(76);

    @Test
    public void tcpDst() {
        print(EOL + "tcpDst()");
        pkt = getPkt(TCP_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldTcpDst.hex for expected values
            verify(mf, 14, TCP_DST, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbTcpDst);
            MfbTcpDst mfi = (MfbTcpDst) mf;
            assertEquals(AM_NEQ, EXP_TCP_DST, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void udpSrc() {
        print(EOL + "udpSrc()");
        pkt = getPkt(UDP_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldUdpSrc.hex for expected values
            verify(mf, 15, UDP_SRC, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbUdpSrc);
            MfbUdpSrc mfi = (MfbUdpSrc) mf;
            assertEquals(AM_NEQ, EXP_UDP_SRC, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final PortNumber EXP_UDP_SRC = PortNumber.valueOf(77);
    private static final PortNumber EXP_UDP_DST = PortNumber.valueOf(78);

    @Test
    public void udpDst() {
        print(EOL + "udpDst()");
        pkt = getPkt(UDP_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldUdpDst.hex for expected values
            verify(mf, 16, UDP_DST, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbUdpDst);
            MfbUdpDst mfi = (MfbUdpDst) mf;
            assertEquals(AM_NEQ, EXP_UDP_DST, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void sctpSrc() {
        print(EOL + "sctpSrc()");
        pkt = getPkt(SCTP_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldSctpSrc.hex for expected values
            verify(mf, 17, SCTP_SRC, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbSctpSrc);
            MfbSctpSrc mfi = (MfbSctpSrc) mf;
            assertEquals(AM_NEQ, EXP_SCTP_SRC, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final PortNumber EXP_SCTP_SRC = PortNumber.valueOf(79);
    private static final PortNumber EXP_SCTP_DST = PortNumber.valueOf(80);

    @Test
    public void sctpDst() {
        print(EOL + "sctpDst()");
        pkt = getPkt(SCTP_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldSctpDst.hex for expected values
            verify(mf, 18, SCTP_DST, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbSctpDst);
            MfbSctpDst mfi = (MfbSctpDst) mf;
            assertEquals(AM_NEQ, EXP_SCTP_DST, mfi.getPort());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void icmpv4Type() {
        print(EOL + "icmpv4Type()");
        pkt = getPkt(ICMPV4_TYPE);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIcmpv4Type.hex for expected values
            verify(mf, 19, ICMPV4_TYPE, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIcmpv4Type);
            MfbIcmpv4Type mfi = (MfbIcmpv4Type) mf;
            assertEquals(AM_NEQ, EXP_ICMP4TYPE, mfi.getICMPv4Type());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final ICMPv4Type EXP_ICMP4TYPE = ICMPv4Type.valueOf(5);

    @Test
    public void icmpv4Code() {
        print(EOL + "icmpv4Code()");
        pkt = getPkt(ICMPV4_CODE);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIcmpv4Code.hex for expected values
            verify(mf, 20, ICMPV4_CODE, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIcmpv4Code);
            MfbIcmpv4Code mfi = (MfbIcmpv4Code) mf;
            assertEquals(AM_NEQ, EXP_ICMP4CODE , mfi.getValue());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_ICMP4CODE = 17;

    @Test
    public void arpOp() {
        print(EOL + "arpOp()");
        pkt = getPkt(ARP_OP);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpOp.hex for expected values
            verify(mf, 21, ARP_OP, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbArpOp);
            MfbArpOp mfi = (MfbArpOp) mf;
            assertEquals(AM_NEQ, EXP_ARP_OP, mfi.getValue());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_ARP_OP = 123;

    private static final IpAddress ARP_IP = IpAddress.valueOf("15.255.124.13");
    private static final IpAddress ARP_IP_MASK = IpAddress.valueOf("255.255.0.0");

    @Test
    public void arpSpa() {
        print(EOL + "arpSpa()");
        pkt = getPkt(ARP_SPA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpSpa.hex for expected values
            verify(mf, 22, ARP_SPA, false, 4);

            assertTrue(AM_WRCL, mf instanceof MfbArpSpa);
            MfbArpSpa mfi = (MfbArpSpa) mf;
            assertEquals(AM_NEQ, ARP_IP, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void arpSpaMasked() {
        print(EOL + "arpSpaMasked()");
        pkt = getPktMasked(ARP_SPA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpSpaMasked.hex for expected values
            verify(mf, 22, ARP_SPA, true, 8);

            assertTrue(AM_WRCL, mf instanceof MfbArpSpa);
            MfbArpSpa mfi = (MfbArpSpa) mf;
            assertEquals(AM_NEQ, ARP_IP, mfi.getIpAddress());
            assertEquals(AM_NEQ, ARP_IP_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void arpTpa() {
        print(EOL + "arpTpa()");
        pkt = getPkt(ARP_TPA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpTpa.hex for expected values
            verify(mf, 23, ARP_TPA, false, 4);

            assertTrue(AM_WRCL, mf instanceof MfbArpTpa);
            MfbArpTpa mfi = (MfbArpTpa) mf;
            assertEquals(AM_NEQ, ARP_IP, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void arpTpaMasked() {
        print(EOL + "arpTpaMasked()");
        pkt = getPktMasked(ARP_TPA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpTpaMasked.hex for expected values
            verify(mf, 23, ARP_TPA, true, 8);

            assertTrue(AM_WRCL, mf instanceof MfbArpTpa);
            MfbArpTpa mfi = (MfbArpTpa) mf;
            assertEquals(AM_NEQ, ARP_IP, mfi.getIpAddress());
            assertEquals(AM_NEQ, ARP_IP_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final MacAddress ARP_MAC = mac("ab:cd:ef:12:23:34");
    private static final MacAddress ARP_MAC_MASK = mac("ff:ff:ff:ff:f0:00");

    @Test
    public void arpSha() {
        print(EOL + "arpSha()");
        pkt = getPkt(ARP_SHA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpSha.hex for expected values
            verify(mf, 24, ARP_SHA, false, 6);

            assertTrue(AM_WRCL, mf instanceof MfbArpSha);
            MfbArpSha mfi = (MfbArpSha) mf;
            assertEquals(AM_NEQ, ARP_MAC, mfi.getMacAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void arpShaMasked() {
        print(EOL + "arpShaMasked()");
        pkt = getPktMasked(ARP_SHA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpShaMasked.hex for expected values
            verify(mf, 24, ARP_SHA, true, 12);

            assertTrue(AM_WRCL, mf instanceof MfbArpSha);
            MfbArpSha mfi = (MfbArpSha) mf;
            assertEquals(AM_NEQ, ARP_MAC, mfi.getMacAddress());
            assertEquals(AM_NEQ, ARP_MAC_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void arpTha() {
        print(EOL + "arpTha()");
        pkt = getPkt(ARP_THA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpTha.hex for expected values
            verify(mf, 25, ARP_THA, false, 6);

            assertTrue(AM_WRCL, mf instanceof MfbArpTha);
            MfbArpTha mfi = (MfbArpTha) mf;
            assertEquals(AM_NEQ, ARP_MAC, mfi.getMacAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void arpThaMasked() {
        print(EOL + "arpThaMasked()");
        pkt = getPktMasked(ARP_THA);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldArpThaMasked.hex for expected values
            verify(mf, 25, ARP_THA, true, 12);

            assertTrue(AM_WRCL, mf instanceof MfbArpTha);
            MfbArpTha mfi = (MfbArpTha) mf;
            assertEquals(AM_NEQ, ARP_MAC, mfi.getMacAddress());
            assertEquals(AM_NEQ, ARP_MAC_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final IpAddress IP6_ADDR = IpAddress.valueOf("cafe::babe");
    private static final IpAddress IP6_MASK = IpAddress.valueOf("ffff:ffff:ffc0::");

    @Test
    public void ipv6Src() {
        print(EOL + "ipv6Src()");
        pkt = getPkt(IPV6_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6Src.hex for expected values
            verify(mf, 26, IPV6_SRC, false, 16);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Src);
            MfbIpv6Src mfi = (MfbIpv6Src) mf;
            assertEquals(AM_NEQ, IP6_ADDR, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6SrcMasked() {
        print(EOL + "ipv6SrcMasked()");
        pkt = getPktMasked(IPV6_SRC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6SrcMasked.hex for expected values
            verify(mf, 26, IPV6_SRC, true, 32);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Src);
            MfbIpv6Src mfi = (MfbIpv6Src) mf;
            assertEquals(AM_NEQ, IP6_ADDR, mfi.getIpAddress());
            assertEquals(AM_NEQ, IP6_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6Dst() {
        print(EOL + "ipv6Dst()");
        pkt = getPkt(IPV6_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6Dst.hex for expected values
            verify(mf, 27, IPV6_DST, false, 16);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Dst);
            MfbIpv6Dst mfi = (MfbIpv6Dst) mf;
            assertEquals(AM_NEQ, IP6_ADDR, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6DstMasked() {
        print(EOL + "ipv6DstMasked()");
        pkt = getPktMasked(IPV6_DST);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6DstMasked.hex for expected values
            verify(mf, 27, IPV6_DST, true, 32);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Dst);
            MfbIpv6Dst mfi = (MfbIpv6Dst) mf;
            assertEquals(AM_NEQ, IP6_ADDR, mfi.getIpAddress());
            assertEquals(AM_NEQ, IP6_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int IP6_FLABEL = 0x54413;
    private static final int IP6_FLABEL_MASK = 0xfffe0;

    @Test
    public void ipv6Flabel() {
        print(EOL + "ipv6Flabel()");
        pkt = getPkt(IPV6_FLABEL);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6Flabel.hex for expected values
            verify(mf, 28, IPV6_FLABEL, false, 3);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Flabel);
            MfbIpv6Flabel mfi = (MfbIpv6Flabel) mf;
            assertEquals(AM_NEQ, IP6_FLABEL, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6FlabelMasked() {
        print(EOL + "ipv6FlabelMasked()");
        pkt = getPktMasked(IPV6_FLABEL);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6FlabelMasked.hex for expected values
            verify(mf, 28, IPV6_FLABEL, true, 6);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Flabel);
            MfbIpv6Flabel mfi = (MfbIpv6Flabel) mf;
            assertEquals(AM_NEQ, IP6_FLABEL, mfi.getValue());
            assertEquals(AM_NEQ, IP6_FLABEL_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void icmpv6Type() {
        print(EOL + "icmpv6Type()");
        pkt = getPkt(ICMPV6_TYPE);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIcmpv6Type.hex for expected values
            verify(mf, 29, ICMPV6_TYPE, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIcmpv6Type);
            MfbIcmpv6Type mfi = (MfbIcmpv6Type) mf;
            assertEquals(AM_NEQ, EXP_ICMP6TYPE, mfi.getICMPv6Type());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final ICMPv6Type EXP_ICMP6TYPE = ICMPv6Type.valueOf(2);
    private static final int EXP_ICMP6CODE = 13;

    @Test
    public void icmpv6Code() {
        print(EOL + "icmpv6Code()");
        pkt = getPkt(ICMPV6_CODE);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIcmpv6Code.hex for expected values
            verify(mf, 30, ICMPV6_CODE, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbIcmpv6Code);
            MfbIcmpv6Code mfi = (MfbIcmpv6Code) mf;
            assertEquals(AM_NEQ, EXP_ICMP6CODE, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final IpAddress ND_TARG = ip("2222::bad:b0b");
    private static final MacAddress ND_SLL = mac("44:44:33:33:22:22");
    private static final MacAddress ND_TLL = mac("44:44:33:33:22:ff");

    @Test
    public void ipv6NdTarget() {
        print(EOL + "ipv6NdTarget()");
        pkt = getPkt(IPV6_ND_TARGET);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6NdTarget.hex for expected values
            verify(mf, 31, IPV6_ND_TARGET, false, 16);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6NdTarget);
            MfbIpv6NdTarget mfi = (MfbIpv6NdTarget) mf;
            assertEquals(AM_NEQ, ND_TARG, mfi.getIpAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6NdSll() {
        print(EOL + "ipv6NdSll()");
        pkt = getPkt(IPV6_ND_SLL);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6NdSll.hex for expected values
            verify(mf, 32, IPV6_ND_SLL, false, 6);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6NdSll);
            MfbIpv6NdSll mfi = (MfbIpv6NdSll) mf;
            assertEquals(AM_NEQ, ND_SLL, mfi.getMacAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6NdTll() {
        print(EOL + "ipv6NdTll()");
        pkt = getPkt(IPV6_ND_TLL);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6NdTll.hex for expected values
            verify(mf, 33, IPV6_ND_TLL, false, 6);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6NdTll);
            MfbIpv6NdTll mfi = (MfbIpv6NdTll) mf;
            assertEquals(AM_NEQ, ND_TLL, mfi.getMacAddress());
            assertNull(AM_HUH, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void mplsLabel() {
        print(EOL + "mplsLabel()");
        pkt = getPkt(MPLS_LABEL);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldMplsLabel.hex for expected values
            verify(mf, 34, MPLS_LABEL, false, 3);

            assertTrue(AM_WRCL, mf instanceof MfbMplsLabel);
            MfbMplsLabel mfi = (MfbMplsLabel) mf;
            assertEquals(AM_NEQ, EXP_MPLS_LABEL, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_MPLS_LABEL = 0xaaaaa;

    @Test
    public void mplsTc() {
        print(EOL + "mplsTc()");
        pkt = getPkt(MPLS_TC);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldMplsTc.hex for expected values
            verify(mf, 35, MPLS_TC, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbMplsTc);
            MfbMplsTc mfi = (MfbMplsTc) mf;
            assertEquals(AM_NEQ, EXP_MPLS_TC, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_MPLS_TC = 4;

    @Test
    public void mplsBos() {
        print(EOL + "mplsBos()");
        pkt = getPkt(MPLS_BOS);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldMplsBos.hex for expected values
            verify(mf, 36, MPLS_BOS, false, 1);

            assertTrue(AM_WRCL, mf instanceof MfbMplsBos);
            MfbMplsBos mfi = (MfbMplsBos) mf;
            assertEquals(AM_NEQ, EXP_MPLS_BOS, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }
    private static final int EXP_MPLS_BOS = 1;


    private static final int PBB_ISID_VALUE = 0x987654;
    private static final int PBB_ISID_MASK = 0x0f0f0f;

    @Test
    public void pbbIsid() {
        print(EOL + "pbbIsid()");
        pkt = getPkt(PBB_ISID);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldPbbIsid.hex for expected values
            verify(mf, 37, PBB_ISID, false, 3);

            assertTrue(AM_WRCL, mf instanceof MfbPbbIsid);
            MfbPbbIsid mfi = (MfbPbbIsid) mf;
            assertEquals(AM_NEQ, PBB_ISID_VALUE, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void pbbIsidMasked() {
        print(EOL + "pbbIsidMasked()");
        pkt = getPktMasked(PBB_ISID);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldPbbIsidMasked.hex for expected values
            verify(mf, 37, PBB_ISID, true, 6);

            assertTrue(AM_WRCL, mf instanceof MfbPbbIsid);
            MfbPbbIsid mfi = (MfbPbbIsid) mf;
            assertEquals(AM_NEQ, PBB_ISID_VALUE, mfi.getValue());
            assertEquals(AM_NEQ, PBB_ISID_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final long TUN_ID_VALUE = 0x0a0b0c0d01020304L;
    private static final long TUN_ID_MASK = 0x00000000ffff0000L;

    @Test
    public void tunnelId() {
        print(EOL + "tunnelId()");
        pkt = getPkt(TUNNEL_ID);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldTunnelId.hex for expected values
            verify(mf, 38, TUNNEL_ID, false, 8);

            assertTrue(AM_WRCL, mf instanceof MfbTunnelId);
            MfbTunnelId mfi = (MfbTunnelId) mf;
            assertEquals(AM_NEQ, TUN_ID_VALUE, mfi.getValue());
            assertEquals(AM_NEQ, 0, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void tunnelIdMasked() {
        print(EOL + "tunnelIdMasked()");
        pkt = getPktMasked(TUNNEL_ID);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldTunnelIdMasked.hex for expected values
            verify(mf, 38, TUNNEL_ID, true, 16);

            assertTrue(AM_WRCL, mf instanceof MfbTunnelId);
            MfbTunnelId mfi = (MfbTunnelId) mf;
            assertEquals(AM_NEQ, TUN_ID_VALUE, mfi.getValue());
            assertEquals(AM_NEQ, TUN_ID_MASK, mfi.getMask());

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final IPv6ExtHdr[] EXTHDR_FLAGS = {
            IPv6ExtHdr.NO_NEXT,
            IPv6ExtHdr.AUTH,
            IPv6ExtHdr.ROUTER,
            IPv6ExtHdr.UN_SEQ,
    };
    private static final IPv6ExtHdr[] EXTHDR_FLAGS_MASK = {
            IPv6ExtHdr.UN_REP,
            IPv6ExtHdr.UN_SEQ,
    };

    @Test
    public void ipv6Exthdr() {
        print(EOL + "ipv6Exthdr()");
        pkt = getPkt(IPV6_EXTHDR);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6Exthdr.hex for expected values
            verify(mf, 39, IPV6_EXTHDR, false, 2);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Exthdr);
            MfbIpv6Exthdr mfi = (MfbIpv6Exthdr) mf;

            Map<IPv6ExtHdr, Boolean> flags = mfi.getFlags();
            print(flags);
            assertEquals(AM_UXS, 9, flags.size());
            assertEquals(AM_NEQ, true, flags.get(IPv6ExtHdr.NO_NEXT));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.ESP));
            assertEquals(AM_NEQ, true, flags.get(IPv6ExtHdr.AUTH));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.DEST));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.FRAG));
            assertEquals(AM_NEQ, true, flags.get(IPv6ExtHdr.ROUTER));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.HOP));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.UN_REP));
            assertEquals(AM_NEQ, true, flags.get(IPv6ExtHdr.UN_SEQ));

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void ipv6ExthdrMasked() {
        print(EOL + "ipv6ExthdrMasked()");
        pkt = getPktMasked(IPV6_EXTHDR);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldIpv6ExthdrMasked.hex for expected values
            verify(mf, 39, IPV6_EXTHDR, true, 4);

            assertTrue(AM_WRCL, mf instanceof MfbIpv6Exthdr);
            MfbIpv6Exthdr mfi = (MfbIpv6Exthdr) mf;

            Map<IPv6ExtHdr, Boolean> flags = mfi.getFlags();
            print(flags);
            assertEquals(AM_UXS, 4, flags.size());
            assertEquals(AM_NEQ, true, flags.get(IPv6ExtHdr.AUTH));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.FRAG));
            assertEquals(AM_NEQ, true, flags.get(IPv6ExtHdr.ROUTER));
            assertEquals(AM_NEQ, false, flags.get(IPv6ExtHdr.HOP));

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void experHp() {
        print(EOL + "experHp()");
        pkt = getPkt(MF_EXPER_HP);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldExperHp.hex for expected values
            verify(mf, OxmClass.EXPERIMENTER.getCode(), OxmClass.EXPERIMENTER,
                    RAW_FT, null, false, 9);

            assertTrue(AM_WRCL, mf instanceof MFieldExperimenter);
            MFieldExperimenter mfi = (MFieldExperimenter) mf;

            assertEquals(AM_NEQ, ExperimenterId.HP.encodedId(), mfi.getId());
            assertArrayEquals(AM_NEQ, HP_ID_BYTES, mfi.getIdAsBytes());
            assertEquals(AM_NEQ, ExperimenterId.HP, mfi.getExpId());
            byte[] data = mfi.getPayload();
            String dataStr = ByteUtils.getStringFromRawBytes(data);
            print("data string: " + dataStr);
            assertEquals(AM_NEQ, PAYLOAD_STR, dataStr);

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

//    private static final int OXM_CLZ_EXP = 0xffff;
//    private static final int OXM_CLZ_HP = 0x0004;

    private static final int RAW_FT = 23;
    private static final byte[] HP_ID_BYTES = {0, 0, 0x24, 0x81-B};
    private static final String PAYLOAD_STR = "Hola!";
    private static final String PAYLOAD_STR_2 = "Simon";


    @Test
    public void clazzHp() {
        print(EOL + "clazzHp()");
        pkt = getPkt(MF_CLAZZ_HP);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldClazzHp.hex for expected values
            verify(mf, OxmClass.HP.getCode(), OxmClass.HP, RAW_FT, null,
                    false, 5);

            assertTrue(AM_WRCL, mf instanceof MFieldMinimal);
            MFieldMinimal mfi = (MFieldMinimal) mf;

            byte[] data = mfi.getPayload();
            String dataStr = ByteUtils.getStringFromRawBytes(data);
            print("data string: \"{}\"", dataStr);
            assertEquals(AM_NEQ, PAYLOAD_STR_2, dataStr);

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    @Test
    public void clazzUnknown() {
        print(EOL + "clazzUnknown()");
        pkt = getPkt(MF_CLAZZ_UNKNOWN);
        try {
            mf =  parseField(pkt, V_1_3);
            print(mf);

            // see fieldClazzUnknown.hex for expected values
            verify(mf, EXP_UNK_CLZ, OxmClass.UNKNOWN, EXP_UNK_FT,
                    null, false, 10);

            assertTrue(AM_WRCL, mf instanceof MFieldMinimal);
            MFieldMinimal mfi = (MFieldMinimal) mf;
            byte[] data = mfi.getPayload();
            print(ByteUtils.toHexArrayString(data));

            assertArrayEquals(AM_NEQ, EXP_UNK_PAYLOAD, data);

        } catch (MessageParseException e) {
            print(e);
            fail(E_PARSE_FAIL);
        }
        checkEOBuffer(pkt);
    }

    private static final int EXP_UNK_CLZ =  0x0042;
    private static final int EXP_UNK_FT =  21;
    private static final byte[] EXP_UNK_PAYLOAD = {1,2,3,4,5,6,7,8,9,10};


    // ======= WRITE TESTS =================================================

    private byte[] getExpBytes(String name) {
        return getExpByteArray("../" + FILE_PREFIX + name);
    }

    private byte[] getExpBytes(OxmBasicFieldType field, String suffix) {
        String basename = StringUtils.toCamelCase(FILE_PREFIX, field);
        return getExpByteArray("../" + basename + suffix);
    }

    private byte[] getExpBytes(OxmBasicFieldType type) {
        return getExpBytes(type, "");
    }

    private byte[] getMaskedExpBytes(OxmBasicFieldType type) {
        return getExpBytes(type, "Masked");
    }

    private void encodeFieldAndVerify13(MatchField mf, String label,
                                        byte[] expData) {
        print(EOL + label + "()");
        print(mf);
        // encode the field into a buffer the same size
        OfPacketWriter pkt = new OfPacketWriter(expData.length);
        FieldFactory.encodeField(mf, pkt);
        // check that all is as expected
        verifyEncodement(label, expData, pkt);
    }

    private void slurpExpectedResultThenEncodeAndVerify13(MatchField mf,
                                                          OxmBasicFieldType ft,
                                                          boolean masked) {
        String label = StringUtils.toCamelCase("write", ft);
        if (masked)
            label += "Masked";
        byte[] expData = masked ? getMaskedExpBytes(ft) : getExpBytes(ft);
        encodeFieldAndVerify13(mf, label, expData);
    }

    private void slurpExpectedResultThenEncodeAndVerify13(MatchField mf,
                                                          OxmBasicFieldType ft,
                                                          String suffix) {
        String label = StringUtils.toCamelCase("write", ft) + suffix;
        byte[] expData = getExpBytes(ft, suffix);
        encodeFieldAndVerify13(mf, label, expData);
    }

    private void slurpExpectedResultThenEncodeAndVerify13(MatchField mf,
                                                          String basename) {
        String label = "write" + basename;
        byte[] expData = getExpBytes(basename);
        encodeFieldAndVerify13(mf, label, expData);
    }


    @Test
    public void writeInPort() {
        mf = createBasicField(V_1_3, IN_PORT, EXP_IN_PORT);
        slurpExpectedResultThenEncodeAndVerify13(mf, IN_PORT, false);
    }

    @Test
    public void writeInPhyPort() {
        mf = createBasicField(V_1_3, IN_PHY_PORT, EXP_IN_PHY_PORT);
        slurpExpectedResultThenEncodeAndVerify13(mf, IN_PHY_PORT, false);
    }

    @Test
    public void writeMetadata() {
        mf = createBasicField(V_1_3, METADATA, META_LONG_V);
        slurpExpectedResultThenEncodeAndVerify13(mf, METADATA, false);
    }

    @Test
    public void writeMetadataMasked() {
        mf = createBasicField(V_1_3, METADATA, META_LONG_V, META_LONG_M);
        slurpExpectedResultThenEncodeAndVerify13(mf, METADATA, true);
    }

    @Test
    public void writeEthDst() {
        mf = createBasicField(V_1_3, ETH_DST, ETH_MAC);
        slurpExpectedResultThenEncodeAndVerify13(mf, ETH_DST, false);
    }

    @Test
    public void writeEthDstMasked() {
        mf = createBasicField(V_1_3, ETH_DST, ETH_MAC, ETH_MAC_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, ETH_DST, true);
    }

    @Test
    public void writeEthSrc() {
        mf = createBasicField(V_1_3, ETH_SRC, ETH_MAC);
        slurpExpectedResultThenEncodeAndVerify13(mf, ETH_SRC, false);
    }

    @Test
    public void writeEthSrcMasked() {
        mf = createBasicField(V_1_3, ETH_SRC, ETH_MAC, ETH_MAC_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, ETH_SRC, true);
    }

    @Test
    public void writeEthType() {
        mf = createBasicField(V_1_3, ETH_TYPE, EthernetType.ARP);
        slurpExpectedResultThenEncodeAndVerify13(mf, ETH_TYPE, false);
    }

    @Test
    public void writeVlanVidNone() {
        mf = createBasicField(V_1_3, VLAN_VID, VlanId.NONE);
        slurpExpectedResultThenEncodeAndVerify13(mf, VLAN_VID, "None");
    }

    @Test
    public void writeVlanVidPresent() {
        mf = createBasicField(V_1_3, VLAN_VID, VlanId.PRESENT);
        slurpExpectedResultThenEncodeAndVerify13(mf, VLAN_VID, "Present");
    }

    @Test
    public void writeVlanVidExact() {
        mf = createBasicField(V_1_3, VLAN_VID, VLAN_ID_42);
        slurpExpectedResultThenEncodeAndVerify13(mf, VLAN_VID, "Exact");
    }

    @Test
    public void writeVlanPcp() {
        mf = createBasicField(V_1_3, VLAN_PCP, EXP_VLAN_PCP);
        slurpExpectedResultThenEncodeAndVerify13(mf, VLAN_PCP, false);
    }

    @Test
    public void writeIpDscp() {
        mf = createBasicField(V_1_3, IP_DSCP, EXP_IP_DSCP);
        slurpExpectedResultThenEncodeAndVerify13(mf, IP_DSCP, false);
    }

    @Test
    public void writeIpEcn() {
        mf = createBasicField(V_1_3, IP_ECN, EXP_IP_ECN);
        slurpExpectedResultThenEncodeAndVerify13(mf, IP_ECN, false);
    }

    @Test
    public void writeIpProto() {
        mf = createBasicField(V_1_3, IP_PROTO, IpProtocol.UDP);
        slurpExpectedResultThenEncodeAndVerify13(mf, IP_PROTO, false);
    }

    @Test
    public void writeIpv4Src() {
        mf = createBasicField(V_1_3, IPV4_SRC, IP4);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV4_SRC, false);
    }

    @Test
    public void writeIpv4SrcMasked() {
        mf = createBasicField(V_1_3, IPV4_SRC, IP4, IP4_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV4_SRC, true);
    }

    @Test
    public void writeIpv4Dst() {
        mf = createBasicField(V_1_3, IPV4_DST, IP4);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV4_DST, false);
    }

    @Test
    public void writeIpv4DstMasked() {
        mf = createBasicField(V_1_3, IPV4_DST, IP4, IP4_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV4_DST, true);
    }

    @Test
    public void writeTcpSrc() {
        mf = createBasicField(V_1_3, TCP_SRC, EXP_TCP_SRC);
        slurpExpectedResultThenEncodeAndVerify13(mf, TCP_SRC, false);
    }

    @Test
    public void writeTcpDst() {
        mf = createBasicField(V_1_3, TCP_DST, EXP_TCP_DST);
        slurpExpectedResultThenEncodeAndVerify13(mf, TCP_DST, false);
    }

    @Test
    public void writeUdpSrc() {
        mf = createBasicField(V_1_3, UDP_SRC, EXP_UDP_SRC);
        slurpExpectedResultThenEncodeAndVerify13(mf, UDP_SRC, false);
    }

    @Test
    public void writeUdpDst() {
        mf = createBasicField(V_1_3, UDP_DST, EXP_UDP_DST);
        slurpExpectedResultThenEncodeAndVerify13(mf, UDP_DST, false);
    }

    @Test
    public void writeSctpSrc() {
        mf = createBasicField(V_1_3, SCTP_SRC, EXP_SCTP_SRC);
        slurpExpectedResultThenEncodeAndVerify13(mf, SCTP_SRC, false);
    }

    @Test
    public void writeSctpDst() {
        mf = createBasicField(V_1_3, SCTP_DST, EXP_SCTP_DST);
        slurpExpectedResultThenEncodeAndVerify13(mf, SCTP_DST, false);
    }

    @Test
    public void writeIcmpv4Type() {
        mf = createBasicField(V_1_3, ICMPV4_TYPE, EXP_ICMP4TYPE);
        slurpExpectedResultThenEncodeAndVerify13(mf, ICMPV4_TYPE, false);
    }

    @Test
    public void writeIcmpv4Code() {
        mf = createBasicField(V_1_3, ICMPV4_CODE, EXP_ICMP4CODE);
        slurpExpectedResultThenEncodeAndVerify13(mf, ICMPV4_CODE, false);
    }

    @Test
    public void writeArpOp() {
        mf = createBasicField(V_1_3, ARP_OP, EXP_ARP_OP);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_OP, false);
    }

    @Test
    public void writeArpSpa() {
        mf = createBasicField(V_1_3, ARP_SPA, ARP_IP);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_SPA, false);
    }

    @Test
    public void writeArpSpaMasked() {
        mf = createBasicField(V_1_3, ARP_SPA, ARP_IP, ARP_IP_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_SPA, true);
    }

    @Test
    public void writeArpTpa() {
        mf = createBasicField(V_1_3, ARP_TPA, ARP_IP);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_TPA, false);
    }

    @Test
    public void writeArpTpaMasked() {
        mf = createBasicField(V_1_3, ARP_TPA, ARP_IP, ARP_IP_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_TPA, true);
    }

    @Test
    public void writeArpSha() {
        mf = createBasicField(V_1_3, ARP_SHA, ARP_MAC);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_SHA, false);
    }

    @Test
    public void writeArpShaMasked() {
        mf = createBasicField(V_1_3, ARP_SHA, ARP_MAC, ARP_MAC_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_SHA, true);
    }

    @Test
    public void writeArpTha() {
        mf = createBasicField(V_1_3, ARP_THA, ARP_MAC);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_THA, false);
    }

    @Test
    public void writeArpThaMasked() {
        mf = createBasicField(V_1_3, ARP_THA, ARP_MAC, ARP_MAC_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, ARP_THA, true);
    }

    @Test
    public void writeIpv6Src() {
        mf = createBasicField(V_1_3, IPV6_SRC, IP6_ADDR);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_SRC, false);
    }

    @Test
    public void writeIpv6SrcMasked() {
        mf = createBasicField(V_1_3, IPV6_SRC, IP6_ADDR, IP6_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_SRC, true);
    }

    @Test
    public void writeIpv6Dst() {
        mf = createBasicField(V_1_3, IPV6_DST, IP6_ADDR);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_DST, false);
    }

    @Test
    public void writeIpv6DstMasked() {
        mf = createBasicField(V_1_3, IPV6_DST, IP6_ADDR, IP6_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_DST, true);
    }

    @Test
    public void writeIpv6Flabel() {
        mf = createBasicField(V_1_3, IPV6_FLABEL, IP6_FLABEL);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_FLABEL, false);
    }

    @Test
    public void writeIpv6FlabelMasked() {
        mf = createBasicField(V_1_3, IPV6_FLABEL, IP6_FLABEL, IP6_FLABEL_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_FLABEL, true);
    }

    @Test
    public void writeIcmpv6Type() {
        mf = createBasicField(V_1_3, ICMPV6_TYPE, EXP_ICMP6TYPE);
        slurpExpectedResultThenEncodeAndVerify13(mf, ICMPV6_TYPE, false);
    }

    @Test
    public void writeIcmpv6Code() {
        mf = createBasicField(V_1_3, ICMPV6_CODE, EXP_ICMP6CODE);
        slurpExpectedResultThenEncodeAndVerify13(mf, ICMPV6_CODE, false);
    }

    @Test
    public void writeIpv6NdTarget() {
        mf = createBasicField(V_1_3, IPV6_ND_TARGET, ND_TARG);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_ND_TARGET, false);
    }

    @Test
    public void writeIpv6NdSll() {
        mf = createBasicField(V_1_3, IPV6_ND_SLL, ND_SLL);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_ND_SLL, false);
    }

    @Test
    public void writeIpv6NdTll() {
        mf = createBasicField(V_1_3, IPV6_ND_TLL, ND_TLL);
        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_ND_TLL, false);
    }

    @Test
    public void writeMplsLabel() {
        mf = createBasicField(V_1_3, MPLS_LABEL, EXP_MPLS_LABEL);
        slurpExpectedResultThenEncodeAndVerify13(mf, MPLS_LABEL, false);
    }

    @Test
    public void writeMplsTc() {
        mf = createBasicField(V_1_3, MPLS_TC, EXP_MPLS_TC);
        slurpExpectedResultThenEncodeAndVerify13(mf, MPLS_TC, false);
    }

    @Test
    public void writeMplsBos() {
        mf = createBasicField(V_1_3, MPLS_BOS, EXP_MPLS_BOS);
        slurpExpectedResultThenEncodeAndVerify13(mf, MPLS_BOS, false);
    }

    @Test
    public void writePbbIsid() {
        mf = createBasicField(V_1_3, PBB_ISID, PBB_ISID_VALUE);
        slurpExpectedResultThenEncodeAndVerify13(mf, PBB_ISID, false);
    }

    @Test
    public void writePbbIsidMasked() {
        mf = createBasicField(V_1_3, PBB_ISID, PBB_ISID_VALUE, PBB_ISID_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, PBB_ISID, true);
    }

    @Test
    public void writeTunnelId() {
        mf = createBasicField(V_1_3, TUNNEL_ID, TUN_ID_VALUE);
        slurpExpectedResultThenEncodeAndVerify13(mf, TUNNEL_ID, false);
    }

    @Test
    public void writeTunnelIdMasked() {
        mf = createBasicField(V_1_3, TUNNEL_ID, TUN_ID_VALUE, TUN_ID_MASK);
        slurpExpectedResultThenEncodeAndVerify13(mf, TUNNEL_ID, true);
    }

    @Test
    public void writeIpv6Exthdr() {
        Map<IPv6ExtHdr, Boolean> map = new HashMap<IPv6ExtHdr, Boolean>();
        map.put(IPv6ExtHdr.NO_NEXT, true);
        map.put(IPv6ExtHdr.ESP, false);
        map.put(IPv6ExtHdr.AUTH, true);
        map.put(IPv6ExtHdr.DEST, false);
        map.put(IPv6ExtHdr.FRAG, false);
        map.put(IPv6ExtHdr.ROUTER, true);
        map.put(IPv6ExtHdr.HOP, false);
        map.put(IPv6ExtHdr.UN_REP, false);
        map.put(IPv6ExtHdr.UN_SEQ, true);
        mf = createBasicField(V_1_3, IPV6_EXTHDR, map);

        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_EXTHDR, false);
    }

    @Test
    public void writeIpv6ExthdrMasked() {
        Map<IPv6ExtHdr, Boolean> map = new HashMap<IPv6ExtHdr, Boolean>();
        map.put(IPv6ExtHdr.AUTH, true);
        map.put(IPv6ExtHdr.FRAG, false);
        map.put(IPv6ExtHdr.ROUTER, true);
        map.put(IPv6ExtHdr.HOP, false);
        mf = createBasicField(V_1_3, IPV6_EXTHDR, map);

        slurpExpectedResultThenEncodeAndVerify13(mf, IPV6_EXTHDR, true);
    }

    @Test
    public void writeExperHp() {
        print(EOL + "writeExperHp()");
        mf = FieldFactory.createExperimenterField(V_1_3, RAW_FT,
                ExperimenterId.HP, bytesFromString(PAYLOAD_STR));
        slurpExpectedResultThenEncodeAndVerify13(mf, MF_EXPER_HP);
    }

    @Test
    public void writeClazzHp() {
        print(EOL + "writeClazzHp()");
        mf = FieldFactory.createMinimalField(V_1_3, OxmClass.HP, RAW_FT,
                bytesFromString(PAYLOAD_STR_2));
        slurpExpectedResultThenEncodeAndVerify13(mf, MF_CLAZZ_HP);
    }


    @Test
    public void writeClazzUnknown() {
        print(EOL + "writeClazzUnknown()");
        mf = FieldFactory.createMinimalField(V_1_3, OxmClass.UNKNOWN,
                EXP_UNK_CLZ, EXP_UNK_FT, EXP_UNK_PAYLOAD);
        slurpExpectedResultThenEncodeAndVerify13(mf, MF_CLAZZ_UNKNOWN);
    }


    // TODO: FieldFactory.createMinimalField(...)
}
