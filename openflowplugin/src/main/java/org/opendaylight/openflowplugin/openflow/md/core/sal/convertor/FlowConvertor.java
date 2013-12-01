/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.BosMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.BosMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EcnMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EcnMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4CodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4CodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4TypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4TypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6CodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6CodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6TypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6TypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6FlabelMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6FlabelMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IsidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IsidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsLabelMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsLabelMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OpCodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OpCodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ProtocolNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ProtocolNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntry.PseudoField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MatchTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting a MD-SAL Flow into the OF flow mod
 */
public class FlowConvertor {
    private static final Logger logger = LoggerFactory.getLogger(FlowConvertor.class);
    private static final String PREFIX_SEPARATOR = "/";

    // Default values for when things are null
    private static final  BigInteger DEFAULT_COOKIE = BigInteger.ZERO; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final TableId DEFAULT_TABLE_ID = new TableId(new Long(0)); // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Integer DEFAULT_IDLE_TIMEOUT = new Integer(5*60); // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Integer DEFAULT_HARD_TIMEOUT = new Integer(10*60); // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Integer DEFAULT_PRIORITY = new Integer(100);  // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Long DEFAULT_BUFFER_ID = Long.parseLong("ffffffff", 16);  // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_PORT = OFPP_ANY; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_GROUP = OFPG_ANY; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_FLOW_REM = true; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_CHECK_OVERLAP = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_RESET_COUNTS = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_NO_PKT_COUNTS = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_NO_BYT_COUNTS = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Class<? extends MatchTypeBase> DEFAULT_MATCH_TYPE = OxmMatchType.class;

    public static FlowModInput toFlowModInput(Flow flow, short version) {
        FlowModInputBuilder flowMod = new FlowModInputBuilder();
        if(flow.getCookie() != null){
            flowMod.setCookie(flow.getCookie());
        } else {
            flowMod.setCookie(DEFAULT_COOKIE);
        }

        if (flow.getCookieMask() != null) {
            flowMod.setCookieMask(new BigInteger(flow.getCookieMask().toString()));
        } else {
            flowMod.setCookieMask(DEFAULT_COOKIE_MASK);
        }

        if (flow.getTableId() != null) {
            flowMod.setTableId(new TableId(flow.getTableId().longValue()));
        } else {
            flowMod.setTableId(DEFAULT_TABLE_ID);
        }

        if (flow instanceof AddFlowInput) {
            flowMod.setCommand(FlowModCommand.OFPFCADD);
        } else if (flow instanceof RemoveFlowInput) {
            if (flow.isStrict() != null && flow.isStrict()) {
                flowMod.setCommand(FlowModCommand.OFPFCDELETESTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCDELETE);
            }
        } else if (flow instanceof UpdateFlowInput) {
            if (flow.isStrict()) {
                flowMod.setCommand(FlowModCommand.OFPFCMODIFYSTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCMODIFY);
            }
        }
        if(flow.getIdleTimeout() != null) {
            flowMod.setIdleTimeout(flow.getIdleTimeout());
        } else {
            flowMod.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        }
        if(flow.getHardTimeout() != null) {
            flowMod.setHardTimeout(flow.getHardTimeout());
        } else {
            flowMod.setHardTimeout(DEFAULT_HARD_TIMEOUT);
        }
        if(flow.getPriority() != null) {
            flowMod.setPriority(flow.getPriority());
        } else {
            flowMod.setPriority(DEFAULT_PRIORITY);
        }
        if(flow.getBufferId() != null ) {
            flowMod.setBufferId(flow.getBufferId());
        } else {
            flowMod.setBufferId(DEFAULT_BUFFER_ID);
        }

        if (flow.getOutPort() != null) {
            flowMod.setOutPort(new PortNumber(flow.getOutPort().longValue()));
        } else {
            flowMod.setOutPort(new PortNumber(DEFAULT_OUT_PORT));
        }
        if(flow.getOutGroup() != null) {
            flowMod.setOutGroup(flow.getOutGroup());
        } else {
            flowMod.setOutGroup(DEFAULT_OUT_GROUP);
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags flowModFlags = flow.getFlags();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags ofFlowModFlags = null;
        if (flowModFlags != null) {
            ofFlowModFlags = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags(
                    flowModFlags.isCHECKOVERLAP(), flowModFlags.isNOBYTCOUNTS(), flowModFlags.isNOPKTCOUNTS(),
                    flowModFlags.isRESETCOUNTS(), flowModFlags.isSENDFLOWREM());
        } else {
            ofFlowModFlags = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags(
                    DEFAULT_OFPFF_CHECK_OVERLAP,DEFAULT_OFPFF_NO_BYT_COUNTS,DEFAULT_OFPFF_NO_PKT_COUNTS,
                    DEFAULT_OFPFF_RESET_COUNTS,DEFAULT_OFPFF_FLOW_REM);
        }
        flowMod.setFlags(ofFlowModFlags);

        if (flow.getMatch() != null) {
            MatchBuilder matchBuilder = new MatchBuilder();
            matchBuilder.setMatchEntries(toMatch(flow.getMatch()));
            matchBuilder.setType(DEFAULT_MATCH_TYPE);
            flowMod.setMatch(matchBuilder.build());
        }

        if (flow.getInstructions() != null) {
            flowMod.setInstructions(toInstructions(flow.getInstructions(), version));
        }
        flowMod.setVersion(version);
        return flowMod.build();
    }

    public static List<MatchEntries> toMatch(
            org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match) {

        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        List<MatchEntries> matchEntriesList = new ArrayList<>();

        if (match.getInPort() != null) {
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setHasMask(false);
            matchEntriesBuilder.setOxmMatchField(InPort.class);
            PortNumberMatchEntryBuilder port = new PortNumberMatchEntryBuilder();
            port.setPortNumber(new PortNumber(match.getInPort()));
            matchEntriesBuilder.addAugmentation(PortNumberMatchEntry.class, port.build());
            matchEntriesList.add(matchEntriesBuilder.build());
        }

        if (match.getInPhyPort() != null) {
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setHasMask(false);
            matchEntriesBuilder.setOxmMatchField(InPhyPort.class);
            PortNumberMatchEntryBuilder phyPort = new PortNumberMatchEntryBuilder();
            phyPort.setPortNumber(new PortNumber(match.getInPhyPort()));
            matchEntriesBuilder.addAugmentation(PortNumberMatchEntry.class, phyPort.build());
            matchEntriesList.add(matchEntriesBuilder.build());
        }

        if (match.getMetadata() != null) {
            boolean hasmask = false;
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setOxmMatchField(Metadata.class);
            addMetadataAugmentation(matchEntriesBuilder, match.getMetadata().getMetadata());
            if (match.getMetadata().getMetadataMask() != null) {
                hasmask = true;
                addMaskAugmentation(matchEntriesBuilder, match.getMetadata().getMetadataMask());
            }
            matchEntriesBuilder.setHasMask(hasmask);
            matchEntriesList.add(matchEntriesBuilder.build());
        }

        if (match.getEthernetMatch() != null) {
            matchEntriesBuilder = new MatchEntriesBuilder();
            EthernetMatch ethernetMatch = match.getEthernetMatch();
            EthernetDestination ethernetDestination = ethernetMatch.getEthernetDestination();
            if (ethernetDestination != null) {
                boolean hasmask = false;
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setOxmMatchField(EthDst.class);
                addMacAddressAugmentation(matchEntriesBuilder, ethernetDestination.getAddress());
                if (ethernetDestination.getMask() != null) {
                    hasmask = true;
                    addMaskAugmentation(matchEntriesBuilder, ethernetDestination.getMask());
                }
                matchEntriesBuilder.setHasMask(hasmask);
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            EthernetSource ethernetSource = ethernetMatch.getEthernetSource();
            if (ethernetSource != null) {
                boolean hasmask = false;
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setOxmMatchField(EthSrc.class);
                addMacAddressAugmentation(matchEntriesBuilder, ethernetSource.getAddress());
                if (ethernetSource.getMask() != null) {
                    hasmask = true;
                    addMaskAugmentation(matchEntriesBuilder, ethernetSource.getMask());
                }
                matchEntriesBuilder.setHasMask(hasmask);
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (ethernetMatch.getEthernetType() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(EthType.class);
                EthTypeMatchEntryBuilder ethertypeBuilder = new EthTypeMatchEntryBuilder();
                ethertypeBuilder.setEthType(new EtherType(ethernetMatch.getEthernetType().getType().getValue()
                        .intValue()));
                matchEntriesBuilder.addAugmentation(EthTypeMatchEntry.class, ethertypeBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }
        }

        VlanMatch vlanMatch = match.getVlanMatch();
        if (vlanMatch != null) {
            if (vlanMatch.getVlanId() != null) {
                // verify
                boolean hasmask = false;
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setOxmMatchField(VlanVid.class);
                VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
                Integer vidEntryValue = vlanMatch.getVlanId().getVlanId().getValue();
                vlanVidBuilder.setCfiBit(vidEntryValue != 0);
                vlanVidBuilder.setVlanVid(vidEntryValue);
                matchEntriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
                if (vlanMatch.getVlanId().getMask() != null) {
                    hasmask = true;
                    addMaskAugmentation(matchEntriesBuilder, vlanMatch.getVlanId().getMask());
                }
                matchEntriesBuilder.setHasMask(hasmask);
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (vlanMatch.getVlanPcp() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(VlanPcp.class);
                VlanPcpMatchEntryBuilder vlanPcpBuilder = new VlanPcpMatchEntryBuilder();
                vlanPcpBuilder.setVlanPcp(vlanMatch.getVlanPcp().getValue());
                matchEntriesBuilder.addAugmentation(VlanPcpMatchEntry.class, vlanPcpBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }
        }

        IpMatch ipMatch = match.getIpMatch();
        if (ipMatch != null) {
            if (ipMatch.getIpDscp() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(IpDscp.class);
                DscpMatchEntryBuilder dscpBuilder = new DscpMatchEntryBuilder();
                dscpBuilder.setDscp(ipMatch.getIpDscp());
                matchEntriesBuilder.addAugmentation(DscpMatchEntry.class, dscpBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (ipMatch.getIpEcn() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(IpDscp.class);
                EcnMatchEntryBuilder ecnBuilder = new EcnMatchEntryBuilder();
                ecnBuilder.setEcn(ipMatch.getIpEcn());
                matchEntriesBuilder.addAugmentation(EcnMatchEntry.class, ecnBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (ipMatch.getIpProto() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(IpProto.class);
                ProtocolNumberMatchEntryBuilder protoNumberBuilder = new ProtocolNumberMatchEntryBuilder();
                protoNumberBuilder.setProtocolNumber((short) ipMatch.getIpProto().getIntValue());
                matchEntriesBuilder.addAugmentation(ProtocolNumberMatchEntry.class, protoNumberBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

        }

        Layer4Match layer4Match = match.getLayer4Match();
        if (layer4Match != null) {
            if (layer4Match instanceof TcpMatch) {
                TcpMatch tcpMatch = (TcpMatch) layer4Match;
                if (tcpMatch.getTcpSourcePort() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(TcpSrc.class);
                    addPortAugmentation(matchEntriesBuilder, tcpMatch.getTcpSourcePort());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (tcpMatch.getTcpDestinationPort() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(TcpDst.class);
                    addPortAugmentation(matchEntriesBuilder, tcpMatch.getTcpDestinationPort());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }

            else if (layer4Match instanceof UdpMatch) {
                UdpMatch udpMatch = (UdpMatch) layer4Match;
                if (udpMatch.getUdpSourcePort() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(UdpSrc.class);
                    addPortAugmentation(matchEntriesBuilder, udpMatch.getUdpSourcePort());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (udpMatch.getUdpDestinationPort() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(UdpDst.class);
                    addPortAugmentation(matchEntriesBuilder, udpMatch.getUdpDestinationPort());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }

            else if (layer4Match instanceof SctpMatch) {
                SctpMatch sctpMatch = (SctpMatch) layer4Match;
                if (sctpMatch.getSctpSourcePort() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(SctpSrc.class);
                    addPortAugmentation(matchEntriesBuilder, sctpMatch.getSctpSourcePort());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (sctpMatch.getSctpDestinationPort() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(SctpDst.class);
                    addPortAugmentation(matchEntriesBuilder, sctpMatch.getSctpDestinationPort());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }
        }

        Icmpv4Match icmpv4Match = match.getIcmpv4Match();
        if (icmpv4Match != null) {
            if (icmpv4Match.getIcmpv4Type() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(Icmpv4Type.class);
                Icmpv4TypeMatchEntryBuilder icmpv4TypeBuilder = new Icmpv4TypeMatchEntryBuilder();
                icmpv4TypeBuilder.setIcmpv4Type(icmpv4Match.getIcmpv4Type());
                matchEntriesBuilder.addAugmentation(Icmpv4TypeMatchEntry.class, icmpv4TypeBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (icmpv4Match.getIcmpv4Code() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(Icmpv4Code.class);
                Icmpv4CodeMatchEntryBuilder icmpv4CodeBuilder = new Icmpv4CodeMatchEntryBuilder();
                icmpv4CodeBuilder.setIcmpv4Code(icmpv4Match.getIcmpv4Code());
                matchEntriesBuilder.addAugmentation(Icmpv4CodeMatchEntry.class, icmpv4CodeBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }
        }

        Icmpv6Match icmpv6Match = match.getIcmpv6Match();
        if (icmpv6Match != null) {
            if (icmpv6Match.getIcmpv6Type() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(Icmpv6Type.class);
                Icmpv6TypeMatchEntryBuilder icmpv6TypeBuilder = new Icmpv6TypeMatchEntryBuilder();
                icmpv6TypeBuilder.setIcmpv6Type(icmpv6Match.getIcmpv6Type());
                matchEntriesBuilder.addAugmentation(Icmpv6TypeMatchEntry.class, icmpv6TypeBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (icmpv6Match.getIcmpv6Code() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(Icmpv6Code.class);
                Icmpv6CodeMatchEntryBuilder icmpv6CodeBuilder = new Icmpv6CodeMatchEntryBuilder();
                icmpv6CodeBuilder.setIcmpv6Code(icmpv6Match.getIcmpv6Code());
                matchEntriesBuilder.addAugmentation(Icmpv6CodeMatchEntry.class, icmpv6CodeBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }
        }

        Layer3Match layer3Match = match.getLayer3Match();
        if (layer3Match != null) {
            matchEntriesBuilder = new MatchEntriesBuilder();
            if (layer3Match instanceof Ipv4Match) {
                Ipv4Match ipv4Match = (Ipv4Match) layer3Match;
                if (ipv4Match.getIpv4Source() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(Ipv4Src.class);
                    boolean hasMask = addIpv4PrefixAugmentation(matchEntriesBuilder, ipv4Match.getIpv4Source());
                    matchEntriesBuilder.setHasMask(hasMask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
                if (ipv4Match.getIpv4Destination() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(Ipv4Dst.class);
                    boolean hasMask = addIpv4PrefixAugmentation(matchEntriesBuilder, ipv4Match.getIpv4Destination());
                    matchEntriesBuilder.setHasMask(hasMask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }

            else if (layer3Match instanceof ArpMatch) {
                ArpMatch arpMatch = (ArpMatch) layer3Match;
                if (arpMatch.getArpOp() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(ArpOp.class);
                    OpCodeMatchEntryBuilder opcodeBuilder = new OpCodeMatchEntryBuilder();
                    opcodeBuilder.setOpCode(arpMatch.getArpOp());
                    matchEntriesBuilder.addAugmentation(OpCodeMatchEntry.class, opcodeBuilder.build());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (arpMatch.getArpSourceTransportAddress() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(ArpSpa.class);
                    boolean hasMask = addIpv4PrefixAugmentation(matchEntriesBuilder, arpMatch.getArpSourceTransportAddress());
                    matchEntriesBuilder.setHasMask(hasMask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (arpMatch.getArpTargetTransportAddress() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(ArpTpa.class);
                    boolean hasMask = addIpv4PrefixAugmentation(matchEntriesBuilder, arpMatch.getArpTargetTransportAddress());
                    matchEntriesBuilder.setHasMask(hasMask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                ArpSourceHardwareAddress arpSourceHardwareAddress = arpMatch.getArpSourceHardwareAddress();
                if (arpSourceHardwareAddress != null) {
                    boolean hasmask = false;
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(ArpSha.class);
                    addMacAddressAugmentation(matchEntriesBuilder, arpSourceHardwareAddress.getAddress());
                    if (arpSourceHardwareAddress.getMask() != null) {
                        hasmask = true;
                        addMaskAugmentation(matchEntriesBuilder, arpSourceHardwareAddress.getMask());
                    }
                    matchEntriesBuilder.setHasMask(hasmask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                ArpTargetHardwareAddress arpTargetHardwareAddress = arpMatch.getArpTargetHardwareAddress();
                if (arpTargetHardwareAddress != null) {
                    boolean hasmask = false;
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(ArpTha.class);
                    addMacAddressAugmentation(matchEntriesBuilder, arpTargetHardwareAddress.getAddress());
                    if (arpTargetHardwareAddress.getMask() != null) {
                        hasmask = true;
                        addMaskAugmentation(matchEntriesBuilder, arpTargetHardwareAddress.getMask());
                    }
                    matchEntriesBuilder.setHasMask(hasmask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }

            else if (layer3Match instanceof Ipv6Match) {
                Ipv6Match ipv6Match = (Ipv6Match) layer3Match;
                if (ipv6Match.getIpv6Source() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Src.class);
                    boolean hasmask = addIpv6PrefixAugmentation(matchEntriesBuilder, ipv6Match.getIpv6Source());
                    matchEntriesBuilder.setHasMask(hasmask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6Destination() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Dst.class);
                    boolean hasmask = addIpv6PrefixAugmentation(matchEntriesBuilder, ipv6Match.getIpv6Destination());
                    matchEntriesBuilder.setHasMask(hasmask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6Label() != null) {
                    boolean hasmask = false;
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Flabel.class);
                    Ipv6FlabelMatchEntryBuilder ipv6FlabelBuilder = new Ipv6FlabelMatchEntryBuilder();
                    ipv6FlabelBuilder.setIpv6Flabel(ipv6Match.getIpv6Label().getIpv6Flabel());
                    matchEntriesBuilder.addAugmentation(Ipv6FlabelMatchEntry.class, ipv6FlabelBuilder.build());
                    if (ipv6Match.getIpv6Label().getFlabelMask() != null) {
                        hasmask = true;
                        addMaskAugmentation(matchEntriesBuilder, ipv6Match.getIpv6Label().getFlabelMask());
                    }
                    matchEntriesBuilder.setHasMask(hasmask);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6NdTarget() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv6NdTarget.class);
                    addIpv6AddressAugmentation(matchEntriesBuilder, ipv6Match.getIpv6NdTarget());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6NdSll() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv6NdSll.class);
                    addMacAddressAugmentation(matchEntriesBuilder, ipv6Match.getIpv6NdSll());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6NdTll() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv6NdTll.class);
                    addMacAddressAugmentation(matchEntriesBuilder, ipv6Match.getIpv6NdTll());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6Exthdr() != null) {
                    // TODO: verify
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
                    PseudoFieldMatchEntryBuilder pseudoBuilder = new PseudoFieldMatchEntryBuilder();
                    Integer bitmap = ipv6Match.getIpv6Exthdr();
                    final Boolean NONEXT = ((bitmap) & (1 << 0)) != 0;
                    final Boolean ESP = ((bitmap) & (1 << 1)) != 0;
                    final Boolean AUTH = ((bitmap) & (1 << 2)) != 0;
                    final Boolean DEST = ((bitmap) & (1 << 3)) != 0;
                    final Boolean FRAG = ((bitmap) & (1 << 4)) != 0;
                    final Boolean ROUTER = ((bitmap) & (1 << 5)) != 0;
                    final Boolean HOP = ((bitmap) & (1 << 6)) != 0;
                    final Boolean UNREP = ((bitmap) & (1 << 7)) != 0;
                    final Boolean UNSEQ = ((bitmap) & (1 << 8)) != 0;
                    pseudoBuilder.setPseudoField(new PseudoField(AUTH, DEST, ESP, FRAG, HOP, NONEXT, ROUTER, UNREP,
                            UNSEQ));
                    matchEntriesBuilder.addAugmentation(PseudoFieldMatchEntry.class, pseudoBuilder.build());
                    addMaskAugmentation(matchEntriesBuilder, ByteBuffer.allocate(2).putInt(bitmap).array());
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }
        }

        ProtocolMatchFields protocolMatchFields = match.getProtocolMatchFields();
        if (protocolMatchFields != null) {
            if (protocolMatchFields.getMplsLabel() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(MplsLabel.class);
                MplsLabelMatchEntryBuilder mplsLabelBuilder = new MplsLabelMatchEntryBuilder();
                mplsLabelBuilder.setMplsLabel(protocolMatchFields.getMplsLabel());
                matchEntriesBuilder.addAugmentation(MplsLabelMatchEntry.class, mplsLabelBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (protocolMatchFields.getMplsBos() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(MplsBos.class);
                BosMatchEntryBuilder bosBuilder = new BosMatchEntryBuilder();
                if (protocolMatchFields.getMplsBos() != 0) {
                    bosBuilder.setBos(true);
                } else {
                    bosBuilder.setBos(false);
                }
                matchEntriesBuilder.addAugmentation(BosMatchEntry.class, bosBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (protocolMatchFields.getMplsTc() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(MplsTc.class);
                TcMatchEntryBuilder tcBuilder = new TcMatchEntryBuilder();
                tcBuilder.setTc(protocolMatchFields.getMplsTc());
                matchEntriesBuilder.addAugmentation(TcMatchEntry.class, tcBuilder.build());
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (protocolMatchFields.getPbb() != null) {
                boolean hasmask = false;
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setOxmMatchField(PbbIsid.class);
                IsidMatchEntryBuilder isidBuilder = new IsidMatchEntryBuilder();
                isidBuilder.setIsid(protocolMatchFields.getPbb().getPbbIsid());
                matchEntriesBuilder.addAugmentation(IsidMatchEntry.class, isidBuilder.build());
                if (protocolMatchFields.getPbb().getPbbMask() != null) {
                    hasmask = true;
                    addMaskAugmentation(matchEntriesBuilder, protocolMatchFields.getPbb().getPbbMask());
                }
                matchEntriesBuilder.setHasMask(hasmask);
                matchEntriesList.add(matchEntriesBuilder.build());
            }
        }

        if (match.getTunnel() != null) {
            boolean hasmask = false;
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setOxmMatchField(TunnelId.class);
            addMetadataAugmentation(matchEntriesBuilder, match.getTunnel().getTunnelId());
            if (match.getTunnel().getTunnelMask() != null) {
                hasmask = true;
                addMaskAugmentation(matchEntriesBuilder, match.getTunnel().getTunnelMask());
            }
            matchEntriesBuilder.setHasMask(false);
            matchEntriesList.add(matchEntriesBuilder.build());
        }

        return matchEntriesList;
    }

    private static List<Instructions> toInstructions(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions instructions,
            short version) {
        List<Instructions> instructionsList = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : instructions
                .getInstruction()) {
            InstructionsBuilder instructionBuilder = new InstructionsBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();
            if (curInstruction instanceof GoToTableCase) {
                GoToTableCase goToTable = (GoToTableCase) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class);
                TableIdInstructionBuilder tableBuilder = new TableIdInstructionBuilder();
                tableBuilder.setTableId(goToTable.getGoToTable().getTableId());
                instructionBuilder.addAugmentation(TableIdInstruction.class, tableBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof WriteMetadataCase) {
                WriteMetadataCase writeMetadata = (WriteMetadataCase) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class);
                MetadataInstructionBuilder metadataBuilder = new MetadataInstructionBuilder();
                metadataBuilder.setMetadata(writeMetadata.getWriteMetadata().getMetadata().toByteArray());
                metadataBuilder.setMetadataMask(writeMetadata.getWriteMetadata().getMetadataMask().toByteArray());
                instructionBuilder.addAugmentation(MetadataInstruction.class, metadataBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof WriteActionsCase) {
                WriteActionsCase writeActions = (WriteActionsCase) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(writeActions.getWriteActions().getAction(),
                        version));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof ApplyActionsCase) {
                ApplyActionsCase applyActions = (ApplyActionsCase) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(applyActions.getApplyActions().getAction(),
                        version));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof ClearActionsCase) {
                ClearActionsCase clearActions = (ClearActionsCase) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(clearActions.getClearActions().getAction(),
                        version));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof MeterCase) {
                MeterCase meter = (MeterCase) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class);
                MeterIdInstructionBuilder meterBuilder = new MeterIdInstructionBuilder();
                Long meterId = Long.parseLong(meter.getMeter().getMeter());
                meterBuilder.setMeterId(meterId);
                instructionBuilder.addAugmentation(MeterIdInstruction.class, meterBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }
        }
        return instructionsList;
    }

    private static void addMaskAugmentation(MatchEntriesBuilder builder, byte[] mask) {
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(mask);
        builder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        builder.setHasMask(true);
    }

    private static void addIpv6AddressAugmentation(MatchEntriesBuilder builder, Ipv6Address address) {
        Ipv6AddressMatchEntryBuilder ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(address);
        builder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
    }

    private static boolean addIpv6PrefixAugmentation(MatchEntriesBuilder builder, Ipv6Prefix address) {
        boolean hasMask = false;
        String[] addressParts = address.getValue().split(PREFIX_SEPARATOR);
        Integer prefix = null;
        if (addressParts.length == 2) {
            prefix = Integer.parseInt(addressParts[1]);
        }

        Ipv6Address ipv6Address = new Ipv6Address(addressParts[0]);
        Ipv6AddressMatchEntryBuilder ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(ipv6Address);
        builder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
        if (prefix != null) {
            hasMask = true;
            addMaskAugmentation(builder, convertIpv6PrefixToByteArray(prefix));
        }
        return hasMask;
    }

    private static byte[] convertIpv6PrefixToByteArray(int prefix) {
        // TODO: Temporary fix. Has performance impacts.
        byte[] mask = new byte[16];
        int oneCount = prefix;
        for (int count = 0; count < 16; count++) {
            int byteBits = 0;
            if (oneCount >= 8) {
                byteBits = 8;
                oneCount = oneCount - 8;
            } else {
                byteBits = oneCount;
                oneCount = 0;
            }

            mask[count] = (byte) (256 - Math.pow(2, 8 - byteBits));
        }
        return mask;
    }

    private static void addMetadataAugmentation(MatchEntriesBuilder builder, BigInteger metadata) {
        MetadataMatchEntryBuilder metadataMatchEntry = new MetadataMatchEntryBuilder();
        metadataMatchEntry.setMetadata(metadata.toByteArray());
        builder.addAugmentation(MetadataMatchEntry.class, metadataMatchEntry.build());
    }

    /**
     * @return true if Ipv4Prefix contains prefix (and it is used in mask), false otherwise
     */
    private static boolean addIpv4PrefixAugmentation(MatchEntriesBuilder builder, Ipv4Prefix address) {
        boolean hasMask = false;
        String[] addressParts = address.getValue().split(PREFIX_SEPARATOR);
        Integer prefix = null;
        if (addressParts.length < 2) {
            prefix = 0;
        } else {
            prefix = Integer.parseInt(addressParts[1]);
        }

        Ipv4Address ipv4Address = new Ipv4Address(addressParts[0]);
        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(ipv4Address);
        builder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        if (prefix != 0) {
            int mask = 0xffffffff << (32 - prefix);
            byte[] maskBytes = new byte[] { (byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8),
                    (byte) mask };
            addMaskAugmentation(builder, maskBytes);
            hasMask = true;
        }
        return hasMask;
    }

    private static void addMacAddressAugmentation(MatchEntriesBuilder builder, MacAddress address) {
        MacAddressMatchEntryBuilder macAddress = new MacAddressMatchEntryBuilder();
        macAddress.setMacAddress(address);
        builder.addAugmentation(MacAddressMatchEntry.class, macAddress.build());
    }

    private static void addPortAugmentation(MatchEntriesBuilder builder,
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber port) {
        PortMatchEntryBuilder portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(port);
        builder.addAugmentation(PortMatchEntry.class, portBuilder.build());
    }

    /**
     * Method converts OF SetField Match to SAL SetFiled matches
     * @param action
     * @return
     */
    public static SetFieldCase ofToSALSetField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.Action action) {
        logger.info("OF SetField match to SAL SetField match converstion begins");
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder setField = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder();
/*        OxmFieldsAction oxmFields = action.getAugmentation(OxmFieldsAction.class);

        List<MatchEntries> matchEntries = oxmFields.getMatchEntries();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field.MatchBuilder match =new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field.MatchBuilder();

        EthernetMatchBuilder ethernetMatchBuilder = null;
        VlanMatchBuilder vlanMatchBuilder = null;
        IpMatchBuilder ipMatchBuilder = null;
        TcpMatchBuilder tcpMatchBuilder = null;
        UdpMatchBuilder udpMatchBuilder = null;
        SctpMatchBuilder sctpMatchBuilder = null;
        Icmpv4MatchBuilder icmpv4MatchBuilder = null;
        Icmpv6MatchBuilder icmpv6MatchBuilder = null;
        Ipv4MatchBuilder ipv4MatchBuilder = null;
        ArpMatchBuilder arpMatchBuilder = null;
        Ipv6MatchBuilder ipv6MatchBuilder = null;
        ProtocolMatchFieldsBuilder protocolMatchFieldsBuilder = null;

        for(MatchEntries matchEntry : matchEntries){
            if(matchEntry instanceof InPort){
                PortNumberMatchEntry inPort = matchEntry.getAugmentation(PortNumberMatchEntry.class);
                match.setInPort(inPort.getPortNumber().getValue());
            }else if (matchEntry instanceof InPhyPort){
                PortNumberMatchEntry phyPort = matchEntry.getAugmentation(PortNumberMatchEntry.class);
                match.setInPhyPort(phyPort.getPortNumber().getValue());
            }else if (matchEntry instanceof Metadata){
                MetadataMatchEntry metadataMatch = matchEntry.getAugmentation(MetadataMatchEntry.class);
                MetadataBuilder metadataBuilder = new MetadataBuilder();
                metadataBuilder.setMetadata(new BigInteger(metadataMatch.getMetadata()));
                MaskMatchEntry maskMatch = matchEntry.getAugmentation(MaskMatchEntry.class);
                if (maskMatch != null){
                    metadataBuilder.setMetadataMask(maskMatch.getMask());
                }
                match.setMetadata(metadataBuilder.build());
            }else if (matchEntry instanceof EthDst){

                if(ethernetMatchBuilder == null)
                    ethernetMatchBuilder = new EthernetMatchBuilder();

                MacAddressMatchEntry macAddressMatch = matchEntry.getAugmentation(MacAddressMatchEntry.class);
                MaskMatchEntry maskMatch = matchEntry.getAugmentation(MaskMatchEntry.class);
                EthernetDestinationBuilder ethernetDestination =  new EthernetDestinationBuilder();
                ethernetDestination.setAddress(macAddressMatch.getMacAddress());
                if(maskMatch != null){
                    ethernetDestination.setMask(maskMatch.getMask());
                }
                ethernetMatchBuilder.setEthernetDestination(ethernetDestination.build());
            }else if (matchEntry instanceof EthSrc){
                if(ethernetMatchBuilder == null)
                    ethernetMatchBuilder = new EthernetMatchBuilder();

                MacAddressMatchEntry macAddressMatch = matchEntry.getAugmentation(MacAddressMatchEntry.class);
                MaskMatchEntry maskMatch = matchEntry.getAugmentation(MaskMatchEntry.class);
                EthernetSourceBuilder ethernetSource =  new EthernetSourceBuilder();
                ethernetSource.setAddress(macAddressMatch.getMacAddress());
                if(maskMatch != null){
                    ethernetSource.setMask(maskMatch.getMask());
                }
                ethernetMatchBuilder.setEthernetSource(ethernetSource.build());
            }else if (matchEntry instanceof EthType){
                if(ethernetMatchBuilder == null)
                    ethernetMatchBuilder = new EthernetMatchBuilder();

                EthTypeMatchEntry etherTypeMatch = matchEntry.getAugmentation(EthTypeMatchEntry.class);
                EthernetTypeBuilder ethernetType=  new EthernetTypeBuilder();
                org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType etherType = new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType((long)etherTypeMatch.getEthType().getValue());
                ethernetType.setType(etherType);
                ethernetMatchBuilder.setEthernetType(ethernetType.build());
            }else if (matchEntry instanceof VlanVid){
                if(vlanMatchBuilder == null)
                    vlanMatchBuilder = new VlanMatchBuilder();

                VlanVidMatchEntry vlanVidMatch = matchEntry.getAugmentation(VlanVidMatchEntry.class);
                MaskMatchEntry maskMatch = matchEntry.getAugmentation(MaskMatchEntry.class);

                VlanIdBuilder vlanIdBuilder = new  VlanIdBuilder();
                vlanIdBuilder.setVlanId(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(vlanVidMatch.getVlanVid()));
                if(maskMatch != null){
                    vlanIdBuilder.setMask(maskMatch.getMask());
                }
                vlanMatchBuilder.setVlanId(vlanIdBuilder.build());

            }else if (matchEntry instanceof VlanPcp){
                if(vlanMatchBuilder == null)
                    vlanMatchBuilder = new VlanMatchBuilder();

                VlanPcpMatchEntry vlanPcpMatch = matchEntry.getAugmentation(VlanPcpMatchEntry.class);
                vlanMatchBuilder.setVlanPcp(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(vlanPcpMatch.getVlanPcp()));
            }else if (matchEntry instanceof IpDscp){
                if(ipMatchBuilder == null)
                    ipMatchBuilder = new IpMatchBuilder();

                DscpMatchEntry dscpMatchEntry = matchEntry.getAugmentation(DscpMatchEntry.class);
                ipMatchBuilder.setIpDscp(dscpMatchEntry.getDscp());

            }else if (matchEntry instanceof IpEcn){
                if(ipMatchBuilder == null)
                    ipMatchBuilder = new IpMatchBuilder();

                EcnMatchEntry ecnMatchEntry = matchEntry.getAugmentation(EcnMatchEntry.class);
                ipMatchBuilder.setIpEcn(ecnMatchEntry.getEcn());

            }else if (matchEntry instanceof IpProto){
                if(ipMatchBuilder == null)
                    ipMatchBuilder = new IpMatchBuilder();

                ProtocolNumberMatchEntry protocolNumberMatch = matchEntry.getAugmentation(ProtocolNumberMatchEntry.class);
                ipMatchBuilder.setIpProtocol(protocolNumberMatch.getProtocolNumber());
            }else if (matchEntry instanceof TcpSrc){
                if(tcpMatchBuilder == null)
                    tcpMatchBuilder = new TcpMatchBuilder();

                PortMatchEntry portMatchEntry = matchEntry.getAugmentation(PortMatchEntry.class);
                tcpMatchBuilder.setTcpSourcePort(portMatchEntry.getPort());

            }else if (matchEntry instanceof TcpDst){
                if(tcpMatchBuilder == null)
                    tcpMatchBuilder = new TcpMatchBuilder();

                PortMatchEntry portMatchEntry = matchEntry.getAugmentation(PortMatchEntry.class);
                tcpMatchBuilder.setTcpDestinationPort(portMatchEntry.getPort());

            }else if (matchEntry instanceof UdpSrc){
                if(udpMatchBuilder == null)
                    udpMatchBuilder = new UdpMatchBuilder();

                PortMatchEntry portMatchEntry = matchEntry.getAugmentation(PortMatchEntry.class);
                udpMatchBuilder.setUdpSourcePort(portMatchEntry.getPort());


            }else if (matchEntry instanceof UdpDst){
                if(udpMatchBuilder == null)
                    udpMatchBuilder = new UdpMatchBuilder();

                PortMatchEntry portMatchEntry = matchEntry.getAugmentation(PortMatchEntry.class);
                udpMatchBuilder.setUdpDestinationPort(portMatchEntry.getPort());
            }else if (matchEntry instanceof SctpSrc){
                if(sctpMatchBuilder == null)
                    sctpMatchBuilder = new SctpMatchBuilder();

                PortMatchEntry portMatchEntry = matchEntry.getAugmentation(PortMatchEntry.class);
                sctpMatchBuilder.setSctpSourcePort(portMatchEntry.getPort());

            }else if (matchEntry instanceof SctpDst){
                if(sctpMatchBuilder == null)
                    sctpMatchBuilder = new SctpMatchBuilder();

                PortMatchEntry portMatchEntry = matchEntry.getAugmentation(PortMatchEntry.class);
                sctpMatchBuilder.setSctpDestinationPort(portMatchEntry.getPort());
            }else if (matchEntry instanceof Icmpv4Type){
                if(icmpv4MatchBuilder == null)
                    icmpv4MatchBuilder = new Icmpv4MatchBuilder();

                Icmpv4TypeMatchEntry icmpv4TypeMatchEntry = matchEntry.getAugmentation(Icmpv4TypeMatchEntry.class);
                icmpv4MatchBuilder.setIcmpv4Type(icmpv4TypeMatchEntry.getIcmpv4Type());

            }else if (matchEntry instanceof Icmpv4Code){
                if(icmpv4MatchBuilder == null)
                    icmpv4MatchBuilder = new Icmpv4MatchBuilder();

                Icmpv4CodeMatchEntry icmpv4CodeMatchEntry = matchEntry.getAugmentation(Icmpv4CodeMatchEntry.class);
                icmpv4MatchBuilder.setIcmpv4Code(icmpv4CodeMatchEntry.getIcmpv4Code());

            }else if (matchEntry instanceof Icmpv6Type){
                if(icmpv6MatchBuilder == null)
                    icmpv6MatchBuilder = new Icmpv6MatchBuilder();

                Icmpv6TypeMatchEntry icmpv6TypeMatchEntry = matchEntry.getAugmentation(Icmpv6TypeMatchEntry.class);
                icmpv6MatchBuilder.setIcmpv6Type(icmpv6TypeMatchEntry.getIcmpv6Type());
            }else if (matchEntry instanceof Icmpv6Code){
                if(icmpv6MatchBuilder == null)
                    icmpv6MatchBuilder = new Icmpv6MatchBuilder();

                Icmpv6CodeMatchEntry icmpv6CodeMatchEntry = matchEntry.getAugmentation(Icmpv6CodeMatchEntry.class);
                icmpv6MatchBuilder.setIcmpv6Code(icmpv6CodeMatchEntry.getIcmpv6Code());
            }else if (matchEntry instanceof Ipv4Src){
                if(ipv4MatchBuilder == null)
                    ipv4MatchBuilder = new Ipv4MatchBuilder();

                Ipv4AddressMatchEntry ipv4AddressMatchEntry = matchEntry.getAugmentation(Ipv4AddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                ipv4MatchBuilder.setIpv4Source(
                        new Ipv4Prefix(ipv4AddressMatchEntry.getIpv4Address().getValue()
                                +"/"+new String(maskMatchEntry.getMask())));

            }else if (matchEntry instanceof Ipv4Dst){
                if(ipv4MatchBuilder == null)
                    ipv4MatchBuilder = new Ipv4MatchBuilder();

                Ipv4AddressMatchEntry ipv4AddressMatchEntry = matchEntry.getAugmentation(Ipv4AddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                ipv4MatchBuilder.setIpv4Destination(
                        new Ipv4Prefix(ipv4AddressMatchEntry.getIpv4Address().getValue()
                                +"/"+new String(maskMatchEntry.getMask())));
            }else if (matchEntry instanceof ArpOp){
                if(arpMatchBuilder == null)
                    arpMatchBuilder = new ArpMatchBuilder();

                OpCodeMatchEntry opCodeMatchEntry = matchEntry.getAugmentation(OpCodeMatchEntry.class);
                arpMatchBuilder.setArpOp(opCodeMatchEntry.getOpCode());

            }else if (matchEntry instanceof ArpSpa){
                if(arpMatchBuilder == null)
                    arpMatchBuilder = new ArpMatchBuilder();

                Ipv4AddressMatchEntry ipv4AddressMatchEntry = matchEntry.getAugmentation(Ipv4AddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                arpMatchBuilder.setArpSourceTransportAddress(
                        new Ipv4Prefix(ipv4AddressMatchEntry.getIpv4Address().getValue()
                                +"/"+new String(maskMatchEntry.getMask())));

            }else if (matchEntry instanceof ArpTpa){
                if(arpMatchBuilder == null)
                    arpMatchBuilder = new ArpMatchBuilder();

                Ipv4AddressMatchEntry ipv4AddressMatchEntry = matchEntry.getAugmentation(Ipv4AddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                arpMatchBuilder.setArpTargetTransportAddress(
                        new Ipv4Prefix(ipv4AddressMatchEntry.getIpv4Address().getValue()
                                +"/"+new String(maskMatchEntry.getMask())));

            }else if (matchEntry instanceof ArpSha){
                if(arpMatchBuilder == null)
                    arpMatchBuilder = new ArpMatchBuilder();

                MacAddressMatchEntry macAddressMatchEntry = matchEntry.getAugmentation(MacAddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                ArpSourceHardwareAddressBuilder arpSourceHardwareAddressBuilder = new ArpSourceHardwareAddressBuilder();
                arpSourceHardwareAddressBuilder.setAddress(macAddressMatchEntry.getMacAddress());
                arpSourceHardwareAddressBuilder.setMask(maskMatchEntry.getMask());
                arpMatchBuilder.setArpSourceHardwareAddress(arpSourceHardwareAddressBuilder.build());

            }else if (matchEntry instanceof ArpTha){
                if(arpMatchBuilder == null)
                    arpMatchBuilder = new ArpMatchBuilder();

                MacAddressMatchEntry macAddressMatchEntry = matchEntry.getAugmentation(MacAddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                ArpTargetHardwareAddressBuilder arpTargetHardwareAddressBuilder = new ArpTargetHardwareAddressBuilder();
                arpTargetHardwareAddressBuilder.setAddress(macAddressMatchEntry.getMacAddress());
                arpTargetHardwareAddressBuilder.setMask(maskMatchEntry.getMask());
                arpMatchBuilder.setArpTargetHardwareAddress(arpTargetHardwareAddressBuilder.build());
            }else if (matchEntry instanceof Ipv6Src){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();

                Ipv6AddressMatchEntry ipv6AddressMatchEntry = matchEntry.getAugmentation(Ipv6AddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                ipv6MatchBuilder.setIpv6Source(new Ipv6Prefix (ipv6AddressMatchEntry.getIpv6Address().getValue()+
                        "/"+new String(maskMatchEntry.getMask())));

            }else if (matchEntry instanceof Ipv6Dst){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();

                Ipv6AddressMatchEntry ipv6AddressMatchEntry = matchEntry.getAugmentation(Ipv6AddressMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                ipv6MatchBuilder.setIpv6Destination(new Ipv6Prefix (ipv6AddressMatchEntry.getIpv6Address().getValue()+
                        "/"+new String(maskMatchEntry.getMask())));

            }else if (matchEntry instanceof Ipv6Flabel){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();

                Ipv6FlabelMatchEntry ipv6FlabelMatchEntry = matchEntry.getAugmentation(Ipv6FlabelMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                Ipv6LabelBuilder ipv6LabelBuilder = new Ipv6LabelBuilder();
                ipv6LabelBuilder.setIpv6Flabel(ipv6FlabelMatchEntry.getIpv6Flabel());
                ipv6LabelBuilder.setFlabelMask(maskMatchEntry.getMask());
                ipv6MatchBuilder.setIpv6Label(ipv6LabelBuilder.build());

            }else if (matchEntry instanceof Ipv6NdTarget){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();
                Ipv6AddressMatchEntry ipv6AddressMatchEntry = matchEntry.getAugmentation(Ipv6AddressMatchEntry.class);
                ipv6MatchBuilder.setIpv6NdTarget(ipv6AddressMatchEntry.getIpv6Address());

            }else if (matchEntry instanceof Ipv6NdSll){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();

                MacAddressMatchEntry macAddressMatchEntry = matchEntry.getAugmentation(MacAddressMatchEntry.class);
                ipv6MatchBuilder.setIpv6NdSll(macAddressMatchEntry.getMacAddress());
            }else if (matchEntry instanceof Ipv6NdTll){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();

                MacAddressMatchEntry macAddressMatchEntry = matchEntry.getAugmentation(MacAddressMatchEntry.class);
                ipv6MatchBuilder.setIpv6NdTll(macAddressMatchEntry.getMacAddress());

            }else if (matchEntry instanceof Ipv6Exthdr){
                if(ipv6MatchBuilder == null)
                    ipv6MatchBuilder = new Ipv6MatchBuilder();

                PseudoFieldMatchEntry pseudoFieldMatchEntry = matchEntry.getAugmentation(PseudoFieldMatchEntry.class);
                PseudoField pseudoField = pseudoFieldMatchEntry.getPseudoField();
                int pseudoFieldInt = 0;
                pseudoFieldInt |= pseudoField.isNonext()?(1 << 0):~(1 << 0);
                pseudoFieldInt |= pseudoField.isEsp()?(1 << 1):~(1 << 1);
                pseudoFieldInt |= pseudoField.isAuth()?(1 << 2):~(1 << 2);
                pseudoFieldInt |= pseudoField.isDest()?(1 << 3):~(1 << 3);
                pseudoFieldInt |= pseudoField.isFrag()?(1 << 4):~(1 << 4);
                pseudoFieldInt |= pseudoField.isRouter()?(1 << 5):~(1 << 5);
                pseudoFieldInt |= pseudoField.isHop()?(1 << 6):~(1 << 6);
                pseudoFieldInt |= pseudoField.isUnrep()?(1 << 7):~(1 << 7);
                pseudoFieldInt |= pseudoField.isUnseq()?(1 << 8):~(1 << 8);

                ipv6MatchBuilder.setIpv6Exthdr(pseudoFieldInt);
            }else if (matchEntry instanceof MplsLabel){
                if(protocolMatchFieldsBuilder == null)
                    protocolMatchFieldsBuilder = new ProtocolMatchFieldsBuilder();

                MplsLabelMatchEntry MplsLabelMatchEntry = matchEntry.getAugmentation(MplsLabelMatchEntry.class);
                protocolMatchFieldsBuilder.setMplsLabel(MplsLabelMatchEntry.getMplsLabel());

            }else if (matchEntry instanceof MplsBos){
                if(protocolMatchFieldsBuilder == null)
                    protocolMatchFieldsBuilder = new ProtocolMatchFieldsBuilder();

                BosMatchEntry bosMatchEntry = matchEntry.getAugmentation(BosMatchEntry.class);
                protocolMatchFieldsBuilder.setMplsBos(bosMatchEntry.isBos()?(short)1:(short)0);

            }else if (matchEntry instanceof MplsTc) {
                if(protocolMatchFieldsBuilder == null)
                    protocolMatchFieldsBuilder = new ProtocolMatchFieldsBuilder();

                TcMatchEntry tcMatchEntry = matchEntry.getAugmentation(TcMatchEntry.class);
                protocolMatchFieldsBuilder.setMplsTc(tcMatchEntry.getTc());

            }else if (matchEntry instanceof PbbIsid){
                if(protocolMatchFieldsBuilder == null)
                    protocolMatchFieldsBuilder = new ProtocolMatchFieldsBuilder();

                IsidMatchEntry isidMatchEntry = matchEntry.getAugmentation(IsidMatchEntry.class);
                PbbBuilder pbbBuilder = new PbbBuilder();
                pbbBuilder.setPbbIsid(isidMatchEntry.getIsid());
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                if(maskMatchEntry != null)
                    pbbBuilder.setPbbMask(maskMatchEntry.getMask());

                protocolMatchFieldsBuilder.setPbb(pbbBuilder.build());
            }else if (matchEntry instanceof TunnelId){
                MetadataMatchEntry metadataMatchEntry = matchEntry.getAugmentation(MetadataMatchEntry.class);
                MaskMatchEntry maskMatchEntry = matchEntry.getAugmentation(MaskMatchEntry.class);
                TunnelBuilder tunnelBuilder = new TunnelBuilder();
                tunnelBuilder.setTunnelId(new BigInteger(metadataMatchEntry.getMetadata()));
                tunnelBuilder.setTunnelMask(maskMatchEntry.getMask());
                match.setTunnel(tunnelBuilder.build());
            }
        }
        if(ethernetMatchBuilder != null){
            match.setEthernetMatch(ethernetMatchBuilder.build());
        }
        if (vlanMatchBuilder != null){
            match.setVlanMatch(vlanMatchBuilder.build());
        }
        if(ipMatchBuilder != null){
            match.setIpMatch(ipMatchBuilder.build());
        }
        if(tcpMatchBuilder != null){
            match.setLayer4Match(tcpMatchBuilder.build());
        }
        if(udpMatchBuilder != null){
            match.setLayer4Match(udpMatchBuilder.build());
        }
        if(sctpMatchBuilder != null){
            match.setLayer4Match(sctpMatchBuilder.build());
        }
        if(icmpv4MatchBuilder != null){
            match.setIcmpv4Match(icmpv4MatchBuilder.build());
        }
        if(icmpv6MatchBuilder != null){
            match.setIcmpv6Match(icmpv6MatchBuilder.build());
        }
        if(ipv4MatchBuilder != null){
            match.setLayer3Match(ipv4MatchBuilder.build());
        }
        if(arpMatchBuilder != null){
            match.setLayer3Match(arpMatchBuilder.build());
        }
        if(ipv6MatchBuilder != null){
            match.setLayer3Match(ipv6MatchBuilder.build());
        }
        if(protocolMatchFieldsBuilder != null){
            match.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
        }
        setField.setMatch(match.build());
  */
        return new SetFieldCaseBuilder().setSetField(setField.build()).build();
    }

}