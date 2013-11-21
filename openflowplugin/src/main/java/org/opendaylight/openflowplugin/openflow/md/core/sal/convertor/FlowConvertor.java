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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadata;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
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

    public static FlowModInput toFlowModInput(Flow flow) {
        FlowModInputBuilder flowMod = new FlowModInputBuilder();

        flowMod.setCookie(flow.getCookie());

        if (flow.getCookieMask() != null) {
            flowMod.setCookieMask(new BigInteger(flow.getCookieMask().toString()));
        }

        if (flow.getTableId() != null) {
            flowMod.setTableId(new TableId(flow.getTableId().longValue()));
        }

        if (flow instanceof AddFlowInput) {
            flowMod.setCommand(FlowModCommand.OFPFCADD);
        } else if (flow instanceof RemoveFlowInput) {
            if (flow.isStrict()) {
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

        flowMod.setIdleTimeout(flow.getIdleTimeout());
        flowMod.setHardTimeout(flow.getHardTimeout());
        flowMod.setPriority(flow.getPriority());
        flowMod.setBufferId(flow.getBufferId());

        if (flow.getOutPort() != null) {
            flowMod.setOutPort(new PortNumber(flow.getOutPort().longValue()));
        }

        flowMod.setOutGroup(flow.getOutGroup());

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags flowModFlags = flow.getFlags();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags ofFlowModFlags = null;
        if (flowModFlags != null) {
            ofFlowModFlags = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags(
                    flowModFlags.isCHECKOVERLAP(), flowModFlags.isNOBYTCOUNTS(), flowModFlags.isNOPKTCOUNTS(),
                    flowModFlags.isRESETCOUNTS(), flowModFlags.isSENDFLOWREM());
        }
        flowMod.setFlags(ofFlowModFlags);

        if (flow.getMatch() != null) {
            MatchBuilder matchBuilder = new MatchBuilder();
            matchBuilder.setMatchEntries(toMatch(flow.getMatch()));
            flowMod.setMatch(matchBuilder.build());
        }

        if (flow.getInstructions() != null) {
            flowMod.setInstructions(toInstructions(flow.getInstructions()));
        }

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
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setHasMask(false);
            matchEntriesBuilder.setOxmMatchField(Metadata.class);
            addMetadataAugmentation(matchEntriesBuilder, match.getMetadata().getMetadata());
            if (match.getMetadata().getMetadataMask() != null) {
                addMaskAugmentation(matchEntriesBuilder, match.getMetadata().getMetadataMask());
            }
            matchEntriesList.add(matchEntriesBuilder.build());
        }

        if (match.getEthernetMatch() != null) {
            EthernetMatch ethernetMatch = match.getEthernetMatch();
            EthernetDestination ethernetDestination = ethernetMatch.getEthernetDestination();
            if (ethernetDestination != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(EthDst.class);
                addMacAddressAugmentation(matchEntriesBuilder, ethernetDestination.getAddress());
                if (ethernetDestination.getMask() != null) {
                    addMaskAugmentation(matchEntriesBuilder, ethernetDestination.getMask());
                }
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            EthernetSource ethernetSource = ethernetMatch.getEthernetSource();
            if (ethernetSource != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(EthSrc.class);
                addMacAddressAugmentation(matchEntriesBuilder, ethernetSource.getAddress());
                if (ethernetSource.getMask() != null) {
                    addMaskAugmentation(matchEntriesBuilder, ethernetSource.getMask());
                }
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
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(true);
                matchEntriesBuilder.setOxmMatchField(VlanVid.class);
                VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
                Integer vidEntryValue = vlanMatch.getVlanId().getVlanId().getValue();
                vlanVidBuilder.setCfiBit(vidEntryValue != 0);
                vlanVidBuilder.setVlanVid(vidEntryValue);
                matchEntriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
                if (vlanMatch.getVlanId().getMask() != null) {
                    addMaskAugmentation(matchEntriesBuilder, vlanMatch.getVlanId().getMask());
                }
                matchEntriesList.add(matchEntriesBuilder.build());
            }

            if (vlanMatch.getVlanPcp() != null) {
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(VlanPcp.class);
                VlanPcpMatchEntryBuilder vlanPcpBuilder = new VlanPcpMatchEntryBuilder();
                vlanPcpBuilder.setVlanPcp(vlanMatch.getVlanPcp().getValue());
                matchEntriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanPcpBuilder.build());
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
            if (layer3Match instanceof Ipv4Match) {
                Ipv4Match ipv4Match = (Ipv4Match) layer3Match;
                if (ipv4Match.getIpv4Source() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv4Src.class);
                    addIpv4PrefixAugmentation(matchEntriesBuilder, ipv4Match.getIpv4Source());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
                if (ipv4Match.getIpv4Destination() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv4Dst.class);
                    addIpv4PrefixAugmentation(matchEntriesBuilder, ipv4Match.getIpv4Destination());
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
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(ArpSpa.class);
                    addIpv4PrefixAugmentation(matchEntriesBuilder, arpMatch.getArpSourceTransportAddress());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (arpMatch.getArpTargetTransportAddress() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(ArpTpa.class);
                    addIpv4PrefixAugmentation(matchEntriesBuilder, arpMatch.getArpTargetTransportAddress());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                ArpSourceHardwareAddress arpSourceHardwareAddress = arpMatch.getArpSourceHardwareAddress();
                if (arpSourceHardwareAddress != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(ArpSha.class);
                    addMacAddressAugmentation(matchEntriesBuilder, arpSourceHardwareAddress.getAddress());
                    if (arpSourceHardwareAddress.getMask() != null) {
                        addMaskAugmentation(matchEntriesBuilder, arpSourceHardwareAddress.getMask());
                    }
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                ArpTargetHardwareAddress arpTargetHardwareAddress = arpMatch.getArpTargetHardwareAddress();
                if (arpTargetHardwareAddress != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(ArpTha.class);
                    addMacAddressAugmentation(matchEntriesBuilder, arpTargetHardwareAddress.getAddress());
                    if (arpTargetHardwareAddress.getMask() != null) {
                        addMaskAugmentation(matchEntriesBuilder, arpTargetHardwareAddress.getMask());
                    }
                    matchEntriesList.add(matchEntriesBuilder.build());
                }
            }

            else if (layer3Match instanceof Ipv6Match) {
                Ipv6Match ipv6Match = (Ipv6Match) layer3Match;
                if (ipv6Match.getIpv6Source() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Src.class);
                    addIpv6PrefixAugmentation(matchEntriesBuilder, ipv6Match.getIpv6Source());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6Destination() != null) {
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Dst.class);
                    addIpv6PrefixAugmentation(matchEntriesBuilder, ipv6Match.getIpv6Destination());
                    matchEntriesList.add(matchEntriesBuilder.build());
                }

                if (ipv6Match.getIpv6Label() != null) {
                    // verify
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
                    matchEntriesBuilder.setOxmMatchField(Ipv6Flabel.class);
                    Ipv6FlabelMatchEntryBuilder ipv6FlabelBuilder = new Ipv6FlabelMatchEntryBuilder();
                    ipv6FlabelBuilder.setIpv6Flabel(ipv6Match.getIpv6Label().getIpv6Flabel());
                    matchEntriesBuilder.addAugmentation(Ipv6FlabelMatchEntry.class, ipv6FlabelBuilder.build());
                    if (ipv6Match.getIpv6Label().getFlabelMask() != null) {
                        addMaskAugmentation(matchEntriesBuilder, ipv6Match.getIpv6Label().getFlabelMask());
                    }
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
                    // verify
                    matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntriesBuilder.setHasMask(false);
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
                matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
                // verify
                matchEntriesBuilder.setHasMask(false);
                matchEntriesBuilder.setOxmMatchField(PbbIsid.class);
                IsidMatchEntryBuilder isidBuilder = new IsidMatchEntryBuilder();
                isidBuilder.setIsid(protocolMatchFields.getPbb().getPbbIsid());
                matchEntriesBuilder.addAugmentation(IsidMatchEntry.class, isidBuilder.build());
                if (protocolMatchFields.getPbb().getPbbMask() != null) {
                    addMaskAugmentation(matchEntriesBuilder, protocolMatchFields.getPbb().getPbbMask());
                }
                matchEntriesList.add(matchEntriesBuilder.build());
            }
        }

        if (match.getTunnel() != null) {
            matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntriesBuilder.setHasMask(false);
            matchEntriesBuilder.setOxmMatchField(TunnelId.class);
            addMetadataAugmentation(matchEntriesBuilder, match.getTunnel().getTunnelId());
            if (match.getTunnel().getTunnelMask() != null) {
                addMaskAugmentation(matchEntriesBuilder, match.getTunnel().getTunnelMask());
            }
        }

        return matchEntriesList;
    }

    private static List<Instructions> toInstructions(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions instructions) {
        List<Instructions> instructionsList = new ArrayList<>();
        InstructionsBuilder instructionBuilder = new InstructionsBuilder();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : instructions
                .getInstruction()) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();
            if (curInstruction instanceof GoToTable) {
                GoToTable goToTable = (GoToTable) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class);
                TableIdInstructionBuilder tableBuilder = new TableIdInstructionBuilder();
                tableBuilder.setTableId(goToTable.getTableId());
                instructionBuilder.addAugmentation(TableIdInstruction.class, tableBuilder.build());
            }

            else if (curInstruction instanceof WriteMetadata) {
                WriteMetadata writeMetadata = (WriteMetadata) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class);
                MetadataInstructionBuilder metadataBuilder = new MetadataInstructionBuilder();
                metadataBuilder.setMetadata(writeMetadata.getMetadata().toByteArray());
                metadataBuilder.setMetadataMask(writeMetadata.getMetadataMask().toByteArray());
                instructionBuilder.addAugmentation(MetadataInstruction.class, metadataBuilder.build());
            }

            else if (curInstruction instanceof WriteActions) {
                WriteActions writeActions = (WriteActions) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(writeActions.getAction()));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
            }

            else if (curInstruction instanceof ApplyActions) {
                ApplyActions applyActions = (ApplyActions) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(applyActions.getAction()));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
            }

            else if (curInstruction instanceof ClearActions) {
                ClearActions clearActions = (ClearActions) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(clearActions.getAction()));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
            }

            else if (curInstruction instanceof Meter) {
                Meter meter = (Meter) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class);
                MeterIdInstructionBuilder meterBuilder = new MeterIdInstructionBuilder();
                Long meterId = Long.parseLong(meter.getMeter());
                meterBuilder.setMeterId(meterId);
                instructionBuilder.addAugmentation(MeterIdInstruction.class, meterBuilder.build());
            }

            instructionsList.add(instructionBuilder.build());
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

    private static void addIpv6PrefixAugmentation(MatchEntriesBuilder builder, Ipv6Prefix address) {
        // TODO: bug
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
            addMaskAugmentation(builder, ByteBuffer.allocate(2).putInt(prefix).array());
        }
    }

    private static void addMetadataAugmentation(MatchEntriesBuilder builder, BigInteger metadata) {
        MetadataMatchEntryBuilder metadataMatchEntry = new MetadataMatchEntryBuilder();
        metadataMatchEntry.setMetadata(metadata.toByteArray());
        builder.addAugmentation(MetadataMatchEntry.class, metadataMatchEntry.build());
    }

    private static void addIpv4PrefixAugmentation(MatchEntriesBuilder builder, Ipv4Prefix address) {
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
            byte[] maskBytes = new byte[4];

            for (int currByte = 3; currByte >= 0; --currByte) {
                maskBytes[currByte] = 0;
                maskBytes[currByte] |= ((mask >>> 8 * (3 - currByte)) & (0xff));
            }
            addMaskAugmentation(builder, maskBytes);
        }
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
}