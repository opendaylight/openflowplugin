package org.opendaylight.openflowplugin.test.ForwardingConsumer;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OvsFlowInstruction extends FlowClientBuild {
    static final Logger logger = LoggerFactory.getLogger(OvsFlowInstruction.class);
    private static final Pattern NODETYPE = Pattern.compile("openflow:");
    private Integer tcpSrcPort;
    private Long etherType;
    private String tunnelID;
    private String attachedMac;
    private String uri;
    private MacAddress srcMac;
    private MacAddress dstMac;
    private VlanId vlanId;
    private Ipv4Prefix prefix;
    private Integer tos;
    private Short nwTtl;
    private Short ipProtocol;
    private Integer tcpDstPort;
    private Integer udpDstPort;
    private Integer udpSrcPort;
    private boolean ofpDropAction;
    private boolean packetInLLDP;
    private String goToTableId;
    private boolean ofpNormal;
    private Long ofpInPort;
    private Long ofpPort;
    private boolean ofpFlood;
    private boolean ofpLocal;
    private boolean ofpController;
    private Long ofpOutputPort;
    private NodeId nodeId;
    private FlowBuilder flowBuilder;
    private OvsFlowInstruction ovsFlowInstruction;

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

    public MacAddress srcMac() {
        return this.srcMac;
    }

    public MacAddress dstMac() {
        return this.dstMac;
    }

    public VlanId vlanId() {
        return this.vlanId;
    }

    public Ipv4Prefix prefix() {
        return this.prefix;
    }

    public Integer tos() {
        return this.tos;
    }

    public Short nwTtl() {
        return this.nwTtl;
    }

    public Short ipProtocol() {
        return this.ipProtocol;
    }

    public Integer tcpDstPort() {
        return this.tcpDstPort;
    }

    public Integer tcpSrcPort() {
        return this.tcpSrcPort;
    }

    public Integer udpDstPort() {
        return this.udpDstPort;
    }

    public Integer udpSrcPort() {
        return this.udpSrcPort;
    }

    public boolean ofpDropAction() {
        return this.ofpDropAction;
    }

    public boolean packetInLLDP() {
        return this.packetInLLDP;
    }

    public String goToTableId() {
        return this.goToTableId;
    }

    public boolean ofpNormal() {
        return this.ofpNormal;
    }

    public Long ofpInPort() {
        return this.ofpInPort;
    }

    public Long ofpPort() {
        return this.ofpPort;
    }

    public boolean ofpFlood() {
        return this.ofpFlood;
    }

    public boolean ofpLocal() {
        return this.ofpLocal;
    }

    public boolean ofpController() {
        return this.ofpController;
    }

    public Long ofpOutputPort() {
        return this.ofpOutputPort;
    }

    public NodeId nodeId() {
        return this.nodeId;
    }

    public OvsFlowInstruction etherType(final Long etherType) {
        this.etherType = etherType;
        return this;
    }

    public OvsFlowInstruction tunnelID(final String tunnelID) {
        this.tunnelID = tunnelID;
        return this;
    }

    public OvsFlowInstruction attachedMac(final String attachedMac) {
        this.attachedMac = attachedMac;
        return this;
    }

    public OvsFlowInstruction uri(final String uri) {
        this.uri = uri;
        return this;
    }

    public OvsFlowInstruction srcMac(
            final MacAddress srcMac) {
        this.srcMac = srcMac;
        return this;
    }

    public OvsFlowInstruction dstMac(
            final MacAddress dstMac) {
        this.dstMac = dstMac;
        return this;
    }

    public OvsFlowInstruction vlanId(final VlanId vlanId) {
        this.vlanId = vlanId;
        return this;
    }

    public OvsFlowInstruction prefix(
            final Ipv4Prefix prefix) {
        this.prefix = prefix;
        return this;
    }

    public OvsFlowInstruction tos(final Integer tos) {
        this.tos = tos;
        return this;
    }

    public OvsFlowInstruction nwTtl(final Short nwTtl) {
        this.nwTtl = nwTtl;
        return this;
    }

    public OvsFlowInstruction ipProtocol(final Short ipProtocol) {
        this.ipProtocol = ipProtocol;
        return this;
    }

    public OvsFlowInstruction tcpDstPort(final Integer tcpDstPort) {
        this.tcpDstPort = tcpDstPort;
        return this;
    }

    public OvsFlowInstruction tcpSrcPort(final Integer tcpSrcPort) {
        this.tcpSrcPort = tcpSrcPort;
        return this;
    }

    public OvsFlowInstruction udpDstPort(final Integer udpDstPort) {
        this.udpDstPort = udpDstPort;
        return this;
    }

    public OvsFlowInstruction udpSrcPort(final Integer udpSrcPort) {
        this.udpSrcPort = udpSrcPort;
        return this;
    }

    public OvsFlowInstruction ofpDropAction(final boolean ofpDropAction) {
        this.ofpDropAction = ofpDropAction;
        return this;
    }

    public OvsFlowInstruction packetInLLDP(final boolean packetInLLDP) {
        this.packetInLLDP = packetInLLDP;
        return this;
    }

    public OvsFlowInstruction goToTableId(final String goToTableId) {
        this.goToTableId = goToTableId;
        return this;
    }

    public OvsFlowInstruction ofpNormal(final boolean ofpNormal) {
        this.ofpNormal = ofpNormal;
        return this;
    }

    public OvsFlowInstruction ofpInPort(final Long ofpInPort) {
        this.ofpInPort = ofpInPort;
        return this;
    }

    public OvsFlowInstruction ofpPort(final Long ofpPort) {
        this.ofpPort = ofpPort;
        return this;
    }

    public OvsFlowInstruction ofpFlood(final boolean ofpFlood) {
        this.ofpFlood = ofpFlood;
        return this;
    }

    public OvsFlowInstruction ofpLocal(final boolean ofpLocal) {
        this.ofpLocal = ofpLocal;
        return this;
    }

    public OvsFlowInstruction ofpController(final boolean ofpController) {
        this.ofpController = ofpController;
        return this;
    }

    public OvsFlowInstruction ofpOutputPort(final Long ofpOutputPort) {
        this.ofpOutputPort = ofpOutputPort;
        return this;
    }

    public OvsFlowInstruction nodeId(final NodeId nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public FlowBuilder buildClientInstruction(FlowBuilder flowBuilder, OvsFlowInstruction ovsFlowInstruction) {

        InstructionBuilder ib = new InstructionBuilder();
        InstructionsBuilder isb = new InstructionsBuilder();

        this.ovsFlowInstruction = ovsFlowInstruction;
        this.flowBuilder = flowBuilder;

        long dpidLong = parseDpid(ovsFlowInstruction.nodeId());
        logger.info("DPID Extracted -> {}", dpidLong);
        int instructionIndex = 0;
        List<Instruction> instructions = new ArrayList<Instruction>();

        /* Drop Instruction */
        if (ovsFlowInstruction.ofpDropAction()) {
            Of13MDSalInstructionImpl.createDropInstructions(ib);
            logger.info("!!! Is DROP !!!");
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Set VLAN ID */
        if (ovsFlowInstruction.vlanId() != null) {
            VlanId vid = ovsFlowInstruction.vlanId();
            Of13MDSalInstructionImpl.createSetVlanInstructions(ib, vid);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Set Tunnel ID */
        if (ovsFlowInstruction.tunnelID() != null) {
            BigInteger tunnelID = new BigInteger(ovsFlowInstruction.tunnelID());
            Of13MDSalInstructionImpl.createSetTunnelIdInstructions(ib, tunnelID);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Add a GOTO table instruction */
        if (ovsFlowInstruction.goToTableId() != null) {
            Of13MDSalInstructionImpl.createGotoTableInstructions(ib,
                    Short.parseShort(ovsFlowInstruction.goToTableId()));
            logger.info("!!! Is GOTO !!!");
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Send to Reserved Port NORMAL (legacy forwarding) */
        if (ovsFlowInstruction.ofpNormal()) {
            Of13MDSalInstructionImpl.createNormalInstructions(ib);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Send to Reserved Port CONTROLLER (e.g. packet_in event) */
        if (ovsFlowInstruction.ofpController()) {
            Of13MDSalInstructionImpl.createSendToControllerInstructions(ib);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Send to Reserved Port FLOOD */
        if (ovsFlowInstruction.ofpFlood()) {
            Of13MDSalInstructionImpl.createOFPPFloodInstruction(ib);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Send to Reserved Port LOCAL (typically for out of band) */
        if (ovsFlowInstruction.ofpLocal()) {
            Of13MDSalInstructionImpl.createOFPPFloodInstruction(ib);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Send to Reserved IN_PORT (e.g. port the packet was received on) */
        if (ovsFlowInstruction.ofpInPort() != null) {
            Of13MDSalInstructionImpl.createOFPPFloodInstruction(ib);
            addInstruction(ib, instructionIndex++, instructions);
        }
        /* Send to Reserved In_Port (e.g. port the packet was received on) */
        if (ovsFlowInstruction.ofpOutputPort() != null) {
            Of13MDSalInstructionImpl.createOutputPortInstructions
                    (ib, dpidLong, ovsFlowInstruction.ofpOutputPort());
            addInstruction(ib, instructionIndex++, instructions);
        }
        isb.setInstruction(instructions);
        flowBuilder.setInstructions(isb.build());
        logger.trace("InstructionBuilder {} InstructionsBuilders: {}",
                ib.getInstruction(), isb.getInstruction());
        return flowBuilder;
    }

    private void addInstruction(InstructionBuilder ib, int order, List<Instruction> instructions) {
        ib.setOrder(order);
        ib.setKey(new InstructionKey(order));
        instructions.add(ib.build());
    }

    private long parseDpid(final NodeId id) {
        final String nodeId = NODETYPE.matcher(id.getValue()).replaceAll("");
        BigInteger nodeIdBigInt = new BigInteger(nodeId);
        Long dpid = nodeIdBigInt.longValue();
        return dpid;
    }

    @Override
    public String toString() {
        return "NewInstruction{" +
                "tcpSrcPort=" + tcpSrcPort +
                ", etherType=" + etherType +
                ", tunnelID='" + tunnelID + '\'' +
                ", attachedMac='" + attachedMac + '\'' +
                ", uri='" + uri + '\'' +
                ", srcMac=" + srcMac +
                ", dstMac=" + dstMac +
                ", vlanId=" + vlanId +
                ", prefix=" + prefix +
                ", tos=" + tos +
                ", nwTtl=" + nwTtl +
                ", ipProtocol=" + ipProtocol +
                ", tcpDstPort=" + tcpDstPort +
                ", udpDstPort=" + udpDstPort +
                ", udpSrcPort=" + udpSrcPort +
                ", ofpDropAction=" + ofpDropAction +
                ", packetInLLDP=" + packetInLLDP +
                ", goToTableId='" + goToTableId + '\'' +
                ", ofpNormal=" + ofpNormal +
                ", ofpInPort=" + ofpInPort +
                ", ofpPort=" + ofpPort +
                ", ofpFlood=" + ofpFlood +
                ", ofpLocal=" + ofpLocal +
                ", ofpController=" + ofpController +
                ", ofpOutputPort=" + ofpOutputPort +
                ", nodeId=" + nodeId +
                ", flowBuilder=" + flowBuilder +
                ", newInstruction=" + ovsFlowInstruction +
                '}';
    }
}
