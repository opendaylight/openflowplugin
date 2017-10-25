/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;

/**
 * Convertor data used in {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchResponseConvertor}
 * containing Openflow version, datapath ID and various builders, because match response convertor cases depends
 * on each other and requires shared builders
 */
public class MatchResponseConvertorData extends VersionDatapathIdConvertorData {
    private MatchBuilder matchBuilder;
    private EthernetMatchBuilder ethernetMatchBuilder;
    private VlanMatchBuilder vlanMatchBuilder;
    private IpMatchBuilder ipMatchBuilder;
    private TcpMatchBuilder tcpMatchBuilder;
    private UdpMatchBuilder udpMatchBuilder;
    private SctpMatchBuilder sctpMatchBuilder;
    private Icmpv4MatchBuilder icmpv4MatchBuilder;
    private Icmpv6MatchBuilder icmpv6MatchBuilder;
    private Ipv4MatchBuilder ipv4MatchBuilder;
    private Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder;
    private Ipv6MatchArbitraryBitMaskBuilder ipv6MatchArbitraryBitMaskBuilder;
    private ArpMatchBuilder arpMatchBuilder;
    private Ipv6MatchBuilder ipv6MatchBuilder;
    private ProtocolMatchFieldsBuilder protocolMatchFieldsBuilder;
    private TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder;
    private TcpFlagsMatchBuilder tcpFlagsMatchBuilder;
    private Class<? extends MatchField> oxmMatchField;

    /**
     * Instantiates a new Match convertor data.
     *
     * @param version the version
     */
    public MatchResponseConvertorData(short version) {
        super(version);
    }

    /**
     * Gets match builder.
     *
     * @return the match builder
     */
    public MatchBuilder getMatchBuilder() {
        return matchBuilder;
    }

    /**
     * Sets match builder.
     *
     * @param matchBuilder the match builder
     */
    public void setMatchBuilder(MatchBuilder matchBuilder) {
        this.matchBuilder = matchBuilder;
    }

    /**
     * Gets ethernet match builder.
     *
     * @return the ethernet match builder
     */
    public EthernetMatchBuilder getEthernetMatchBuilder() {
        return ethernetMatchBuilder;
    }

    /**
     * Sets ethernet match builder.
     *
     * @param ethernetMatchBuilder the ethernet match builder
     */
    public void setEthernetMatchBuilder(EthernetMatchBuilder ethernetMatchBuilder) {
        this.ethernetMatchBuilder = ethernetMatchBuilder;
    }

    /**
     * Gets vlan match builder.
     *
     * @return the vlan match builder
     */
    public VlanMatchBuilder getVlanMatchBuilder() {
        return vlanMatchBuilder;
    }

    /**
     * Sets vlan match builder.
     *
     * @param vlanMatchBuilder the vlan match builder
     */
    public void setVlanMatchBuilder(VlanMatchBuilder vlanMatchBuilder) {
        this.vlanMatchBuilder = vlanMatchBuilder;
    }

    /**
     * Gets ip match builder.
     *
     * @return the ip match builder
     */
    public IpMatchBuilder getIpMatchBuilder() {
        return ipMatchBuilder;
    }

    /**
     * Sets ip match builder.
     *
     * @param ipMatchBuilder the ip match builder
     */
    public void setIpMatchBuilder(IpMatchBuilder ipMatchBuilder) {
        this.ipMatchBuilder = ipMatchBuilder;
    }

    /**
     * Gets tcp match builder.
     *
     * @return the tcp match builder
     */
    public TcpMatchBuilder getTcpMatchBuilder() {
        return tcpMatchBuilder;
    }

    /**
     * Sets tcp match builder.
     *
     * @param tcpMatchBuilder the tcp match builder
     */
    public void setTcpMatchBuilder(TcpMatchBuilder tcpMatchBuilder) {
        this.tcpMatchBuilder = tcpMatchBuilder;
    }

    /**
     * Gets udp match builder.
     *
     * @return the udp match builder
     */
    public UdpMatchBuilder getUdpMatchBuilder() {
        return udpMatchBuilder;
    }

    /**
     * Sets udp match builder.
     *
     * @param udpMatchBuilder the udp match builder
     */
    public void setUdpMatchBuilder(UdpMatchBuilder udpMatchBuilder) {
        this.udpMatchBuilder = udpMatchBuilder;
    }

    /**
     * Gets sctp match builder.
     *
     * @return the sctp match builder
     */
    public SctpMatchBuilder getSctpMatchBuilder() {
        return sctpMatchBuilder;
    }

    /**
     * Sets sctp match builder.
     *
     * @param sctpMatchBuilder the sctp match builder
     */
    public void setSctpMatchBuilder(SctpMatchBuilder sctpMatchBuilder) {
        this.sctpMatchBuilder = sctpMatchBuilder;
    }

    /**
     * Gets icmpv 4 match builder.
     *
     * @return the icmpv 4 match builder
     */
    public Icmpv4MatchBuilder getIcmpv4MatchBuilder() {
        return icmpv4MatchBuilder;
    }

    /**
     * Sets icmpv 4 match builder.
     *
     * @param icmpv4MatchBuilder the icmpv 4 match builder
     */
    public void setIcmpv4MatchBuilder(Icmpv4MatchBuilder icmpv4MatchBuilder) {
        this.icmpv4MatchBuilder = icmpv4MatchBuilder;
    }

    /**
     * Gets icmpv 6 match builder.
     *
     * @return the icmpv 6 match builder
     */
    public Icmpv6MatchBuilder getIcmpv6MatchBuilder() {
        return icmpv6MatchBuilder;
    }

    /**
     * Sets icmpv 6 match builder.
     *
     * @param icmpv6MatchBuilder the icmpv 6 match builder
     */
    public void setIcmpv6MatchBuilder(Icmpv6MatchBuilder icmpv6MatchBuilder) {
        this.icmpv6MatchBuilder = icmpv6MatchBuilder;
    }

    /**
     * Gets ipv 4 match builder.
     *
     * @return the ipv 4 match builder
     */
    public Ipv4MatchBuilder getIpv4MatchBuilder() {
        return ipv4MatchBuilder;
    }

    /**
     * Sets ipv 4 match builder.
     *
     * @param ipv4MatchBuilder the ipv 4 match builder
     */
    public void setIpv4MatchBuilder(Ipv4MatchBuilder ipv4MatchBuilder) {
        this.ipv4MatchBuilder = ipv4MatchBuilder;
    }

    /**
     * Gets ipv 4 match arbitrary bit mask builder.
     *
     * @return the ipv 4 match arbitrary bit mask builder
     */
    public Ipv4MatchArbitraryBitMaskBuilder getIpv4MatchArbitraryBitMaskBuilder() {
        return ipv4MatchArbitraryBitMaskBuilder;
    }

    /**
     * Sets ipv 4 match arbitrary bit mask builder.
     *
     * @param ipv4MatchArbitraryBitMaskBuilder the ipv 4 match arbitrary bit mask builder
     */
    public void setIpv4MatchArbitraryBitMaskBuilder(Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder) {
        this.ipv4MatchArbitraryBitMaskBuilder = ipv4MatchArbitraryBitMaskBuilder;
    }

    /**
     * Gets ipv 6 match arbitrary bit mask builder.
     *
     * @return the ipv 6 match arbitrary bit mask builder
     */
    public Ipv6MatchArbitraryBitMaskBuilder getIpv6MatchArbitraryBitMaskBuilder() {
        return ipv6MatchArbitraryBitMaskBuilder;
    }

    /**
     * Sets ipv 6 match arbitrary bit mask builder.
     *
     * @param ipv6MatchArbitraryBitMaskBuilder the ipv 6 match arbitrary bit mask builder
     */
    public void setIpv6MatchArbitraryBitMaskBuilder(Ipv6MatchArbitraryBitMaskBuilder ipv6MatchArbitraryBitMaskBuilder) {
        this.ipv6MatchArbitraryBitMaskBuilder = ipv6MatchArbitraryBitMaskBuilder;
    }

    /**
     * Gets arp match builder.
     *
     * @return the arp match builder
     */
    public ArpMatchBuilder getArpMatchBuilder() {
        return arpMatchBuilder;
    }

    /**
     * Sets arp match builder.
     *
     * @param arpMatchBuilder the arp match builder
     */
    public void setArpMatchBuilder(ArpMatchBuilder arpMatchBuilder) {
        this.arpMatchBuilder = arpMatchBuilder;
    }

    /**
     * Gets ipv 6 match builder.
     *
     * @return the ipv 6 match builder
     */
    public Ipv6MatchBuilder getIpv6MatchBuilder() {
        return ipv6MatchBuilder;
    }

    /**
     * Sets ipv 6 match builder.
     *
     * @param ipv6MatchBuilder the ipv 6 match builder
     */
    public void setIpv6MatchBuilder(Ipv6MatchBuilder ipv6MatchBuilder) {
        this.ipv6MatchBuilder = ipv6MatchBuilder;
    }

    /**
     * Gets protocol match fields builder.
     *
     * @return the protocol match fields builder
     */
    public ProtocolMatchFieldsBuilder getProtocolMatchFieldsBuilder() {
        return protocolMatchFieldsBuilder;
    }

    /**
     * Sets protocol match fields builder.
     *
     * @param protocolMatchFieldsBuilder the protocol match fields builder
     */
    public void setProtocolMatchFieldsBuilder(ProtocolMatchFieldsBuilder protocolMatchFieldsBuilder) {
        this.protocolMatchFieldsBuilder = protocolMatchFieldsBuilder;
    }

    /**
     * Gets tunnel ipv 4 match builder.
     *
     * @return the tunnel ipv 4 match builder
     */
    public TunnelIpv4MatchBuilder getTunnelIpv4MatchBuilder() {
        return tunnelIpv4MatchBuilder;
    }

    /**
     * Sets tunnel ipv 4 match builder.
     *
     * @param tunnelIpv4MatchBuilder the tunnel ipv 4 match builder
     */
    public void setTunnelIpv4MatchBuilder(TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder) {
        this.tunnelIpv4MatchBuilder = tunnelIpv4MatchBuilder;
    }

    /**
     * Sets tcp flags match builder.
     *
     * @param tcpFlagsMatchBuilder the tcp flags match builder
     */
    public void setTcpFlagsMatchBuilder(TcpFlagsMatchBuilder tcpFlagsMatchBuilder) {
        this.tcpFlagsMatchBuilder = tcpFlagsMatchBuilder;
    }

    /**
     * Gets tcp flags match builder.
     *
     * @return the tcp flags match builder
     */
    public TcpFlagsMatchBuilder getTcpFlagsMatchBuilder() {
        return tcpFlagsMatchBuilder;
    }

    /**
     * Sets oxm match field.
     *
     * @param oxmMatchField the oxm match field
     */
    public void setOxmMatchField(Class<? extends MatchField> oxmMatchField) {
        this.oxmMatchField = oxmMatchField;
    }

    /**
     * Gets oxm match field.
     *
     * @return the oxm match field
     */
    public Class<? extends MatchField> getOxmMatchField() {
        return oxmMatchField;
    }
}
