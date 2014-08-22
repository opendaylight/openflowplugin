/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.util.*;


/**
 * IANA Interface types.
 * <p>
 * See http://www.iana.org/assignments/ianaiftype-mib
 *
 * @author Steve Britt
 * @author Simon Hunt
 */
public enum IfType {
    /**
     * The only non-IANA value here, this indicates that the interface type is
     * not yet known.
     */
    UNKNOWN(0),
    /**
     * None of the following.
     */
    OTHER(1),
    REGULAR_1822(2),
    HDH_1822(3),
    DDN_X25(4),
    RFC_877_X25(5),
    /**
     * For all Ethernet-like interfaces, regardless of speed, as per RFC
     * 3635.
     */
    ETHERNET_CSMACD(6),
    /**
     * Deprecated via RFC 3635; interface type 6 should be used instead.
     */
    ISO8_802_3_CSMACD(7),
    ISO8_802_4_TOKENBUS(8),
    ISO8_802_5_TOKENRING(9),
    ISO8_802_6_MAN(10),
    /**
     * Deprecated via RFC 3635; interface type 6 should be used instead.
     */
    STAR_LAN(11),
    PROTEON_10MBIT(12),
    PROTEON_80MBIT(13),
    HYPERCHANNEL(14),
    FDDI(15),
    LAPB(16),
    SDLC(17),
    DS1(18),
    /**
     * Obsolete; see DS1 MIB.
     */
    E1(19),
    /**
     * No longer used; see RFC 2127.
     */
    BASIC_ISDN(20),
    /**
     * No longer used; see RFC 2127.
     */
    PRIMARY_ISDN(21),
    PROPRIETARY_POINT_TO_POINT_SERIAL(22),
    PPP(23),
    SOFTWARE_LOOPBACK(24),
    /**
     * CLNP over IP.
     */
    EON(25),
    ETHERNET_3MBIT(26),
    /**
     * XNS over IP.
     */
    NSIP(27),
    /**
     * Generic SLIP.
     */
    SLIP(28),
    /**
     * ULTRA technologies.
     */
    ULTRA(29),
    DS3(30),
    /**
     * SMDS, coffee.
     */
    SIP(31),
    /**
     * DTE only.
     */
    FRAME_RELAY(32),
    RS232(33),
    PARALLEL_PORT(34),
    ARCNET(35),
    ARCNET_PLUS(36),
    /**
     * ATM cells.
     */
    ATM(37),
    MIO_X25(38),
    /**
     * SONET or SDH.
     */
    SONET(39),
    X25_PLE(40),
    ISO8_802_211C(41),
    LOCALTALK(42),
    SMDS_DXI(43),
    /**
     * FRNETSERV MIB.
     */
    FRAME_RELAY_SERVICE(44),
    V35(45),
    HSSI(46),
    HIPPI(47),
    /**
     * Generic modem.
     */
    MODEM(48),
    /**
     * AAL5 over ATM.
     */
    AAL5(49),
    SONET_PATH(50),
    SONET_VT(51),
    /**
     * SMDS inter-carrier interface.
     */
    SMDS_ICIP(52),
    PROPRIETARY_VIRTUAL(53),
    HP_VLAN(53),
    PROPRIETARY_MULTIPLEXOR(54),
    HP_MESH(54),
    HP_TRUNK(54),
    /**
     * 100 Base-VG.
     */
    IEEE_802_12_HUNDRED_VG_ANYLAN(55),
    FIBRE_CHANNEL(56),
    HIPPI_INTERFACE(57),
    /**
     * Obsolete; interface types 32 or 44 should be used instead.
     */
    FRAME_RELAY_INTERCONNECT(58),
    /**
     * ATM emulated LAN for 802.3.
     */
    AFLANE_802_3(59),
    /**
     * ATM emulated LAN for 802.5.
     */
    AFLANE_802_5(60),
    /**
     * ATM emulated circuit.
     */
    CCT_EMULATED(61),
    /**
     * Obsoleted via RFC 3635; interface type 6 should be used instead.
     */
    FAST_ETHERNET(62),
    /**
     * ISDN and X.25.
     */
    ISDN(63),
    /**
     * CCITT V.11/X.21.
     */
    V11_X21(64),
    /**
     * CCITT V.36.
     */
    V36(65),
    /**
     * CCITT G703 at 64kbps.
     */
    G703_64KBIT(66),
    /**
     * Obsolete; see DS1 MIB.
     */
    G703_2MBIT(67),
    /**
     * SNA QLLC.
     */
    QLLC(68),
    /**
     * Obsoleted via RFC 3635; interface type 6 should be used instead.
     */
    FAST_ETHERNET_FX(69),
    FAST_ETHERNET_FX_SFP(70),
    CHANNEL(70),
    /**
     * Radio spread spectrum.
     */
    IEEE_802_11(71),
    /**
     * IBM System 360/370 OEMI channel.
     */
    IBM_370_OEM_CHANNEL(72),
    /**
     * IBM Enterprise Systems Connection.
     */
    ESCON(73),
    /**
     * Data link switching.
     */
    DLSW(74),
    /**
     * ISDN S/T interface.
     */
    ISDN_ST(75),
    /**
     * ISDN U interface.
     */
    ISDN_U(76),
    /**
     * Link Access Protocol D.
     */
    LAPD(77),
    /**
     * IP switching objects.
     */
    IP_SWITCH(78),
    /**
     * Remote source bridge routing.
     */
    RSRB(79),
    /**
     * ATM logical port.
     */
    ATM_LOGICAL(80),
    /**
     * Digital Signal Level 0.
     */
    DS0(81),
    /**
     * Group of DS0s on the same DS1.
     */
    DS0_BUNDLE(82),
    /**
     * Bisynchronous protocol.
     */
    BISYNC(83),
    /**
     * Asynchronous protocol.
     */
    ASYNC(84),
    /**
     * Combat Net Radio.
     */
    CNR(85),
    ISO8_802_5R_DTR(86),
    /**
     * External Position Location Report System.
     */
    EPLRS(87),
    /**
     * AppleTalk Remote Access Protocol.
     */
    ARAP(88),
    PROPRIETARY_CONNECTIONLESS(89),
    /**
     * CCITT-ITU X.29 host PAD protocol.
     */
    X29_PAD(90),
    /**
     * CCITT-ITU X.3 terminal PAD protocol.
     */
    X3_PAD(91),
    /**
     * Multiprotocol interconnect over Frame Relay.
     */
    FRAME_RELAY_MPI(92),
    /**
     * CCITT-ITU X.213 protocol.
     */
    X213(93),
    /**
     * Asymmetric Digital Subscriber Loop.
     */
    ADSL(94),
    /**
     * Rate-Adapting Digital Subscriber Loop.
     */
    RADSL(95),
    /**
     * Symmetric Digital Subscriber Loop.
     */
    SDSL(96),
    /**
     * Very High-Speed Digital Subscriber Loop.
     */
    VDSL(97),
    ISO8_802_5_CRFP(98),
    /**
     * Myricom Myrinet.
     */
    MYRINET(99),
    /**
     * Voice receive and transmit.
     */
    VOICE_EM(100),
    /**
     * Voice Foreign Exchange Office.
     */
    VOICE_FXO(101),
    /**
     * Voice Foreign Exchange Station.
     */
    VOICE_FXS(102),
    VOICE_ENCAPSULATION(103),
    VOICE_OVER_IP_ENCAPSULATION(104),
    ATM_DXI(105),
    ATM_FUNI(106),
    ATM_IMA(107),
    PPP_MULTILINK_BUNDLE(108),
    /**
     * IBM IP over CDLC.
     */
    IP_OVER_CDLC(109),
    /**
     * IBM Common Link Access to workstation.
     */
    IP_OVER_CLAW(110),
    /**
     * IBM stack-to-stack.
     */
    STACK_TO_STACK(111),
    /**
     * IBM virtual IP address.
     */
    VIPA(112),
    /**
     * IBM multi-protocol channel support.
     */
    MPC(113),
    IP_OVER_ATM(114),
    ISO8_802_5J_FIBER(115),
    /**
     * IBM twin-axial data link control.
     */
    TDLC(116),
    /**
     * Deprecated via RFC 3635; interface type 6 should be used instead.
     */
    GIGABIT_ETHERNET(117),
    /**
     * Deprecated via RFC 3635; interface type 6 should be used instead.
     */
    GIGABIT_ETHERNET_LX(118),
    HDLC(118),
    /**
     * Deprecated via RFC 3635; interface type 6 should be used instead.
     */
    GIGABIT_ETHERNET_T(119),
    /**
     * Link Access Protocol F.
     */
    LAPF(119),
    GIGABIT_ETHERNET_STK(120),
    V37(120),
    GIGABIT_ETHERNET_LH(121),
    /**
     * X.25 multi-link protocol.
     */
    X25_MLP(121),
    TEN_GIGABIT_ETHERNET_CX4(122),
    X25_HUNT_GROUP(122),
    GIGABIT_ETHERNET_ESP(123),
    TRANSP_HDLC(123),
    TEN_GIGABIT_ETHERNET_SR(124),
    INTERLEAVE_CHANNEL(124),
    TEN_GIGABIT_ETHERNET_ER(125),
    FAST_CHANNEL(125),
    TEN_GIGABIT_ETHERNET_LR(126),
    /**
     * IP (for APPN HPR in IP networks).
     */
    IP(126),
    /**
     * Cable television MAC layer.
     */
    DOCS_CABLE_MAC_LAYER(127),
    /**
     * Cable television downstream interface.
     */
    DOCS_CABLE_DOWNSTREAM(128),
    /**
     * Cable television upstream interface.
     */
    DOCS_CABLE_UPSTREAM(129),
    HPICFTC_SFP_LRM(130),
    /**
     * Avalon Parallel Processor.
     */
    A12MBIT_PP_SWITCH(130),
    /**
     * Encapsulation interface.
     */
    TUNNEL(131),
    /**
     * Coffee pot.
     */
    COFFEE(132),
    /**
     * Circuit emulation service.
     */
    CES(133),
    ATM_SUB_INTERFACE(134),
    /**
     * Layer 2 virtual LAN using 802.1Q.
     */
    L2VLAN(135),
    /**
     * Layer 3 virtual LAN using IP.
     */
    L3_IP_VLAN(136),
    /**
     * Layer 3 virtual LAN using IPX.
     */
    L3_IPX_VLAN(137),
    /**
     * IP over power lines.
     */
    DIGITAL_POWERLINE(138),
    /**
     * Multi-media mail over IP.
     */
    MEDIA_MAIL_OVER_IP(139),
    /**
     * Dynamic synchronous transfer mode.
     */
    DTM(140),
    /**
     * Data Communications Network.
     */
    DCN(141),
    IP_FORWARDING(142),
    /**
     * Multi-rate symmetric DSL.
     */
    MSDSL(143),
    /**
     * IEEE 1394 high-performance serial bus.
     */
    IEEE_1394(144),
    /**
     * HIPPI 6400.
     */
    IF_GSN(145),
    DVB_RCC_MAC_LAYER(146),
    DVB_RCC_DOWNSTREAM(147),
    DVB_RCC_UPSTREAM(148),
    ATM_VIRTUAL(149),
    MPLS_TUNNEL(150),
    /**
     * Spatial Reuse Protocol.
     */
    SRP(151),
    VOICE_OVER_ATM(152),
    VOICE_OVER_FRAME_RELAY(153),
    /**
     * Digital Subscriber Loop over ISDN.
     */
    IDSL(154),
    /**
     * Avici Composite Link Interface.
     */
    COMPOSITE_LINK(155),
    SS7_SIGNALING_LINK(156),
    PROPRIETARY_WIRELESS_POINT_TO_POINT(157),
    FRAME_FORWARD(158),
    /**
     * Multi-protocol over ATM AAL5.
     */
    RFC1483(159),
    USB(160),
    IEEE_802_3AD_LINK_AGGREGATE(161),
    BGP_POLICY_ACCOUNTING(162),
    FRF_16_MULTILINK_FRAME_RELAY_BUNDLE(163),
    H323_GATEKEEPER(164),
    /**
     * H323 voice and video proxy.
     */
    H323_PROXY(165),
    MPLS(166),
    MULTI_FREQUENCY_SIGNALING_LINK(167),
    /**
     * High bitrate DSL (second generation).
     */
    HDSL_2(168),
    /**
     * Multi-rate HDSL2.
     */
    SHDSL(169),
    /**
     * Facility data link 4kbps on a DS1.
     */
    DS1_FDL(170),
    /**
     * Packet over SONET/SDH interface.
     */
    POS(171),
    DVB_ASI_INPUT(172),
    DVB_ASI_OUTPUT(173),
    /**
     * Power link communications.
     */
    PLC(174),
    /**
     * Non-facility associated signaling.
     */
    NFAS(175),
    TR008(176),
    /**
     * Remote Digital Terminal.
     */
    GR303_RDT(177),
    /**
     * Integrated Digital Terminal.
     */
    GR303_IDT(178),
    ISUP(179),
    /**
     * Cisco proprietary MAC layer.
     */
    PROPRIETARY_DOCS_WIRELESS_MAC_LAYER(180),
    /**
     * Cisco proprietary downstream.
     */
    PROPRIETARY_DOCS_WIRELESS_DOWNSTREAM(181),
    /**
     * Cisco proprietary upstream.
     */
    PROPRIETARY_DOCS_WIRELESS_UPSTREAM(182),
    /**
     * HIPERLAN type 2 radio interface.
     */
    HIPER_LAN_2(183),
    /**
     * Deprecated for IEEE 802.16f standard; interface type 237 should be
     * used instead.
     */
    PROPRIETARY_BROADBAND_WIRELESS_AP_TO_MULTIPOINT(184),
    SONET_OVERHEAD_CHANNEL(185),
    DIGITAL_WRAPPER_OVERHEAD_CHANNEL(186),
    /**
     * ATM adaptation layer 2.
     */
    AAL2(187),
    /**
     * MAC layer over radio links.
     */
    RADIO_MAC(188),
    /**
     * ATM over radio links.
     */
    ATM_RADIO(189),
    INTER_MACHINE_TRUNKS(190),
    /**
     * Multiple virtual lines DSL
     */
    MVL(191),
    LONG_REACH_DSL(192),
    FRAME_RELAY_DLCI_ENDPOINT(193),
    ATM_VCI_ENDPOINT(194),
    OPTICAL_CHANNEL(195),
    OPTICAL_TRANSPORT(196),
    PROPRIETARY_ATM(197),
    VOICE_OVER_CABLE(198),
    INFINIBAND(199),
    TE_LINK(200),
    Q_2931(201),
    VIRTUAL_TRUNK_GROUP(202),
    SIP_TRUNK_GROUP(203),
    SIP_SIGNALING(204),
    /**
     * Cable television upstream channel.
     */
    DOCS_CABLE_UPSTREAM_CHANNEL(205),
    /**
     * Acorn Econet.
     */
    ECONET(206),
    /**
     * FSAN 155Mbps Symmetrical PON interface.
     */
    PON_155(207),
    /**
     * FSAN 622Mbps Symmetrical PON interface.
     */
    PON_622(208),
    TRANSPARENT_BRIDGE(209),
    /**
     * Interface common to multiple lines.
     */
    LINE_GROUP(210),
    /**
     * Voice E&M Feature Group D.
     */
    VOICE_EM_FGD(211),
    /**
     * Voice E&M Feature Group D exchange access (North America).
     */
    VOICE_FGD_EANA(212),
    /**
     * Voice Direct Inward Dialing.
     */
    VOICE_DID(213),
    MPEG_TRANSPORT(214),
    /**
     * Deprecated.
     */
    SIX_TO_FOUR(215),
    /**
     * GPRS Tunneling Protocol.
     */
    GTP(216),
    PDN_ETHERLOOP_1(217),
    PDN_ETHERLOOP_2(218),
    OPTICAL_CHANNEL_GROUP(219),
    /**
     * Home PNA ITU-T G.989.
     */
    HOME_PNA(220),
    /**
     * Generic Framing Procedure.
     */
    GFP(221),
    /**
     * Layer 2 VLAN using Cisco ISL.
     */
    CISCO_ISL_VLAN(222),
    /**
     * Actelis proprietary MetaLOOP high-speed link.
     */
    ACTELIS_METALOOP(223),
    FCIP_LINK(224),
    /**
     * Resilient Packet Ring.
     */
    RPR(225),
    /**
     * RF Qam interface.
     */
    QAM(226),
    /**
     * Link Management Protocol.
     */
    LMP(227),
    /**
     * Cambridge Broadband Networks Limited VectaStar.
     */
    CBL_VECTASTAR(228),
    /**
     * Cable television modular CMTS downstream interface.
     */
    DOCS_CABLE_MCTMS_DOWNSTREAM(229),
    /**
     * Asymmetric Digital Subscriber Loop version 2.  Deprecated; interface
     * type 238 should be used instead.
     */
    ADSL2(230),
    MAC_SEC_CONTROLLED(231),
    MAC_SEC_UNCONTROLLED(232),
    /**
     * Avici Optical Ethernet aggregate.
     */
    AVICI_OPTICAL_ETHERNET(233),
    ATM_BOND(234),
    /**
     * Voice E&M Feature Group D operator services.
     */
    VOICE_FGD_OS(235),
    /**
     * Multi-media over Coax Alliance (MoCA).
     */
    MOCA_VERSION_1(236),
    IEEE_802_16_WMAN(237),
    /**
     * Asymmetric Digital Subscriber Loop version 2+ and all variants.
     */
    ADSL2_PLUS(238),
    DVB_RCS_MAC_LAYER(239),
    /**
     * DVB Satellite TDM.
     */
    DVB_TDM(240),
    DVB_RCS_TDMA(241),
    /**
     * LAPS based on ITU-T X.86/Y.1323.
     */
    X86_LAPS(242),
    WWAN_3GPP(243),
    WWAN_3GPP_2(244),
    /**
     * Voice P-phone EBS physical interface.
     */
    VOICE_EBS(245),
    PSEUDO_WIRE(246),
    /**
     * Internal LAN on a bridge per IEEE 802.1ap.
     */
    ILAN(247),
    /**
     * Provider instance port on a bridge per IEEE 802.1ah PBB.
     */
    PIP(248),
    /**
     * Alcatel-Lucent Ethernet Link Protection.
     */
    ALU_ELP(249),
    /**
     * Gigabit-capable passive optical networks (G-PON) per ITU-T G.948.
     */
    GPON(250),
    /**
     * Very High-Speed Digital Subscriber Loop version 2 (per ITU-T
     * G.993.2).
     */
    VDSL2(251),
    /**
     * WLAN profile interface.
     */
    CAP_WAP_802_11_PROFILE(252),
    /**
     * WLAN BSS interface.
     */
    CAP_WAP_802_11_BSS(253),
    /**
     * WLAN Virtual Radio interface.
     */
    CAP_WAP_802_11_WTP_VIRTUAL_RADIO(254);

    //=========================================================================

    // Map for reverse lookup.
    private static final Map<Integer, List<IfType>> LOOKUP =
            new HashMap<Integer, List<IfType>>();

    // List of one entry to be returned when the looked-up ifType is unknown.
    private static final ArrayList<IfType> UNKNOWN_LIST =
            new ArrayList<IfType>(1);

    // IANA-assigned interface type value.
    private int value;


    /**
     * Initialize IANA-assigned numeric value to interface type LOOKUP and
     * empty ifType search result.
     */
    static {
        List<IfType> labelsForType;
        for (IfType type : values()) {
            labelsForType = LOOKUP.get(type.getValue());
            if (labelsForType == null) {
                labelsForType = new ArrayList<IfType>();
                LOOKUP.put(type.getValue(), labelsForType);
            }
            if (! labelsForType.contains(type))
                labelsForType.add(type);
        }
        UNKNOWN_LIST.add(UNKNOWN);
    }


    /**
     * Private constructor.
     *
     * @param value the IANA-assigned numeric value
     */
    private IfType(int value) {
        this.value = value;
    }


    /**
     * Returns the numeric value associated with this interface type constant.
     *
     * @return the numeric value
     */
    public int getValue() {
        return value;
    }


    /**
     * Set of all constants that are link aggregate interfaces.
     */
    // TODO: Complete the list of link aggregate interfaces.
    //       The interface types here are constrained to the same small set
    //       employed in PCM 3.x and should be expanded for completeness.
    private static final EnumSet<IfType> LINK_AGG =
            EnumSet.of(IEEE_802_3AD_LINK_AGGREGATE);


    /**
     * Returns true if this interface type corresponds to a
     * virtual link aggregate interface; false otherwise.
     *
     * @return true if this interface type corresponds to a virtual link
     *         aggregate interface; false otherwise
     */
    public boolean isLinkAggregateInterface() {
        return LINK_AGG.contains(this);
    }


    /**
     * Set of all constants that are multiplexor interfaces.
     */
    // TODO: Complete list of multiplexor interfaces
    //       The interface types here are constrained to the same small set
    //       employed in PCM 3.x and should be expanded for completeness.
    private static final EnumSet<IfType> MULTIPLEXOR =
            EnumSet.of(PROPRIETARY_MULTIPLEXOR);


    /**
     * Returns true if this interface type corresponds to a
     * virtual multiplexor interface; false otherwise.
     *
     * @return true if this interface type corresponds to a virtual multiplexor
     *         interface; false otherwise
     */
    public boolean isMultiplexorInterface() {
        return MULTIPLEXOR.contains(this);
    }


    /**
     * Set of all constants that are physical ports.
     */
    // TODO: Complete list of physical port types.
    //       The interface types here are constrained to the same small set
    //       employed in PCM 3.x and should be expanded for completeness.
    private static final EnumSet<IfType> PHYS_PORT = EnumSet.of(
            DOCS_CABLE_MAC_LAYER,
            ETHERNET_CSMACD,
            FAST_ETHERNET,
            FAST_ETHERNET_FX,
            FAST_ETHERNET_FX_SFP,
            GIGABIT_ETHERNET,
            GIGABIT_ETHERNET_ESP,
            GIGABIT_ETHERNET_LH,
            GIGABIT_ETHERNET_LX,
            GIGABIT_ETHERNET_STK,
            GIGABIT_ETHERNET_T,
            HPICFTC_SFP_LRM,
            IEEE_802_11,
            IEEE_802_12_HUNDRED_VG_ANYLAN,
            ISO8_802_3_CSMACD,
            STAR_LAN,
            TEN_GIGABIT_ETHERNET_CX4,
            TEN_GIGABIT_ETHERNET_ER,
            TEN_GIGABIT_ETHERNET_LR,
            TEN_GIGABIT_ETHERNET_SR
    );


    /**
     * Returns true if this interface type corresponds to a
     * physical port; false otherwise.
     *
     * @return true if this interface type corresponds to a physical port;
     *         false otherwise
     */
    public boolean isPhysicalPort() {
        return PHYS_PORT.contains(this);
    }


    /**
     * Set of all constants that are physical ports with fiber connectors.
     */
    // TODO: Complete list of physical port types with fiber connectors.
    //       The interface types here are constrained to the same small set
    //       employed in PCM 3.x and should be expanded for completeness.
    private static final EnumSet<IfType> PHYS_FIBER_PORT = EnumSet.of(
            FAST_ETHERNET_FX,
            FAST_ETHERNET_FX_SFP,
            GIGABIT_ETHERNET,
            GIGABIT_ETHERNET_ESP,
            GIGABIT_ETHERNET_LH,
            GIGABIT_ETHERNET_LX,
            GIGABIT_ETHERNET_STK,
            GIGABIT_ETHERNET_T,
            TEN_GIGABIT_ETHERNET_CX4,
            TEN_GIGABIT_ETHERNET_ER,
            TEN_GIGABIT_ETHERNET_LR,
            TEN_GIGABIT_ETHERNET_SR
    );


    /**
     * Returns true if this interface type is a physical
     * interface type that uses a fiber media connector; false otherwise.
     *
     * @return true if this interface type is a physical interface type that
     *         uses a fiber connector; false otherwise
     */
    public boolean isFiberConnector() {
        return PHYS_FIBER_PORT.contains(this);
    }


    /**
     * Set of all constants that are virtual interfaces.
     */
    // TODO : Complete the list of virtual interfaces.
    //        The interface types here are constrained to the same small set
    //        employed in PCM 3.x and should be expanded for completeness.
    private static final EnumSet<IfType> VIRTUAL = EnumSet.of(
            IP_FORWARDING,
            L2VLAN,
            PROPRIETARY_VIRTUAL
    );


    /**
     * Returns true if this interface type corresponds to a
     * virtual interface; false otherwise.
     *
     * @return true if the specified interface type corresponds to a virtual
     *         interface; false otherwise
     */
    public boolean isVirtualInterface() {
        return VIRTUAL.contains(this);
    }


    /**
     * Returns the interface type constant corresponding to the specified
     * numeric value.
     *
     * @param value the numeric value of the required interface type
     * @return the interface type constant corresponding to the specified
     *         numeric value, or {@code null} if no such interface type exists
     */
    public static List<IfType> valueOf(int value) {
        List<IfType> result = LOOKUP.get(value);
        return ((result == null) ? Collections.unmodifiableList(UNKNOWN_LIST)
                                 : Collections.unmodifiableList(result));
    }
}
