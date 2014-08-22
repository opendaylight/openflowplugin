/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline.impl;

/**
 * Denotes different TableAttribute types.
 *
 * @author Pramod Shanbhag
 */
public enum TableAttributeCode {
    INSTRUCTIONS_GOTO_TABLE(0),
    INSTRUCTIONS_WRITE_METADATA(1),
    INSTRUCTIONS_WRITE_ACTIONS(2),
    INSTRUCTIONS_APPLY_ACTIONS(3),
    INSTRUCTIONS_CLEAR_ACTIONS(4),
    INSTRUCTIONS_METER(5),
    INSTRUCTIONS_EXPERIMENTER(6),
    INSTRUCTIONS_MISS_GOTO_TABLE(7),
    INSTRUCTIONS_MISS_WRITE_METADATA(8),
    INSTRUCTIONS_MISS_WRITE_ACTIONS(9),
    INSTRUCTIONS_MISS_APPLY_ACTIONS(10),
    INSTRUCTIONS_MISS_CLEAR_ACTIONS(11),
    INSTRUCTIONS_MISS_METER(12),
    INSTRUCTIONS_MISS_EXPERIMENTER(13),
    NEXT_TABLES(14),
    NEXT_TABLES_MISS(15),
    WRITE_ACTIONS_OUTPUT(16),
    WRITE_ACTIONS_COPY_TTL_OUT(17),
    WRITE_ACTIONS_COPY_TTL_IN(18),
    WRITE_ACTIONS_SET_MPLS_TTL(19),
    WRITE_ACTIONS_DEC_MPLS_TTL(20),
    WRITE_ACTIONS_PUSH_VLAN(21),
    WRITE_ACTIONS_POP_VLAN(22),
    WRITE_ACTIONS_PUSH_MPLS(23),
    WRITE_ACTIONS_POP_MPLS(24),
    WRITE_ACTIONS_SET_QUEUE(25),
    WRITE_ACTIONS_GROUP(26),
    WRITE_ACTIONS_SET_NW_TTL(27),
    WRITE_ACTIONS_DEC_NW_TTL(28),
    WRITE_ACTIONS_SET_FIELD(29),
    WRITE_ACTIONS_PUSH_PBB(30),
    WRITE_ACTIONS_POP_PBB(31),
    WRITE_ACTIONS_EXPERIMENTER(32),
    WRITE_ACTIONS_MISS_OUTPUT(33),
    WRITE_ACTIONS_MISS_COPY_TTL_OUT(34),
    WRITE_ACTIONS_MISS_COPY_TTL_IN(35),
    WRITE_ACTIONS_MISS_SET_MPLS_TTL(36),
    WRITE_ACTIONS_MISS_DEC_MPLS_TTL(37),
    WRITE_ACTIONS_MISS_PUSH_VLAN(38),
    WRITE_ACTIONS_MISS_POP_VLAN(39),
    WRITE_ACTIONS_MISS_PUSH_MPLS(40),
    WRITE_ACTIONS_MISS_POP_MPLS(41),
    WRITE_ACTIONS_MISS_SET_QUEUE(42),
    WRITE_ACTIONS_MISS_GROUP(43),
    WRITE_ACTIONS_MISS_SET_NW_TTL(44),
    WRITE_ACTIONS_MISS_DEC_NW_TTL(45),
    WRITE_ACTIONS_MISS_SET_FIELD(46),
    WRITE_ACTIONS_MISS_PUSH_PBB(47),
    WRITE_ACTIONS_MISS_POP_PBB(48),
    WRITE_ACTIONS_MISS_EXPERIMENTER(49),
    APPLY_ACTIONS_OUTPUT(50),
    APPLY_ACTIONS_COPY_TTL_OUT(51),
    APPLY_ACTIONS_COPY_TTL_IN(52),
    APPLY_ACTIONS_SET_MPLS_TTL(53),
    APPLY_ACTIONS_DEC_MPLS_TTL(54),
    APPLY_ACTIONS_PUSH_VLAN(55),
    APPLY_ACTIONS_POP_VLAN(56),
    APPLY_ACTIONS_PUSH_MPLS(57),
    APPLY_ACTIONS_POP_MPLS(58),
    APPLY_ACTIONS_SET_QUEUE(59),
    APPLY_ACTIONS_GROUP(60),
    APPLY_ACTIONS_SET_NW_TTL(61),
    APPLY_ACTIONS_DEC_NW_TTL(62),
    APPLY_ACTIONS_SET_FIELD(63),
    APPLY_ACTIONS_PUSH_PBB(64),
    APPLY_ACTIONS_POP_PBB(65),
    APPLY_ACTIONS_EXPERIMENTER(66),
    APPLY_ACTIONS_MISS_OUTPUT(67),
    APPLY_ACTIONS_MISS_COPY_TTL_OUT(68),
    APPLY_ACTIONS_MISS_COPY_TTL_IN(69),
    APPLY_ACTIONS_MISS_SET_MPLS_TTL(70),
    APPLY_ACTIONS_MISS_DEC_MPLS_TTL(71),
    APPLY_ACTIONS_MISS_PUSH_VLAN(72),
    APPLY_ACTIONS_MISS_POP_VLAN(73),
    APPLY_ACTIONS_MISS_PUSH_MPLS(74),
    APPLY_ACTIONS_MISS_POP_MPLS(75),
    APPLY_ACTIONS_MISS_SET_QUEUE(76),
    APPLY_ACTIONS_MISS_GROUP(77),
    APPLY_ACTIONS_MISS_SET_NW_TTL(78),
    APPLY_ACTIONS_MISS_DEC_NW_TTL(79),
    APPLY_ACTIONS_MISS_SET_FIELD(80),
    APPLY_ACTIONS_MISS_PUSH_PBB(81),
    APPLY_ACTIONS_MISS_POP_PBB(82),
    APPLY_ACTIONS_MISS_EXPERIMENTER(83),
    MATCH_IN_PORT(84),
    MATCH_IN_PHY_PORT(85),
    MATCH_METADATA(86),
    MATCH_ETH_DST(87),
    MATCH_ETH_SRC(88),
    MATCH_ETH_TYPE(89),
    MATCH_VLAN_VID(90),
    MATCH_VLAN_PCP(91),
    MATCH_IP_DSCP(92),
    MATCH_IP_ECN(93),
    MATCH_IP_PROTO(94),
    MATCH_IPV4_SRC(95),
    MATCH_IPV4_DST(96),
    MATCH_TCP_SRC(97),
    MATCH_TCP_DST(98),
    MATCH_UDP_SRC(99),
    MATCH_UDP_DST(100),
    MATCH_SCTP_SRC(101),
    MATCH_SCTP_DST(102),
    MATCH_ICMPV4_TYPE(103),
    MATCH_ICMPV4_CODE(104),
    MATCH_ARP_OP(105),
    MATCH_ARP_SPA(106),
    MATCH_ARP_TPA(107),
    MATCH_ARP_SHA(108),
    MATCH_ARP_THA(109),
    MATCH_IPV6_SRC(110),
    MATCH_IPV6_DST(111),
    MATCH_IPV6_FLABEL(112),
    MATCH_ICMPV6_TYPE(113),
    MATCH_ICMPV6_CODE(114),
    MATCH_IPV6_ND_TARGET(115),
    MATCH_IPV6_ND_SLL(116),
    MATCH_IPV6_ND_TLL(117),
    MATCH_MPLS_LABEL(118),
    MATCH_MPLS_TC(119),
    MATCH_MPLS_BOS(120),
    MATCH_PBB_ISID(121),
    MATCH_TUNNEL_ID(122),
    MATCH_IPV6_EXTHDR(123),
    WILDCARDS_IN_PORT(124),
    WILDCARDS_IN_PHY_PORT(125),
    WILDCARDS_METADATA(126),
    WILDCARDS_ETH_DST(127),
    WILDCARDS_ETH_SRC(128),
    WILDCARDS_ETH_TYPE(129),
    WILDCARDS_VLAN_VID(130),
    WILDCARDS_VLAN_PCP(131),
    WILDCARDS_IP_DSCP(132),
    WILDCARDS_IP_ECN(133),
    WILDCARDS_IP_PROTO(134),
    WILDCARDS_IPV4_SRC(135),
    WILDCARDS_IPV4_DST(136),
    WILDCARDS_TCP_SRC(137),
    WILDCARDS_TCP_DST(138),
    WILDCARDS_UDP_SRC(139),
    WILDCARDS_UDP_DST(140),
    WILDCARDS_SCTP_SRC(141),
    WILDCARDS_SCTP_DST(142),
    WILDCARDS_ICMPV4_TYPE(143),
    WILDCARDS_ICMPV4_CODE(144),
    WILDCARDS_ARP_OP(145),
    WILDCARDS_ARP_SPA(146),
    WILDCARDS_ARP_TPA(147),
    WILDCARDS_ARP_SHA(148),
    WILDCARDS_ARP_THA(149),
    WILDCARDS_IPV6_SRC(150),
    WILDCARDS_IPV6_DST(151),
    WILDCARDS_IPV6_FLABEL(152),
    WILDCARDS_ICMPV6_TYPE(153),
    WILDCARDS_ICMPV6_CODE(154),
    WILDCARDS_IPV6_ND_TARGET(155),
    WILDCARDS_IPV6_ND_SLL(156),
    WILDCARDS_IPV6_ND_TLL(157),
    WILDCARDS_MPLS_LABEL(158),
    WILDCARDS_MPLS_TC(159),
    WILDCARDS_MPLS_BOS(160),
    WILDCARDS_PBB_ISID(161),
    WILDCARDS_TUNNEL_ID(162),
    WILDCARDS_IPV6_EXTHDR(163),
    WRITE_SETFIELD_IN_PORT(164),
    WRITE_SETFIELD_IN_PHY_PORT(165),
    WRITE_SETFIELD_METADATA(166),
    WRITE_SETFIELD_ETH_DST(167),
    WRITE_SETFIELD_ETH_SRC(168),
    WRITE_SETFIELD_ETH_TYPE(169),
    WRITE_SETFIELD_VLAN_VID(170),
    WRITE_SETFIELD_VLAN_PCP(171),
    WRITE_SETFIELD_IP_DSCP(172),
    WRITE_SETFIELD_IP_ECN(173),
    WRITE_SETFIELD_IP_PROTO(174),
    WRITE_SETFIELD_IPV4_SRC(175),
    WRITE_SETFIELD_IPV4_DST(176),
    WRITE_SETFIELD_TCP_SRC(177),
    WRITE_SETFIELD_TCP_DST(178),
    WRITE_SETFIELD_UDP_SRC(179),
    WRITE_SETFIELD_UDP_DST(180),
    WRITE_SETFIELD_SCTP_SRC(181),
    WRITE_SETFIELD_SCTP_DST(182),
    WRITE_SETFIELD_ICMPV4_TYPE(183),
    WRITE_SETFIELD_ICMPV4_CODE(184),
    WRITE_SETFIELD_ARP_OP(185),
    WRITE_SETFIELD_ARP_SPA(186),
    WRITE_SETFIELD_ARP_TPA(187),
    WRITE_SETFIELD_ARP_SHA(188),
    WRITE_SETFIELD_ARP_THA(189),
    WRITE_SETFIELD_IPV6_SRC(190),
    WRITE_SETFIELD_IPV6_DST(191),
    WRITE_SETFIELD_IPV6_FLABEL(192),
    WRITE_SETFIELD_ICMPV6_TYPE(193),
    WRITE_SETFIELD_ICMPV6_CODE(194),
    WRITE_SETFIELD_IPV6_ND_TARGET(195),
    WRITE_SETFIELD_IPV6_ND_SLL(196),
    WRITE_SETFIELD_IPV6_ND_TLL(197),
    WRITE_SETFIELD_MPLS_LABEL(198),
    WRITE_SETFIELD_MPLS_TC(199),
    WRITE_SETFIELD_MPLS_BOS(200),
    WRITE_SETFIELD_PBB_ISID(201),
    WRITE_SETFIELD_TUNNEL_ID(202),
    WRITE_SETFIELD_IPV6_EXTHDR(203),
    WRITE_SETFIELD_MISS_IN_PORT(204),
    WRITE_SETFIELD_MISS_IN_PHY_PORT(205),
    WRITE_SETFIELD_MISS_METADATA(206),
    WRITE_SETFIELD_MISS_ETH_DST(207),
    WRITE_SETFIELD_MISS_ETH_SRC(208),
    WRITE_SETFIELD_MISS_ETH_TYPE(209),
    WRITE_SETFIELD_MISS_VLAN_VID(210),
    WRITE_SETFIELD_MISS_VLAN_PCP(211),
    WRITE_SETFIELD_MISS_IP_DSCP(212),
    WRITE_SETFIELD_MISS_IP_ECN(213),
    WRITE_SETFIELD_MISS_IP_PROTO(214),
    WRITE_SETFIELD_MISS_IPV4_SRC(215),
    WRITE_SETFIELD_MISS_IPV4_DST(216),
    WRITE_SETFIELD_MISS_TCP_SRC(217),
    WRITE_SETFIELD_MISS_TCP_DST(218),
    WRITE_SETFIELD_MISS_UDP_SRC(219),
    WRITE_SETFIELD_MISS_UDP_DST(220),
    WRITE_SETFIELD_MISS_SCTP_SRC(221),
    WRITE_SETFIELD_MISS_SCTP_DST(222),
    WRITE_SETFIELD_MISS_ICMPV4_TYPE(223),
    WRITE_SETFIELD_MISS_ICMPV4_CODE(224),
    WRITE_SETFIELD_MISS_ARP_OP(225),
    WRITE_SETFIELD_MISS_ARP_SPA(226),
    WRITE_SETFIELD_MISS_ARP_TPA(227),
    WRITE_SETFIELD_MISS_ARP_SHA(228),
    WRITE_SETFIELD_MISS_ARP_THA(229),
    WRITE_SETFIELD_MISS_IPV6_SRC(230),
    WRITE_SETFIELD_MISS_IPV6_DST(231),
    WRITE_SETFIELD_MISS_IPV6_FLABEL(232),
    WRITE_SETFIELD_MISS_ICMPV6_TYPE(233),
    WRITE_SETFIELD_MISS_ICMPV6_CODE(234),
    WRITE_SETFIELD_MISS_IPV6_ND_TARGET(235),
    WRITE_SETFIELD_MISS_IPV6_ND_SLL(236),
    WRITE_SETFIELD_MISS_IPV6_ND_TLL(237),
    WRITE_SETFIELD_MISS_MPLS_LABEL(238),
    WRITE_SETFIELD_MISS_MPLS_TC(239),
    WRITE_SETFIELD_MISS_MPLS_BOS(240),
    WRITE_SETFIELD_MISS_PBB_ISID(241),
    WRITE_SETFIELD_MISS_TUNNEL_ID(242),
    WRITE_SETFIELD_MISS_IPV6_EXTHDR(243),
    APPLY_SETFIELD_IN_PORT(244),
    APPLY_SETFIELD_IN_PHY_PORT(245),
    APPLY_SETFIELD_METADATA(246),
    APPLY_SETFIELD_ETH_DST(247),
    APPLY_SETFIELD_ETH_SRC(248),
    APPLY_SETFIELD_ETH_TYPE(249),
    APPLY_SETFIELD_VLAN_VID(250),
    APPLY_SETFIELD_VLAN_PCP(251),
    APPLY_SETFIELD_IP_DSCP(252),
    APPLY_SETFIELD_IP_ECN(253),
    APPLY_SETFIELD_IP_PROTO(254),
    APPLY_SETFIELD_IPV4_SRC(255),
    APPLY_SETFIELD_IPV4_DST(256),
    APPLY_SETFIELD_TCP_SRC(257),
    APPLY_SETFIELD_TCP_DST(258),
    APPLY_SETFIELD_UDP_SRC(259),
    APPLY_SETFIELD_UDP_DST(260),
    APPLY_SETFIELD_SCTP_SRC(261),
    APPLY_SETFIELD_SCTP_DST(262),
    APPLY_SETFIELD_ICMPV4_TYPE(263),
    APPLY_SETFIELD_ICMPV4_CODE(264),
    APPLY_SETFIELD_ARP_OP(265),
    APPLY_SETFIELD_ARP_SPA(266),
    APPLY_SETFIELD_ARP_TPA(267),
    APPLY_SETFIELD_ARP_SHA(268),
    APPLY_SETFIELD_ARP_THA(269),
    APPLY_SETFIELD_IPV6_SRC(270),
    APPLY_SETFIELD_IPV6_DST(271),
    APPLY_SETFIELD_IPV6_FLABEL(272),
    APPLY_SETFIELD_ICMPV6_TYPE(273),
    APPLY_SETFIELD_ICMPV6_CODE(274),
    APPLY_SETFIELD_IPV6_ND_TARGET(275),
    APPLY_SETFIELD_IPV6_ND_SLL(276),
    APPLY_SETFIELD_IPV6_ND_TLL(277),
    APPLY_SETFIELD_MPLS_LABEL(278),
    APPLY_SETFIELD_MPLS_TC(279),
    APPLY_SETFIELD_MPLS_BOS(280),
    APPLY_SETFIELD_PBB_ISID(281),
    APPLY_SETFIELD_TUNNEL_ID(282),
    APPLY_SETFIELD_IPV6_EXTHDR(283),
    APPLY_SETFIELD_MISS_IN_PORT(284),
    APPLY_SETFIELD_MISS_IN_PHY_PORT(285),
    APPLY_SETFIELD_MISS_METADATA(286),
    APPLY_SETFIELD_MISS_ETH_DST(287),
    APPLY_SETFIELD_MISS_ETH_SRC(288),
    APPLY_SETFIELD_MISS_ETH_TYPE(289),
    APPLY_SETFIELD_MISS_VLAN_VID(290),
    APPLY_SETFIELD_MISS_VLAN_PCP(291),
    APPLY_SETFIELD_MISS_IP_DSCP(292),
    APPLY_SETFIELD_MISS_IP_ECN(293),
    APPLY_SETFIELD_MISS_IP_PROTO(294),
    APPLY_SETFIELD_MISS_IPV4_SRC(295),
    APPLY_SETFIELD_MISS_IPV4_DST(296),
    APPLY_SETFIELD_MISS_TCP_SRC(297),
    APPLY_SETFIELD_MISS_TCP_DST(298),
    APPLY_SETFIELD_MISS_UDP_SRC(299),
    APPLY_SETFIELD_MISS_UDP_DST(300),
    APPLY_SETFIELD_MISS_SCTP_SRC(301),
    APPLY_SETFIELD_MISS_SCTP_DST(302),
    APPLY_SETFIELD_MISS_ICMPV4_TYPE(303),
    APPLY_SETFIELD_MISS_ICMPV4_CODE(304),
    APPLY_SETFIELD_MISS_ARP_OP(305),
    APPLY_SETFIELD_MISS_ARP_SPA(306),
    APPLY_SETFIELD_MISS_ARP_TPA(307),
    APPLY_SETFIELD_MISS_ARP_SHA(308),
    APPLY_SETFIELD_MISS_ARP_THA(309),
    APPLY_SETFIELD_MISS_IPV6_SRC(310),
    APPLY_SETFIELD_MISS_IPV6_DST(311),
    APPLY_SETFIELD_MISS_IPV6_FLABEL(312),
    APPLY_SETFIELD_MISS_ICMPV6_TYPE(313),
    APPLY_SETFIELD_MISS_ICMPV6_CODE(314),
    APPLY_SETFIELD_MISS_IPV6_ND_TARGET(315),
    APPLY_SETFIELD_MISS_IPV6_ND_SLL(316),
    APPLY_SETFIELD_MISS_IPV6_ND_TLL(317),
    APPLY_SETFIELD_MISS_MPLS_LABEL(318),
    APPLY_SETFIELD_MISS_MPLS_TC(319),
    APPLY_SETFIELD_MISS_MPLS_BOS(320),
    APPLY_SETFIELD_MISS_PBB_ISID(321),
    APPLY_SETFIELD_MISS_TUNNEL_ID(322),
    APPLY_SETFIELD_MISS_IPV6_EXTHDR(323),
    ;
    private final int code;
    
    TableAttributeCode(int code) {
        this.code = code;
    }
    
    //TODO : error handling for no match.
    public static TableAttributeCode codeByName(String name) {
        TableAttributeCode code = null;
        for (TableAttributeCode c: values())
            if (c.name().equals(name)) {
                code = c;
                break;
            }
        if (code == null)
            throw new RuntimeException("No matching code : " + name);
        return code;
    }

    public int code() {
        return code;
    }
    
}
