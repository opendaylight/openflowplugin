/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.util.Iterator;

import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10Builder;

/**
 *
 */
public class MatchConvertorV10Impl implements MatchConvertor<MatchV10> {

    /** default MAC */
    public static final MacAddress zeroMac = new MacAddress("00:00:00:00:00:00");
    /** default IPv4 */
    public static final Ipv4Address zeroIPv4 = new Ipv4Address("0.0.0.0");

    /*
     * The value 0xffff (OFP_VLAN_NONE) is used to indicate
     * that no VLAN ID is set for OF Flow.
     */
    private static final int OFP_VLAN_NONE = (int) 0xffff;

    /**
     * Method builds openflow 1.0 specific match (MatchV10) from MD-SAL match.
     * @param match MD-SAL match
     * @return OF-API match
     * @author avishnoi@in.ibm.com
     */
    @Override
    public MatchV10 convert(final Match match,final BigInteger datapathid) {
        MatchV10Builder matchBuilder = new MatchV10Builder();
        boolean _dLDST = true;
        boolean _dLSRC = true;
        boolean _dLTYPE = true;
        boolean _dLVLAN = true;
        boolean _dLVLANPCP = true;
        boolean _iNPORT = true;
        boolean _nWPROTO = true;
        boolean _nWTOS = true;
        boolean _tPDST = true;
        boolean _tPSRC = true;

        matchBuilder.setInPort(0);
        matchBuilder.setDlDst(zeroMac);
        matchBuilder.setDlSrc(zeroMac);
        matchBuilder.setDlType(0);
        matchBuilder.setDlVlan(OFP_VLAN_NONE);
        matchBuilder.setDlVlanPcp((short) 0);
        matchBuilder.setNwDst(zeroIPv4);
        matchBuilder.setNwDstMask((short) 0);
        matchBuilder.setNwSrc(zeroIPv4);
        matchBuilder.setNwSrcMask((short) 0);
        matchBuilder.setNwProto((short) 0);
        matchBuilder.setNwTos((short) 0);
        matchBuilder.setTpSrc(0);
        matchBuilder.setTpDst(0);

        if (match != null) {
            EthernetMatch ethernetMatch = match.getEthernetMatch();
            if(ethernetMatch!= null){
                _dLDST = convertEthernetDlDst(matchBuilder, ethernetMatch);
                _dLSRC = convertEthernetDlSrc(matchBuilder, ethernetMatch);
                _dLTYPE = convertEthernetDlType(matchBuilder, ethernetMatch);
            }
            VlanMatch vlanMatch = match.getVlanMatch();
            if(vlanMatch!= null){
                _dLVLAN = convertDlVlan(matchBuilder, vlanMatch);
                _dLVLANPCP = convertDlVlanPcp(matchBuilder, vlanMatch);
            }
            NodeConnectorId inPort = match.getInPort();
            if(inPort!=null){
                _iNPORT = convertInPortMatch(matchBuilder, inPort);
            }
            Layer3Match l3Match = match.getLayer3Match();
            if(l3Match != null){
                if(l3Match instanceof Ipv4Match){
                    Ipv4Match ipv4 = (Ipv4Match)l3Match;
                    _tPSRC = convertL3Ipv4SrcMatch(matchBuilder, ipv4);
                    _tPDST = convertL3Ipv4DstMatch(matchBuilder, ipv4);
                }
            }
            IpMatch ipMatch = match.getIpMatch();
            if(ipMatch!=null){
                _nWPROTO = convertNwProto(matchBuilder, ipMatch);
                _nWTOS = convertNwTos(matchBuilder, ipMatch);
            }
            Layer4Match layer4Match = match.getLayer4Match();
            if (layer4Match != null) {
                if (layer4Match instanceof TcpMatch) {
                    TcpMatch tcpMatch = (TcpMatch) layer4Match;
                    _tPSRC = convertL4TpSrcMatch(matchBuilder, tcpMatch);
                    _tPDST = convertL4TpDstMatch(matchBuilder, tcpMatch);
                } else if (layer4Match instanceof UdpMatch) {
                    UdpMatch udpMatch = (UdpMatch) layer4Match;
                    _tPSRC = convertL4UdpSrcMatch(matchBuilder, udpMatch);
                    _tPDST = convertL4UdpDstMatch(matchBuilder, udpMatch);
                }
            }
        }

        FlowWildcardsV10 wildCards = new FlowWildcardsV10(
                _dLDST, _dLSRC, _dLTYPE, _dLVLAN,
                _dLVLANPCP, _iNPORT, _nWPROTO, _nWTOS, _tPDST, _tPSRC);
        matchBuilder.setWildcards(wildCards);

        return matchBuilder.build();
    }

    /**
     * @param matchBuilder
     * @param udpMatch
     * @return is wildCard
     */
    private static boolean convertL4UdpDstMatch(final MatchV10Builder matchBuilder,
            final UdpMatch udpMatch) {
        if (udpMatch.getUdpDestinationPort() != null) {
            matchBuilder.setTpDst(udpMatch.getUdpDestinationPort().getValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param udpMatch
     * @return is wildCard
     */
    private static boolean convertL4UdpSrcMatch(final MatchV10Builder matchBuilder,
            final UdpMatch udpMatch) {
        if (udpMatch.getUdpSourcePort() != null) {
            matchBuilder.setTpSrc(udpMatch.getUdpSourcePort().getValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param tcpMatch
     * @return is wildCard
     */
    private static boolean convertL4TpDstMatch(final MatchV10Builder matchBuilder,
            final TcpMatch tcpMatch) {
        if (tcpMatch.getTcpDestinationPort() != null) {
            matchBuilder.setTpDst(tcpMatch.getTcpDestinationPort().getValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param tcpMatch
     * @return is wildCard
     */
    private static boolean convertL4TpSrcMatch(final MatchV10Builder matchBuilder,
            final TcpMatch tcpMatch) {
        if (tcpMatch.getTcpSourcePort() != null) {
            matchBuilder.setTpSrc(tcpMatch.getTcpSourcePort().getValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ipMatch
     * @return is wildCard
     */
    private static boolean convertNwTos(final MatchV10Builder matchBuilder,
            final IpMatch ipMatch) {
        if (ipMatch.getIpDscp() != null) {
            matchBuilder.setNwTos(ipMatch.getIpDscp().getValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ipMatch
     * @return is wildCard
     */
    private static boolean convertNwProto(final MatchV10Builder matchBuilder, final IpMatch ipMatch) {
        if (ipMatch.getIpProtocol() != null) {
            matchBuilder.setNwProto(ipMatch.getIpProtocol());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ipv4
     * @return is wildCard
     */
    private static boolean convertL3Ipv4DstMatch(final MatchV10Builder matchBuilder,
            final Ipv4Match ipv4) {
        if(ipv4.getIpv4Destination()!=null){
            Iterator<String> addressParts = MatchConvertorImpl.PREFIX_SPLITTER.split(ipv4.getIpv4Destination().getValue()).iterator();
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            Integer prefix = buildPrefix(addressParts);
            matchBuilder.setNwDst(ipv4Address);
            matchBuilder.setNwDstMask(prefix.shortValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ipv4
     * @return is wildCard
     */
    private static boolean convertL3Ipv4SrcMatch(final MatchV10Builder matchBuilder,
            final Ipv4Match ipv4) {
        if(ipv4.getIpv4Source()!=null){
            Iterator<String> addressParts = MatchConvertorImpl.PREFIX_SPLITTER.split(ipv4.getIpv4Source().getValue()).iterator();
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            int prefix = buildPrefix(addressParts);

            matchBuilder.setNwSrc(ipv4Address);
            matchBuilder.setNwSrcMask((short) prefix);
            return false;
        }
        return true;
    }

    /**
     * @param addressParts
     * @return
     */
    private static int buildPrefix(final Iterator<String> addressParts) {
        int prefix = 32;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        }
        return prefix;
    }

    /**
     * @param matchBuilder
     * @param vlanMatch
     * @return
     */
    private static boolean convertDlVlanPcp(final MatchV10Builder matchBuilder,
            final VlanMatch vlanMatch) {
        if (vlanMatch.getVlanPcp() != null) {
            matchBuilder.setDlVlanPcp(vlanMatch.getVlanPcp().getValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param vlanMatch
     * @return
     */
    private static boolean convertDlVlan(final MatchV10Builder matchBuilder, final VlanMatch vlanMatch) {
        if (vlanMatch.getVlanId() != null) {
            int vlanId = vlanMatch.getVlanId().getVlanId().getValue();
            matchBuilder.setDlVlan((vlanId == 0 ? OFP_VLAN_NONE : vlanId));
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ethernetMatch
     * @return is wildCard
     */
    private static boolean convertEthernetDlType(final MatchV10Builder matchBuilder,
            final EthernetMatch ethernetMatch) {
        if (ethernetMatch.getEthernetType() != null) {
            matchBuilder.setDlType(ethernetMatch.getEthernetType().getType().getValue().intValue());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ethernetMatch
     * @return is wildCard
     */
    private static boolean convertEthernetDlSrc(final MatchV10Builder matchBuilder,
            final EthernetMatch ethernetMatch) {
        if (ethernetMatch.getEthernetSource() != null) {
            matchBuilder.setDlSrc(ethernetMatch.getEthernetSource().getAddress());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param ethernetMatch
     * @return is wildCard
     */
    private static boolean convertEthernetDlDst(final MatchV10Builder matchBuilder,
            final EthernetMatch ethernetMatch) {
        if (ethernetMatch.getEthernetDestination() != null) {
            matchBuilder.setDlDst(ethernetMatch.getEthernetDestination().getAddress());
            return false;
        }
        return true;
    }

    /**
     * @param matchBuilder
     * @param inPort
     */
    private static boolean convertInPortMatch(final MatchV10Builder matchBuilder, final NodeConnectorId inPort) {
        if (inPort != null) {
            matchBuilder.setInPort(InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF10, inPort).intValue());
            return false;
        }
        return true;
    }

}
