package org.opendaylight.openflowplugin.test.ForwardingConsumer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class OvsFlowMatch extends FlowClientBuild{
    static final Logger logger = LoggerFactory.getLogger(OvsFlowMatch.class);

    private Long etherType;
    private String tunnelID;
    private String attachedMac;
    private String uri;
    private VlanId vlanId;
    private Integer tos;
    private Short nwTtl;
    private Integer inPortNumber;
    private Integer ipProtocol;
    private String srcMacAddr;
    private String dstMacAddr;
    private String macAddressMask;
    private String srcIpPrefix;
    private String dstIpPrefix;
    private Integer srcTcpPort;
    private Integer dstTcpPort;
    private Integer srcUdpPort;
    private Integer dstUdpPort;
    private Long dpid;
    private NodeId nodeId;
    private FlowBuilder flowBuilder;
    private OvsFlowMatch ovsFlowMatch;

    public Long etherType() {
        return this.etherType;
    }

    public String tunnelID() {
        return this.tunnelID;
    }

    public String attachedMac() {
        return this.attachedMac;
    }

    public String uri() {
        return this.uri;
    }

    public VlanId vlanId() {
        return this.vlanId;
    }

    public Integer tos() {
        return this.tos;
    }

    public Short nwTtl() {
        return this.nwTtl;
    }

    public Integer inPortNumber() {
        return this.inPortNumber;
    }

    public Integer ipProtocol() {
        return this.ipProtocol;
    }

    public String srcMacAddr() {
        return this.srcMacAddr;
    }

    public String dstMacAddr() {
        return this.dstMacAddr;
    }

    public String macAddressMask() {
        return this.macAddressMask;
    }

    public String srcIpPrefix() {
        return this.srcIpPrefix;
    }

    public String dstIpPrefix() {
        return this.dstIpPrefix;
    }

    public Integer srcTcpPort() {
        return this.srcTcpPort;
    }

    public Integer dstTcpPort() {
        return this.dstTcpPort;
    }

    public Integer srcUdpPort() {
        return this.srcUdpPort;
    }

    public Integer dstUdpPort() {
        return this.dstUdpPort;
    }

    public Long dpid() {
        return this.dpid;
    }

    public NodeId nodeId() {
        return this.nodeId;
    }

    public OvsFlowMatch etherType(final Long etherType) {
        this.etherType = etherType;
        return this;
    }

    public OvsFlowMatch tunnelID(final String tunnelID) {
        this.tunnelID = tunnelID;
        return this;
    }

    public OvsFlowMatch attachedMac(final String attachedMac) {
        this.attachedMac = attachedMac;
        return this;
    }

    public OvsFlowMatch uri(final String uri) {
        this.uri = uri;
        return this;
    }

    public OvsFlowMatch vlanId(final VlanId vlanId) {
        this.vlanId = vlanId;
        return this;
    }

    public OvsFlowMatch tos(final Integer tos) {
        this.tos = tos;
        return this;
    }

    public OvsFlowMatch nwTtl(final Short nwTtl) {
        this.nwTtl = nwTtl;
        return this;
    }

    public OvsFlowMatch inPortNumber(final Integer inPortNumber) {
        this.inPortNumber = inPortNumber;
        return this;
    }

    public OvsFlowMatch ipProtocol(final Integer ipProtocol) {
        this.ipProtocol = ipProtocol;
        return this;
    }

    public OvsFlowMatch srcMacAddr(final String srcMacAddr) {
        this.srcMacAddr = srcMacAddr;
        return this;
    }

    public OvsFlowMatch dstMacAddr(final String dstMacAddr) {
        this.dstMacAddr = dstMacAddr;
        return this;
    }

    public OvsFlowMatch macAddressMask(final String macAddressMask) {
        this.macAddressMask = macAddressMask;
        return this;
    }

    public OvsFlowMatch srcIpPrefix(final String srcIpPrefix) {
        this.srcIpPrefix = srcIpPrefix;
        return this;
    }

    public OvsFlowMatch dstIpPrefix(final String dstIpPrefix) {
        this.dstIpPrefix = dstIpPrefix;
        return this;
    }

    public OvsFlowMatch srcTcpPort(final Integer srcTcpPort) {
        this.srcTcpPort = srcTcpPort;
        return this;
    }

    public OvsFlowMatch dstTcpPort(final Integer dstTcpPort) {
        this.dstTcpPort = dstTcpPort;
        return this;
    }

    public OvsFlowMatch srcUdpPort(final Integer srcUdpPort) {
        this.srcUdpPort = srcUdpPort;
        return this;
    }

    public OvsFlowMatch dstUdpPort(final Integer dstUdpPort) {
        this.dstUdpPort = dstUdpPort;
        return this;
    }

    public OvsFlowMatch dpid(final Long dpid) {
        this.dpid = dpid;
        return this;
    }

    public OvsFlowMatch nodeId(final NodeId nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public FlowBuilder buildNewMatch(FlowBuilder flowBuilder, OvsFlowMatch ovsFlowMatch) {

        this.flowBuilder = flowBuilder;
        this.ovsFlowMatch = ovsFlowMatch;
        MatchBuilder matchBuilder = new MatchBuilder();
        // Match: IP protocol
        if (ovsFlowMatch.ipProtocol() != null) {
            logger.info("NEWMATCH ipProtocool() ->  {}", ipProtocol());
            Of13MDSalMatchImpl.createProtocolMatch(matchBuilder, ipProtocol);
        }
        // Match: Ethertype
        if (ovsFlowMatch.etherType() != null) {
            EtherType etherType = new EtherType(ovsFlowMatch.etherType());
            logger.info("Consumer ClientMatch EtherType: {}", etherType.getValue());
            Of13MDSalMatchImpl.createEtherTypeMatch(matchBuilder, etherType);
        }
        // Match: Tunnel ID
        if (ovsFlowMatch.tunnelID() != null) {
            BigInteger tunID = new BigInteger(ovsFlowMatch.tunnelID());
            Of13MDSalMatchImpl.createTunnelIDMatch(matchBuilder, tunID);
        }
        // Match: Source Mac Address and Mask (typically broadcast/multicast)
        if (this.srcMacAddr() != null && this.macAddressMask() != null) {
            MacAddress macAddress = new MacAddress(this.srcMacAddr());
            MacAddress macAddressMask = new MacAddress(this.macAddressMask());
            Of13MDSalMatchImpl.createSrcEthMatch(matchBuilder, macAddress, macAddressMask);
        }
        // Match: Source Mac Address w/o Mask
        if (this.srcMacAddr() != null && this.macAddressMask() == null) {
            MacAddress macAddress = new MacAddress(this.srcMacAddr());
            Of13MDSalMatchImpl.createSrcEthMatch(matchBuilder, macAddress, null);
        }
        // Match: Destination Mac Address and Mask (typically broadcast/multicast)
        if (this.dstMacAddr() != null && this.dstMacAddr() != null) {
            MacAddress macAddress = new MacAddress(this.dstMacAddr());
            MacAddress macAddressMask = new MacAddress(this.macAddressMask());
            Of13MDSalMatchImpl.createDstEthMatch(matchBuilder, macAddress, macAddressMask);
        }
        // Match: Destination Mac Address w/o Mask
        if (this.dstMacAddr() != null && this.macAddressMask() == null) {
            MacAddress macAddress = new MacAddress(this.dstMacAddr());
            Of13MDSalMatchImpl.createDstEthMatch(matchBuilder, macAddress, null);
        }
        // Match: L3 Source IPv4
        if (this.srcIpPrefix() != null) {
            Ipv4Prefix ipv4Address = new Ipv4Prefix(this.srcIpPrefix);
            Of13MDSalMatchImpl.createSrcIPv4Match(matchBuilder, ipv4Address);
        }
        // Match: L3 Destination IPv4
        if (this.dstIpPrefix() != null) {
            Ipv4Prefix ipv4Address = new Ipv4Prefix(this.dstIpPrefix);
            Of13MDSalMatchImpl.createDstIPv4Match(matchBuilder, ipv4Address);
        }
        // Match: L4 Source TCP Port
        if (this.srcTcpPort() != null) {
            PortNumber srcTcpPort = new PortNumber(this.srcTcpPort());
            Of13MDSalMatchImpl.createSrcTcpPortMatch(matchBuilder, srcTcpPort);
        }
        // Match: L4 Destination TCP Port
        if (this.dstTcpPort() != null) {
            PortNumber dstTcpPort = new PortNumber(this.dstTcpPort());
            Of13MDSalMatchImpl.createDstTcpPortMatch(matchBuilder, dstTcpPort);
        }
        // Match: L4 Source UDP Port
        if (this.srcUdpPort() != null) {
            PortNumber srcUdpPort = new PortNumber(this.srcUdpPort());
            Of13MDSalMatchImpl.createSrcUdpPortMatch(matchBuilder, srcUdpPort);
        }
        // Match: L4 Destination UDP Port
        if (this.dstUdpPort() != null) {
            PortNumber dstUdpPort = new PortNumber(this.dstUdpPort());
            Of13MDSalMatchImpl.createDstUdpPortMatch(matchBuilder, dstUdpPort);
        }
        // Match: Ingress OpenFlow Port (virtual or physical)
        if (ovsFlowMatch.inPortNumber() != null && ovsFlowMatch.nodeId() != null) {
            Of13MDSalMatchImpl.createInPortMatch(matchBuilder, nodeId,
                    this.inPortNumber());
        }

        flowBuilder.setMatch(matchBuilder.build());
        return flowBuilder;
    }

    @Override
    public String toString() {
        return "NewMatch{" +
                "etherType=" + etherType +
                ", tunnelID='" + tunnelID + '\'' +
                ", attachedMac='" + attachedMac + '\'' +
                ", uri='" + uri + '\'' +
                ", vlanId=" + vlanId +
                ", tos=" + tos +
                ", nwTtl=" + nwTtl +
                ", inPortNumber=" + inPortNumber +
                ", ipProtocol=" + ipProtocol +
                ", srcMacAddr='" + srcMacAddr + '\'' +
                ", dstMacAddr='" + dstMacAddr + '\'' +
                ", macAddressMask='" + macAddressMask + '\'' +
                ", srcIpPrefix='" + srcIpPrefix + '\'' +
                ", dstIpPrefix='" + dstIpPrefix + '\'' +
                ", srcTcpPort=" + srcTcpPort +
                ", dstTcpPort=" + dstTcpPort +
                ", srcUdpPort=" + srcUdpPort +
                ", dstUdpPort=" + dstUdpPort +
                ", dpid=" + dpid +
                ", nodeId=" + nodeId +
                ", flowBuilder=" + flowBuilder +
                ", this=" + this +
                '}';
    }
}