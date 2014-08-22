/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.SafeMap;
import org.opendaylight.util.net.EthernetType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the main encoding and decoding API for the Packet Library.
 *
 * @author Frank Wood
 */
public class Codec {

    private static final int DEF_PKT_LAYER_CAPACITY = 10;
    private static final int DEF_PKT_MIN_ENCODE_LEN = 60;

    private static final int IP_VERSION_MASK = 0x0f0;
    private static final int IP_VERSION_BIT_SHIFT = 4;

    private static final String E_NO_IP = "No IP layer";

    private static final SafeMap<EthernetType, ProtocolId> ETHTYPE_2_ID =
            new SafeMap.Builder<EthernetType, ProtocolId>(ProtocolId.UNKNOWN)
                .add(EthernetType.IPv4, ProtocolId.IP)
                .add(EthernetType.IPv6, ProtocolId.IPV6)
                .add(EthernetType.ARP, ProtocolId.ARP)
                .add(EthernetType.LLDP, ProtocolId.LLDP)
                .add(EthernetType.BDDP, ProtocolId.BDDP)
                .add(EthernetType.MPLS_U, ProtocolId.MPLS)
                .add(EthernetType.MPLS_M, ProtocolId.MPLS)
                .add(EthernetType.valueOf(0x08863), ProtocolId.PPP_ETHERNET)
                .add(EthernetType.valueOf(0x08864), ProtocolId.PPP_ETHERNET)
                .add(EthernetType.valueOf(0x06558), ProtocolId.ETHERNET)
                .build();

    private static final SafeMap<IpType, ProtocolId> IPTYPE_2_ID =
            new SafeMap.Builder<IpType, ProtocolId>(ProtocolId.UNKNOWN)
                .add(IpType.ICMP, ProtocolId.ICMP)
                .add(IpType.IPV6_ICMP, ProtocolId.ICMPV6)
                .add(IpType.TCP, ProtocolId.TCP)
                .add(IpType.UDP, ProtocolId.UDP)
                .add(IpType.SCTP, ProtocolId.SCTP)
                .add(IpType.GRE, ProtocolId.GRE)
                .build();

    /**
     * Encode a packet.
     *
     * @param pkt packet to encode
     * @return the encoded packet
     */
    public static byte[] encode(Packet pkt) {
        return encode(pkt, DEF_PKT_MIN_ENCODE_LEN);
    }

    /**
     * Encode a packet specifying the minimum encode length. In the case of
     * Ethernet packets, the minimum length is used to determine the amount
     * of padding appended after the payload.
     *
     * @param pkt packet to encode
     * @param minLen minimum packet encode length
     * @return the encoded packet
     */
    public static byte[] encode(Packet pkt, int minLen) {
        EncodedPayload ep = new EncodedPayload(pkt.size());

        // Encode protocols from highest to lowest layer.
        for (int i=pkt.size()-1; i>=0; i--)
            ep.add(encode(pkt.get(i), ep, pkt));

        return ep.flatten(minLen).array();
    }

    /**
     * Creates a new pseudo header from the IPv4/IPv6 layer of the packet.
     * Filling in the address information only. It is assumed that the packet
     * contains a IPv4 or IPv6 layer.
     * 
     * Note that {@link Packet#innermost()} is used here to account for packets
     * that may be tunneled.
     *
     * @param pkt packet that will be encoded
     * @param type type of the upper-layer protocol creating this pseudo header
     * @return the new IPv4/IPv6 pseudo header
     * @throws ProtocolException if the packet doesn't contain an IP layer
     */
    private static IpPseudoHdr ipPseudoHdr(Packet pkt, IpType type) {
        Ip ip = (Ip) pkt.innermost(ProtocolId.IP);
        if (null != ip)
            return new IpPseudoHdr(ip.srcAddr(), ip.dstAddr(), type);

        IpV6 ipV6 = (IpV6) pkt.innermost(ProtocolId.IPV6);
        if (null != ipV6)
            return new IpPseudoHdr(ipV6.srcAddr(), ipV6.dstAddr(), type);

        throw new ProtocolException(E_NO_IP);
    }

    /**
     * Calls the appropriate encoder for the given protocol returning the
     * packet writer filled with the encoded bytes.
     *
     * @param p input protocol
     * @param ep encoded payload for this protocol
     * @param pkt packet that this protocol is contained
     * @return the new packet writer containing the encoded bytes
     */
    private static PacketWriter encode(Protocol p, EncodedPayload ep,
            Packet pkt) {
        
        switch (p.id()) {

            case ETHERNET:
                return EthernetCodec.encode((Ethernet) p, ep);

            case MPLS:
                return MplsCodec.encode((Mpls) p);

            case PPP_ETHERNET:
                return PppEthernetCodec.encode((PppEthernet) p, ep);

            case ARP:
                return ArpCodec.encode((Arp) p);

            case IP:
                return IpCodec.encode((Ip) p, ep);

            case IPV6:
                return IpCodecV6.encode((IpV6) p, ep);

            case GRE:
                return GreCodec.encode((Gre) p);                
                
            case LLDP:
            case BDDP:
                return LldpCodec.encode((Lldp) p);

            case ICMP:
                return IcmpCodec.encode((Icmp) p);

            case ICMPV6:
                return IcmpCodecV6.encode((IcmpV6) p,
                                          ipPseudoHdr(pkt, IpType.IPV6_ICMP));
            case TCP:
                return TcpCodec.encode((Tcp) p, ep, ipPseudoHdr(pkt, IpType.TCP));

            case UDP:
                return UdpCodec.encode((Udp) p, ep, ipPseudoHdr(pkt, IpType.UDP));

            case SCTP:
                return SctpCodec.encode((Sctp) p);

            case DHCP:
                return DhcpCodec.encode((Dhcp) p);

            case DHCPV6:
                return DhcpCodecV6.encode((DhcpV6) p);

            case DNS:
                return DnsCodec.encode((Dns) p);

            default:
                return UnknownProtocolCodec.encode((UnknownProtocol) p);
        }
    }

    /**
     * Decodes an Ethernet packet.
     *
     * @param fb encoded frame bytes
     * @return the new packet containing the decoded protocol layers
     * @throws ProtocolException if there is an error parsing from reader
     */
    public static Packet decodeEthernet(byte[] fb) {
        return decodeEthernet(fb, DEF_PKT_LAYER_CAPACITY);
    }

    /**
     * Decodes an Ethernet packet.
     *
     * @param fb encoded frame bytes
     * @param height number of layers to decode before stopping
     * @return the new packet containing the decoded protocol layers
     * @throws ProtocolException if there is an error parsing from reader
     */
    public static Packet decodeEthernet(byte[] fb, int height) {
        PacketReader r = new PacketReader(ByteBuffer.wrap(fb));
        return decode(r, ProtocolId.ETHERNET, height);
    }

    /**
     * Decodes an Ethernet packet.
     *
     * @param r packet reader containing the frame bytes
     * @return the new packet containing the decoded protocol layers
     * @throws ProtocolException if there is an error parsing from reader
     */
    static Packet decodeEthernet(PacketReader r) {
        return decode(r, ProtocolId.ETHERNET, DEF_PKT_LAYER_CAPACITY);
    }

    /**
     * Decodes an Ethernet packet.
     *
     * @param r packet reader containing the frame bytes
     * @param height number of layers to decode before stopping
     * @return the new packet containing the decoded protocol layers
     * @throws ProtocolException if there is an error parsing from reader
     */
    static Packet decodeEthernet(PacketReader r, int height) {
        return decode(r, ProtocolId.ETHERNET, height);
    }

    /**
     * Decodes an Ethernet packet.
     *
     * @param r packet reader containing the frame bytes
     * @param id protocol ID of the first protocol to decode
     * @param height number of layers to decode before stopping
     * @return the new packet containing the decoded protocol layers
     * @throws ProtocolException if there is an error parsing from reader
     */
    private static Packet decode(PacketReader r, ProtocolId id, int height) {
        List<Protocol> layers = new ArrayList<Protocol>(height);

        ProtocolId payloadId = id;
        int payloadLen = r.readableBytes();
        int currHeight = 0;

        /* Keep processing layers until:
         *  -we run out of bytes in the buffer
         *  -we have reached out target layer height
         *  -the previous decoded layer specifies NONE for a payload
         */
        while (r.readableBytes() > 0
                && currHeight++ < height
                && payloadId != ProtocolId.NONE) {
            try {
                DecodedLayer dl = decodeLayer(r, payloadId, payloadLen);

                payloadId = dl.payloadId;
                payloadLen = dl.payloadLen;

                layers.add(dl.layer);

            } catch (ProtocolException e) {
                throw new ProtocolException(e, new Packet(layers));
            }
        }
        return new Packet(layers);
    }

    /**
     * Private data structure returned after a layer is decoded.
     */
    private static class DecodedLayer {

        public Protocol layer;
        public ProtocolId payloadId = ProtocolId.UNKNOWN;
        public int payloadLen = 0;

        private DecodedLayer(Protocol layer, ProtocolId id, int len) {
            this.layer = layer;
            this.payloadId = id;
            this.payloadLen = len;
        }

        private DecodedLayer(Protocol layer, ProtocolId id) {
            this.layer = layer;
            this.payloadId = id;
        }

        private DecodedLayer(Protocol layer) {
            this.layer = layer;
        }
    }

    /**
     * Decodes the next protocol layer from the reader according to the
     * specified protocol ID.
     *
     * @param r packet reader containing the frame bytes
     * @param id protocol ID determined from the lower layers
     * @param len length for this protocol, determined from the lower layers
     * @return new decoded layer data store
     * @throws ProtocolException if there is an error parsing from reader
     */
    private static DecodedLayer decodeLayer(PacketReader r, ProtocolId id,
                                            int len) {
        ProtocolId payloadId = ProtocolId.UNKNOWN;

        switch (id) {

            case ETHERNET:
                Ethernet eth = EthernetCodec.decode(r);
                payloadId = ETHTYPE_2_ID.get(eth.type());
                return new DecodedLayer(eth, payloadId, r.readableBytes());

            case PPP_ETHERNET:
                PppEthernet pppEth = PppEthernetCodec.decode(r);

                if (pppEth.code() != PppEthernet.Code.SESSION_DATA)
                    return new DecodedLayer(pppEth, ProtocolId.NONE);

                payloadId = PppEthernetCodec.PPP_ETH_2_ID.get(pppEth.pppProtocolId());
                payloadId = (ProtocolId.UNKNOWN != payloadId) ? payloadId : ProtocolId.NONE;
                return new DecodedLayer(pppEth, payloadId);

            case MPLS:
                Mpls mpls = MplsCodec.decode(r);
                // This is really, really, really lame but when the MPLS label
                // header is added it replaces the Ethernet type with MPLS and
                // (to my knowledge) there is no way to determine what protocol
                // follows. So, we peek at the next byte and 'guess' whether
                // it is IPv4 or IPv6.
                short u8 = r.peekU8();
                IpVersion v = IpVersion.get((u8 & IP_VERSION_MASK)
                                                >> IP_VERSION_BIT_SHIFT);
                payloadId = (v == IpVersion.V4) ? ProtocolId.IP : ProtocolId.IPV6;
                return new DecodedLayer(mpls, payloadId);

            case ARP:
                Arp arp = ArpCodec.decode(r);
                return new DecodedLayer(arp, ProtocolId.NONE);

            case IP:
                Ip ip = IpCodec.decode(r);
                payloadId = IPTYPE_2_ID.get(ip.type());
                return new DecodedLayer(ip, payloadId,
                                        ip.totalLen() - ip.hdrLen());

            case IPV6:
                IpV6 ipV6 = IpCodecV6.decode(r);
                payloadId = IPTYPE_2_ID.get(ipV6.nextProtocol());
                return new DecodedLayer(ipV6, payloadId,
                                        ipV6.nextProtocolLen());

            case GRE:
                Gre gre = GreCodec.decode(r);
                payloadId = ETHTYPE_2_ID.get(gre.protoType());
                return new DecodedLayer(gre, payloadId, r.readableBytes());
                
            case LLDP:
                Lldp lldp = LldpCodec.decode(r);
                return new DecodedLayer(lldp, ProtocolId.NONE);

            case BDDP:
                Bddp bddp = new Bddp(LldpCodec.decode(r));
                return new DecodedLayer(bddp, ProtocolId.NONE);

            case ICMP:
                Icmp icmp = IcmpCodec.decode(r, len);
                return new DecodedLayer(icmp);

            case ICMPV6:
                IcmpV6 icmpV6 = IcmpCodecV6.decode(r, len);
                return new DecodedLayer(icmpV6);

            case TCP:
                Tcp tcp = TcpCodec.decode(r);
                return new DecodedLayer(tcp, ProtocolId.UNKNOWN, len - tcp.hdrLen());

            case UDP:
                Udp udp = UdpCodec.decode(r);
                if (DhcpCodec.isDhcp(udp.srcPort(), udp.dstPort()))
                    payloadId = ProtocolId.DHCP;
                else if (DhcpCodecV6.isDhcpV6(udp.srcPort(), udp.dstPort()))
                    payloadId = ProtocolId.DHCPV6;
                else if (DnsCodec.isDns(udp.srcPort(), udp.dstPort()))
                    payloadId = ProtocolId.DNS;
                else
                    payloadId = ProtocolId.UNKNOWN;
                return new DecodedLayer(udp, payloadId,
                                        udp.len() - UdpCodec.FIXED_HDR_LEN);

            case SCTP:
                Sctp sctp = SctpCodec.decode(r, len);
                return new DecodedLayer(sctp);

            case DHCP:
                Dhcp dhcp = DhcpCodec.decode(r);
                return new DecodedLayer(dhcp, ProtocolId.NONE);

            case DHCPV6:
                DhcpV6 dhcpV6 = DhcpCodecV6.decode(r, len);
                return new DecodedLayer(dhcpV6, ProtocolId.NONE);

            case DNS:
                Dns dns = DnsCodec.decode(r);
                return new DecodedLayer(dns, ProtocolId.NONE);

            default:
                UnknownProtocol up = UnknownProtocolCodec.decode(r, len);
                return new DecodedLayer(up, ProtocolId.NONE);
        }
    }

}
