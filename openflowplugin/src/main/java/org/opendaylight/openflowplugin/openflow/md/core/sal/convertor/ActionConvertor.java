/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.Ordering;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ActionExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionSetNwDstReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionSetNwSrcReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTosActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanPcpActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.StripVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.in._case.CopyTtlInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.copy.ttl.out._case.CopyTtlOutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.mpls.ttl._case.DecMplsTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.dec.nw.ttl._case.DecNwTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.pbb.action._case.PopPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.vlan.action._case.PopVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.tos.action._case.SetNwTosAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.pcp.action._case.SetVlanPcpAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetDlSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.pop.mpls._case.PopMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.mpls._case.PushMplsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.dst._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.dl.src._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.tos._case.SetNwTosActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.ttl._case.SetNwTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.queue._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.dst._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.src._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.pcp._case.SetVlanPcpActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.vlan.vid._case.SetVlanVidActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.dst._case.TcpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.src._case.TcpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.src._case.UdpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author usha@ericsson Action List:This class takes data from SAL layer and
 *         converts into OF Data
 * @author avishnoi@in.ibm.com Added convertor for OF bucket actions to SAL
 *         actions
 */
public final class ActionConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(ActionConvertor.class);
    private static final String UNKNOWN_ACTION_TYPE_VERSION = "Unknown Action Type for the Version";
    private static final Ordering<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> ACTION_ORDERING =
            Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>build());

    private ActionConvertor() {
        // NOOP
    }

    /**
     * Translates SAL actions into OF Library actions
     *
     * @param actions    SAL actions
     * @param version    Openflow protocol version used
     * @param datapathid datapath id
     * @param flow       TODO
     * @return OF Library actions
     */
    public static List<Action> getActions(
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions,
            final short version, final BigInteger datapathid, final Flow flow) {
        List<Action> actionsList = new ArrayList<>();
        Action ofAction;

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> sortedActions =
                ACTION_ORDERING.sortedCopy(actions);

        for (int actionItem = 0; actionItem < sortedActions.size(); actionItem++) {
            ofAction = null;
            ActionBuilder actionBuilder = new ActionBuilder();

            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action = sortedActions.get(
                    actionItem).getAction();


            if (action instanceof OutputActionCase) {
                ofAction = salToOFAction((OutputActionCase) action, actionBuilder, version);
            } else if (action instanceof DropActionCase){
                //noop
            } else if (action instanceof GroupActionCase) {
                ofAction = salToOFGroupAction(action, actionBuilder);
            } else if (action instanceof CopyTtlOutCase) {
                ofAction = salToOFCopyTTLIOut(actionBuilder);
            } else if (action instanceof CopyTtlInCase) {
                ofAction = salToOFCopyTTLIIn(actionBuilder);
            } else if (action instanceof SetMplsTtlActionCase) {
                ofAction = salToOFSetMplsTtl(action, actionBuilder);
            } else if (action instanceof DecMplsTtlCase) {
                ofAction = salToOFDecMplsTtl(actionBuilder);
            } else if (action instanceof PushVlanActionCase) {
                ofAction = salToOFPushVlanAction(action, actionBuilder, version);
            } else if (action instanceof PopVlanActionCase) {
                ofAction = (version == OFConstants.OFP_VERSION_1_0) ?
                        salToOFStripVlan(actionBuilder, version)
                        : salToOFPopVlan(actionBuilder);
            } else if (action instanceof PushMplsActionCase) {
                ofAction = salToOFPushMplsAction(action, actionBuilder);
            } else if (action instanceof PopMplsActionCase) {
                ofAction = salToOFPopMpls(action, actionBuilder);
            } else if (action instanceof SetQueueActionCase) {
                ofAction = salToOFSetQueue(action, actionBuilder);
            } else if (action instanceof SetNwTtlActionCase) {
                ofAction = salToOFSetNwTtl(action, actionBuilder);
            } else if (action instanceof DecNwTtlCase) {
                ofAction = salToOFDecNwTtl(actionBuilder);
            } else if (action instanceof SetFieldCase) {
                ofAction = salToOFSetField(action, actionBuilder, version, datapathid);
            } else if (action instanceof PushPbbActionCase) {
                ofAction = salToOFPushPbbAction(action, actionBuilder);
            } else if (action instanceof PopPbbActionCase) {
                ofAction = salToOFPopPBB(actionBuilder);

                // 1.0 Actions
            } else if (action instanceof SetVlanIdActionCase) {
                ofAction = salToOFSetVlanId(action, actionBuilder, version);
            } else if (action instanceof SetVlanPcpActionCase) {
                ofAction = salToOFSetVlanpcp(action, actionBuilder, version);
            } else if (action instanceof StripVlanActionCase) {
                ofAction = salToOFStripVlan(actionBuilder, version);
            } else if (action instanceof SetDlSrcActionCase) {
                ofAction = salToOFSetDlSrc(action, actionBuilder, version);
            } else if (action instanceof SetDlDstActionCase) {
                ofAction = salToOFSetDlDst(action, actionBuilder, version);
            } else if (action instanceof SetNwSrcActionCase) {
                ofAction = salToOFSetNwSrc(action, actionBuilder, version);
            } else if (action instanceof SetNwDstActionCase) {
                ofAction = salToOFSetNwDst(action, actionBuilder, version);
            } else if (action instanceof SetTpSrcActionCase) {
                ofAction = salToOFSetTpSrc(action, actionBuilder, version, IPProtocols.fromProtocolNum(flow.getMatch().
                        getIpMatch().getIpProtocol()));
            } else if (action instanceof SetTpDstActionCase) {
                ofAction = salToOFSetTpDst(action, actionBuilder, version, IPProtocols.fromProtocolNum(flow.getMatch().
                        getIpMatch().getIpProtocol()));
            } else if (action instanceof SetNwTosActionCase) {
                ofAction = salToOFSetNwTos(action, actionBuilder, version);
            } else if (action instanceof GeneralExtensionGrouping) {
                /**
                 * TODO: EXTENSION PROPOSAL (action, MD-SAL to OFJava)
                 * - we might need sessionContext as converter input
                 *
                 */

                GeneralExtensionGrouping extensionCaseGrouping = (GeneralExtensionGrouping) action;
                Extension extAction = extensionCaseGrouping.getExtension();
                ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(extensionCaseGrouping.getExtensionKey(), version);
                ConvertorToOFJava<Action> convertor =
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                if (convertor != null) {
                    ofAction = convertor.convert(extAction);
                }
            } else {
                // try vendor codecs
                TypeVersionKey<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> key =
                        new TypeVersionKey<>(
                                (Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action>) action.getImplementedInterface(),
                                version);
                ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action> convertor =
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                LOG.trace("OFP Extension action, key:{}, converter:{}", key, convertor);
                if (convertor != null) {
                    ofAction = convertor.convert(action);
                }
            }

            if (ofAction != null) {
                actionsList.add(ofAction);
            }
        }
        return actionsList;
    }

    private static Action salToOFSetField(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version, final BigInteger datapathid) {

        SetFieldCase setFieldCase = (SetFieldCase) action;
        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match =
                setFieldCase.getSetField();

        if (version == OFConstants.OFP_VERSION_1_0) {
            // pushvlan +setField can be called to configure 1.0 switches via MDSAL app
            if (match.getVlanMatch() != null) {
                SetVlanVidActionBuilder vlanidActionBuilder = new SetVlanVidActionBuilder();
                SetVlanVidCaseBuilder setVlanVidCaseBuilder = new SetVlanVidCaseBuilder();
                vlanidActionBuilder.setVlanVid(match.getVlanMatch().getVlanId().getVlanId().getValue());
                setVlanVidCaseBuilder.setSetVlanVidAction(vlanidActionBuilder.build());

                actionBuilder.setActionChoice(setVlanVidCaseBuilder.build());
                return actionBuilder.build();
            } else {
                return emtpyAction(actionBuilder);
            }

        } else {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
            MatchReactor.getInstance().convert(match, version, setFieldBuilder, datapathid);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());

            return actionBuilder.build();
        }
    }

    private static Action salToOFDecNwTtl(final ActionBuilder actionBuilder) {
        actionBuilder.setActionChoice(new DecNwTtlCaseBuilder().build());
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFPushMplsAction(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        PushMplsActionCase pushMplsActionCase = (PushMplsActionCase) action;
        PushMplsCaseBuilder pushMplsCaseBuilder = new PushMplsCaseBuilder();
        PushMplsActionBuilder pushMplsBuilder = new PushMplsActionBuilder();
        pushMplsBuilder.setEthertype(new EtherType(pushMplsActionCase.getPushMplsAction().getEthernetType()));
        pushMplsCaseBuilder.setPushMplsAction(pushMplsBuilder.build());
        actionBuilder.setActionChoice(pushMplsCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFPushPbbAction(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        PushPbbActionCase pushPbbActionCase = (PushPbbActionCase) action;
        PushPbbCaseBuilder pushPbbCaseBuilder = new PushPbbCaseBuilder();
        PushPbbActionBuilder pushPbbBuilder = new PushPbbActionBuilder();
        pushPbbBuilder.setEthertype(new EtherType(pushPbbActionCase.getPushPbbAction().getEthernetType()));
        pushPbbCaseBuilder.setPushPbbAction(pushPbbBuilder.build());
        actionBuilder.setActionChoice(pushPbbCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFPushVlanAction(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            // if client configure openflow 1.0 switch as a openflow 1.3 switch using openflow 1.3 instructions
            // then we can ignore PUSH_VLAN as set-vlan-id will push a vlan header if not present
            return null;
        }
        PushVlanActionCase pushVlanActionCase = (PushVlanActionCase) action;
        PushVlanAction pushVlanAction = pushVlanActionCase.getPushVlanAction();

        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanBuilder = new PushVlanActionBuilder();
        if (null != pushVlanAction.getEthernetType()) {
            pushVlanBuilder.setEthertype(new EtherType(pushVlanAction.getEthernetType()));
        }
        pushVlanCaseBuilder.setPushVlanAction(pushVlanBuilder.build());
        actionBuilder.setActionChoice(pushVlanCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFSetNwTtl(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        SetNwTtlActionCase nwTtlActionCase = (SetNwTtlActionCase) action;

        SetNwTtlCaseBuilder nwTtlCaseBuilder = new SetNwTtlCaseBuilder();
        SetNwTtlActionBuilder nwTtlBuilder = new SetNwTtlActionBuilder();
        nwTtlBuilder.setNwTtl(nwTtlActionCase.getSetNwTtlAction().getNwTtl());
        nwTtlCaseBuilder.setSetNwTtlAction(nwTtlBuilder.build());
        actionBuilder.setActionChoice(nwTtlCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFSetQueue(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        SetQueueActionCase setQueueActionCase = (SetQueueActionCase) action;
        SetQueueAction setQueueAction = setQueueActionCase.getSetQueueAction();

        SetQueueCaseBuilder setQueueCaseBuilder = new SetQueueCaseBuilder();
        SetQueueActionBuilder setQueueBuilder = new SetQueueActionBuilder();
        setQueueBuilder.setQueueId(setQueueAction.getQueueId());
        setQueueCaseBuilder.setSetQueueAction(setQueueBuilder.build());
        actionBuilder.setActionChoice(setQueueCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFPopMpls(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        PopMplsActionCase popMplsActionCase = (PopMplsActionCase) action;

        PopMplsCaseBuilder popMplsCaseBuilder = new PopMplsCaseBuilder();
        PopMplsActionBuilder popMplsBuilder = new PopMplsActionBuilder();
        popMplsBuilder.setEthertype(new EtherType(new EtherType(popMplsActionCase.getPopMplsAction().getEthernetType())));
        popMplsCaseBuilder.setPopMplsAction(popMplsBuilder.build());
        actionBuilder.setActionChoice(popMplsCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFPopVlan(final ActionBuilder actionBuilder) {
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFPopPBB(final ActionBuilder actionBuilder) {
        actionBuilder.setActionChoice(new PopPbbCaseBuilder().build());
        return emtpyAction(actionBuilder);
    }

    // set-vlan-id (1.0 feature) can be called on  1.3 switches as well using ADSAL apis
    private static Action salToOFSetVlanId(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        SetVlanIdActionCase setvlanidcase = (SetVlanIdActionCase) action;
        SetVlanIdAction setvlanidaction = setvlanidcase.getSetVlanIdAction();

        SetVlanVidActionBuilder vlanidActionBuilder = new SetVlanVidActionBuilder();
        SetVlanVidCaseBuilder setVlanVidCaseBuilder = new SetVlanVidCaseBuilder();

        if (version == OFConstants.OFP_VERSION_1_0) {
            vlanidActionBuilder.setVlanVid(setvlanidaction.getVlanId().getValue());
            setVlanVidCaseBuilder.setSetVlanVidAction(vlanidActionBuilder.build());
            actionBuilder.setActionChoice(setVlanVidCaseBuilder.build());
            return actionBuilder.build();

        } else {
            if (version >= OFConstants.OFP_VERSION_1_3) {
                SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
                SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
                List<MatchEntry> entries = new ArrayList<>();
                MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
                matchBuilder.setOxmClass(OpenflowBasicClass.class);
                matchBuilder.setOxmMatchField(VlanVid.class);
                matchBuilder.setHasMask(false);
                VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
                VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
                vlanVidBuilder.setCfiBit(true);
                vlanVidBuilder.setVlanVid(setvlanidaction.getVlanId().getValue());
                vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
                matchBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
                entries.add(matchBuilder.build());
                setFieldBuilder.setMatchEntry(entries);
                setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
                actionBuilder.setActionChoice(setFieldCaseBuilder.build());
                return actionBuilder.build();
            } else {
                LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
                return null;
            }
        }
    }

    private static Action salToOFSetVlanpcp(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        SetVlanPcpActionCase setvlanpcpcase = (SetVlanPcpActionCase) action;
        SetVlanPcpAction setvlanpcpaction = setvlanpcpcase.getSetVlanPcpAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetVlanPcpActionBuilder setVlanPcpActionBuilder = new SetVlanPcpActionBuilder();
            SetVlanPcpCaseBuilder setVlanPcpCaseBuilder = new SetVlanPcpCaseBuilder();
            setVlanPcpActionBuilder.setVlanPcp(setvlanpcpaction.getVlanPcp().getValue());
            setVlanPcpCaseBuilder.setSetVlanPcpAction(setVlanPcpActionBuilder.build());
            actionBuilder.setActionChoice(setVlanPcpCaseBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

            List<MatchEntry> matchEntriesList = new ArrayList<>();
            matchEntriesList.add(MatchConvertorImpl.toOfVlanPcp(setvlanpcpaction.getVlanPcp()));
            setFieldBuilder.setMatchEntry(matchEntriesList);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());

            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    private static Action salToOFStripVlan(final ActionBuilder actionBuilder, final short version) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            actionBuilder.setActionChoice(new StripVlanCaseBuilder().build());
            return emtpyAction(actionBuilder);
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
            List<MatchEntry> entries = new ArrayList<>();
            MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
            matchBuilder.setOxmClass(OpenflowBasicClass.class);
            matchBuilder.setOxmMatchField(VlanVid.class);
            matchBuilder.setHasMask(false);
            VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
            VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
            vlanVidBuilder.setCfiBit(true);
            vlanVidBuilder.setVlanVid(0x0000);
            vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
            matchBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
            matchBuilder.setHasMask(false);
            entries.add(matchBuilder.build());
            setFieldBuilder.setMatchEntry(entries);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    private static Action salToOFSetDlSrc(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        SetDlSrcActionCase setdlsrccase = (SetDlSrcActionCase) action;
        SetDlSrcAction setdlsrcaction = setdlsrccase.getSetDlSrcAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetDlSrcCaseBuilder setDlSrcCaseBuilder = new SetDlSrcCaseBuilder();
            SetDlSrcActionBuilder setDlSrcActionBuilder = new SetDlSrcActionBuilder();
            setDlSrcActionBuilder.setDlSrcAddress(setdlsrcaction.getAddress());
            setDlSrcCaseBuilder.setSetDlSrcAction(setDlSrcActionBuilder.build());
            actionBuilder.setActionChoice(setDlSrcCaseBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

            List<MatchEntry> entries = new ArrayList<>();
            MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
            matchBuilder.setOxmClass(OpenflowBasicClass.class);
            matchBuilder.setOxmMatchField(EthSrc.class);
            EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
            EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
            ethSrcBuilder.setMacAddress(setdlsrcaction.getAddress());
            matchBuilder.setHasMask(false);
            ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
            matchBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
            entries.add(matchBuilder.build());
            setFieldBuilder.setMatchEntry(entries);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());

            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    private static Action salToOFSetDlDst(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        SetDlDstActionCase setdldstcase = (SetDlDstActionCase) action;
        SetDlDstAction setdldstaction = setdldstcase.getSetDlDstAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetDlDstCaseBuilder setDlDstCaseBuilder = new SetDlDstCaseBuilder();
            SetDlDstActionBuilder setDlDstActionBuilder = new SetDlDstActionBuilder();
            setDlDstActionBuilder.setDlDstAddress(setdldstaction.getAddress());
            setDlDstCaseBuilder.setSetDlDstAction(setDlDstActionBuilder.build());
            actionBuilder.setActionChoice(setDlDstCaseBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

            List<MatchEntry> entries = new ArrayList<>();

            MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
            matchBuilder.setOxmClass(OpenflowBasicClass.class);
            matchBuilder.setOxmMatchField(EthDst.class);
            EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
            EthDstBuilder ethDstBuilder = new EthDstBuilder();
            ethDstBuilder.setMacAddress(setdldstaction.getAddress());
            matchBuilder.setHasMask(false);
            ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
            matchBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
            entries.add(matchBuilder.build());
            setFieldBuilder.setMatchEntry(entries);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }
    }

    protected static Action salToOFSetNwSrc(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        try {
            ActionSetNwSrcReactor.getInstance().convert((SetNwSrcActionCase) action, version, actionBuilder, null);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        return actionBuilder.build();
    }

    protected static Action salToOFSetNwDst(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        try {
            ActionSetNwDstReactor.getInstance().convert((SetNwDstActionCase) action, version, actionBuilder, null);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }

        return actionBuilder.build();
    }

    private static Action salToOFSetNwTos(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version) {

        SetNwTosActionCase setnwtoscase = (SetNwTosActionCase) action;
        SetNwTosAction setnwtosaction = setnwtoscase.getSetNwTosAction();

        if (version == OFConstants.OFP_VERSION_1_0) {
            SetNwTosActionBuilder setNwTosActionBuilder = new SetNwTosActionBuilder();
            SetNwTosCaseBuilder setNwTosCaseBuilder = new SetNwTosCaseBuilder();
            setNwTosActionBuilder.setNwTos(setnwtosaction.getTos().shortValue());
            setNwTosCaseBuilder.setSetNwTosAction(setNwTosActionBuilder.build());
            actionBuilder.setActionChoice(setNwTosCaseBuilder.build());
            return actionBuilder.build();
        } else if (version >= OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

            List<MatchEntry> entries = new ArrayList<>();
            entries.add(MatchConvertorImpl.toOfIpDscp(new Dscp(
                    ActionUtil.tosToDscp(setnwtosaction.getTos().shortValue())
            )));
            setFieldBuilder.setMatchEntry(entries);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());
            return actionBuilder.build();
        } else {
            LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
            return null;
        }

    }

    private static Action salToOFSetTpSrc(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version, final IPProtocols protocol) {

        SetTpSrcActionCase settpsrccase = (SetTpSrcActionCase) action;
        SetTpSrcAction settpsrcaction = settpsrccase.getSetTpSrcAction();
        if (version == OFConstants.OFP_VERSION_1_0) {
            SetTpSrcCaseBuilder setTpSrcCaseBuilder = new SetTpSrcCaseBuilder();
            SetTpSrcActionBuilder setTpSrcActionBuilder = new SetTpSrcActionBuilder();
            setTpSrcActionBuilder.setPort(new PortNumber(settpsrcaction.getPort()
                    .getValue()
                    .longValue()));
            setTpSrcCaseBuilder.setSetTpSrcAction(setTpSrcActionBuilder.build());
            actionBuilder.setActionChoice(setTpSrcCaseBuilder.build());
            return actionBuilder.build();
        } else if (version == OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

            MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
            matchBuilder.setOxmClass(OpenflowBasicClass.class);
            matchBuilder.setHasMask(false);

            InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
            int port = settpsrcaction.getPort().getValue().intValue();
            int type = 0xff & port;

            switch (protocol) {
                case ICMP:
                    matchBuilder.setOxmMatchField(Icmpv4Type.class);
                    Icmpv4TypeCaseBuilder icmpv4TypeCaseBuilder = new Icmpv4TypeCaseBuilder();
                    Icmpv4TypeBuilder icmpv4TypeBuilder = new Icmpv4TypeBuilder();
                    icmpv4TypeBuilder.setIcmpv4Type((short) type);
                    icmpv4TypeCaseBuilder.setIcmpv4Type(icmpv4TypeBuilder.build());
                    matchBuilder.setMatchEntryValue(icmpv4TypeCaseBuilder.build());
                    break;
                case ICMPV6:
                    matchBuilder.setOxmMatchField(Icmpv6Type.class);
                    Icmpv6TypeCaseBuilder icmpv6TypeCaseBuilder = new Icmpv6TypeCaseBuilder();
                    Icmpv6TypeBuilder icmpv6TypeBuilder = new Icmpv6TypeBuilder();
                    icmpv6TypeBuilder.setIcmpv6Type((short) type);
                    icmpv6TypeCaseBuilder.setIcmpv6Type(icmpv6TypeBuilder.build());
                    matchBuilder.setMatchEntryValue(icmpv6TypeCaseBuilder.build());
                    break;
                case TCP:
                    matchBuilder.setOxmMatchField(TcpSrc.class);
                    TcpSrcCaseBuilder tcpSrcCaseBuilder = new TcpSrcCaseBuilder();
                    TcpSrcBuilder tcpSrcBuilder = new TcpSrcBuilder();
                    tcpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    tcpSrcCaseBuilder.setTcpSrc(tcpSrcBuilder.build());
                    matchBuilder.setMatchEntryValue(tcpSrcCaseBuilder.build());
                    break;
                case UDP:
                    matchBuilder.setOxmMatchField(UdpSrc.class);
                    UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
                    UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
                    udpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
                    matchBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
                    break;
                default:
                    LOG.warn("Unknown protocol with combination of SetSourcePort: {}", protocol);
                    break;
            }
            List<MatchEntry> entries = new ArrayList<MatchEntry>();
            entries.add(matchBuilder.build());
            setFieldBuilder.setMatchEntry(entries);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());
            return actionBuilder.build();
        }
        LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
        return null;
    }

    private static Action salToOFSetTpDst(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder, final short version, final IPProtocols protocol) {

        SetTpDstActionCase settpdstcase = (SetTpDstActionCase) action;
        SetTpDstAction settpdstaction = settpdstcase.getSetTpDstAction();
        if (version == OFConstants.OFP_VERSION_1_0) {
            SetTpDstCaseBuilder setTpDstCaseBuilder = new SetTpDstCaseBuilder();
            SetTpDstActionBuilder setTpDstActionBuilder = new SetTpDstActionBuilder();
            setTpDstActionBuilder.setPort(new PortNumber(settpdstaction.getPort().getValue().longValue()));
            setTpDstCaseBuilder.setSetTpDstAction(setTpDstActionBuilder.build());
            actionBuilder.setActionChoice(setTpDstCaseBuilder.build());
            return actionBuilder.build();
        } else if (version == OFConstants.OFP_VERSION_1_3) {
            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

            MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
            matchBuilder.setOxmClass(OpenflowBasicClass.class);
            matchBuilder.setHasMask(false);
            int port = settpdstaction.getPort().getValue().intValue();
            int code = 0xff & port;

            switch (protocol) {
                case ICMP:
                    matchBuilder.setOxmMatchField(Icmpv4Code.class);
                    Icmpv4CodeCaseBuilder icmpv4CodeCaseBuilder = new Icmpv4CodeCaseBuilder();
                    Icmpv4CodeBuilder icmpv4CodeBuilder = new Icmpv4CodeBuilder();
                    icmpv4CodeBuilder.setIcmpv4Code((short) code);
                    icmpv4CodeCaseBuilder.setIcmpv4Code(icmpv4CodeBuilder.build());
                    matchBuilder.setMatchEntryValue(icmpv4CodeCaseBuilder.build());
                    break;
                case ICMPV6:
                    matchBuilder.setOxmMatchField(Icmpv6Code.class);
                    Icmpv6CodeCaseBuilder icmpv6CodeCaseBuilder = new Icmpv6CodeCaseBuilder();
                    Icmpv6CodeBuilder icmpv6CodeBuilder = new Icmpv6CodeBuilder();
                    icmpv6CodeBuilder.setIcmpv6Code((short) code);
                    icmpv6CodeCaseBuilder.setIcmpv6Code(icmpv6CodeBuilder.build());
                    matchBuilder.setMatchEntryValue(icmpv6CodeCaseBuilder.build());
                    break;
                case TCP:
                    matchBuilder.setOxmMatchField(TcpDst.class);
                    TcpDstCaseBuilder tcpDstCaseBuilder = new TcpDstCaseBuilder();
                    TcpDstBuilder tcpDstBuilder = new TcpDstBuilder();
                    tcpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
                    matchBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
                    break;
                case UDP:
                    matchBuilder.setOxmMatchField(UdpDst.class);
                    UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
                    UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
                    udpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(port));
                    udpDstCaseBuilder.setUdpDst(udpDstBuilder.build());
                    matchBuilder.setMatchEntryValue(udpDstCaseBuilder.build());
                    break;
                default:
                    LOG.warn("Unknown protocol with combination of SetSourcePort: {}", protocol);
                    break;
            }
            List<MatchEntry> entries = new ArrayList<MatchEntry>();
            entries.add(matchBuilder.build());
            setFieldBuilder.setMatchEntry(entries);
            setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
            actionBuilder.setActionChoice(setFieldCaseBuilder.build());
            return actionBuilder.build();
        }
        LOG.error(UNKNOWN_ACTION_TYPE_VERSION, version);
        return null;
    }

    private static Action salToOFGroupAction(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        GroupActionCase groupActionCase = (GroupActionCase) action;
        GroupAction groupAction = groupActionCase.getGroupAction();
        GroupCaseBuilder groupCaseBuilder = new GroupCaseBuilder();
        GroupActionBuilder groupActionBuilder = new GroupActionBuilder();

        if (null != groupAction.getGroupId()) {
            groupActionBuilder.setGroupId(groupAction.getGroupId());
        } else {
            groupActionBuilder.setGroupId(Long.parseLong(groupAction.getGroup()));
        }

        groupCaseBuilder.setGroupAction(groupActionBuilder.build());
        actionBuilder.setActionChoice(groupCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFDecMplsTtl(final ActionBuilder actionBuilder) {
        actionBuilder.setActionChoice(new DecMplsTtlCaseBuilder().build());
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFSetMplsTtl(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action,
            final ActionBuilder actionBuilder) {
        SetMplsTtlActionCase mplsTtlActionCase = (SetMplsTtlActionCase) action;
        SetMplsTtlAction mplsTtlAction = mplsTtlActionCase.getSetMplsTtlAction();
        SetMplsTtlCaseBuilder setMplsTtlCaseBuilder = new SetMplsTtlCaseBuilder();
        SetMplsTtlActionBuilder setMplsTtlBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlBuilder.setMplsTtl(mplsTtlAction.getMplsTtl()/* SAL */);
        setMplsTtlCaseBuilder.setSetMplsTtlAction(setMplsTtlBuilder.build());
        actionBuilder.setActionChoice(setMplsTtlCaseBuilder.build());
        return actionBuilder.build();
    }

    private static Action salToOFCopyTTLIIn(final ActionBuilder actionBuilder) {
        actionBuilder.setActionChoice(new CopyTtlInCaseBuilder().build());
        return emtpyAction(actionBuilder);
    }

    private static Action salToOFCopyTTLIOut(final ActionBuilder actionBuilder) {
        actionBuilder.setActionChoice(new CopyTtlOutCaseBuilder().build());
        return emtpyAction(actionBuilder);

    }

    private static Action emtpyAction(final ActionBuilder actionBuilder) {
        return actionBuilder.build();
    }

    private static Action salToOFAction(
            final OutputActionCase outputActionCase,
            final ActionBuilder actionBuilder, final short version) {

        OutputAction outputAction = outputActionCase.getOutputAction();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();

        if (outputAction.getMaxLength() != null) {
            outputBuilder.setMaxLength(outputAction.getMaxLength());
        } else {
            outputBuilder.setMaxLength(0);
        }
        Uri uri = outputAction.getOutputNodeConnector();
        OpenflowVersion ofVersion = OpenflowVersion.get(version);
        Long portNumber = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, uri.getValue());
        if (OpenflowPortsUtil.checkPortValidity(ofVersion, portNumber)) {
            outputBuilder.setPort(new PortNumber(portNumber));
        } else {
            LOG.error("Invalid Port specified {} for Output Action for OF version: {}", portNumber, ofVersion);
        }
        caseBuilder.setOutputAction(outputBuilder.build());
        actionBuilder.setActionChoice(caseBuilder.build());
        return actionBuilder.build();

    }

    /**
     * Method to convert OF actions associated with bucket to SAL Actions.
     *
     * @param actionList action list
     * @param ofVersion  current ofp version
     * @param actionPath TODO
     * @return List of converted SAL Actions.
     */
    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> toMDSalActions(
            final List<Action> actionList, final OpenflowVersion ofVersion, final ActionPath actionPath) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> bucketActions = new ArrayList<>();
        if(actionList != null){
            for (Action action : actionList) {
                if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase) {
                    bucketActions.add(ofToSALOutputAction(ofVersion, action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase) {
                    bucketActions.add(ofToSALGroupAction(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase) {
                    CopyTtlOutBuilder copyTtlOutaction = new CopyTtlOutBuilder();
                    bucketActions.add(new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder().setCopyTtlOut(copyTtlOutaction.build()).build());
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase) {
                    CopyTtlInBuilder copyTtlInaction = new CopyTtlInBuilder();
                    bucketActions.add(new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder().setCopyTtlIn(copyTtlInaction.build()).build());

                } else if (action.getActionChoice() instanceof SetMplsTtlCase) {
                    bucketActions.add(ofToSALSetMplsTtl(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase) {
                    DecMplsTtlBuilder decMplsTtl = new DecMplsTtlBuilder();
                    bucketActions.add(new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder().setDecMplsTtl(decMplsTtl.build()).build());
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase) {
                    bucketActions.add(ofToSALPushVlanAction(action));
                } else if ((action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase)
                        || (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCase)) {
                    // OF1.0 nodes will emit StripVlan and OF1.3+ will emit StripVlan/PopVlan, convert both to PopVlan for SAL
                    PopVlanActionBuilder popVlan = new PopVlanActionBuilder();
                    bucketActions.add(new PopVlanActionCaseBuilder().setPopVlanAction(popVlan.build()).build());
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase) {
                    bucketActions.add(ofToSALPushMplsAction(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase) {
                    bucketActions.add(ofToSALPopMplsAction(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase) {
                    bucketActions.add(ofToSALSetQueue(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase) {
                    bucketActions.add(ofToSALSetNwTtl(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCase) {
                    DecNwTtlBuilder decNwTtl = new DecNwTtlBuilder();
                    bucketActions.add(new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder()
                            .setDecNwTtl(decNwTtl.build()).build());
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase) {
                    bucketActions.add(new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder()
                            .setSetField(MatchConvertorImpl.fromOFSetFieldToSALSetFieldAction(action, ofVersion)).build());
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase) {
                    bucketActions.add(ofToSALPushPbbAction(action));
                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCase) {
                    PopPbbActionBuilder popPbb = new PopPbbActionBuilder();
                    bucketActions.add(new PopPbbActionCaseBuilder().setPopPbbAction(popPbb.build()).build());

                } else if (action.getActionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCase) {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder setNwDstActionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder();
                    bucketActions.add(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build());

                } else {
                    /**
                     * TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL)
                     * - we might also need a way on how to identify exact type of augmentation to be
                     *   used as match can be bound to multiple models
                     */
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action processedAction =
                            ActionExtensionHelper.processAlienAction(action, ofVersion, actionPath);
                    if (processedAction != null) {
                        bucketActions.add(processedAction);
                    }
                }
            }
        }
        return bucketActions;
    }

    /**
     * Method converts OF Output action object to SAL Output action object.
     *
     * @param ofVersion openflow version
     * @param action    org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.
     *                  action.rev130731.actions.actions.list.Action
     * @return OutputAction
     */
    public static OutputActionCase ofToSALOutputAction(final OpenflowVersion ofVersion, final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder outputAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder();

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase) action.getActionChoice();

        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputAction outputActionFromOF = actionCase.getOutputAction();
        if (outputActionFromOF.getPort() != null) {
            PortNumberUni protocolAgnosticPort = OpenflowPortsUtil.getProtocolAgnosticPort(
                    ofVersion, outputActionFromOF.getPort().getValue());
            String portNumberAsString = OpenflowPortsUtil.portNumberToString(protocolAgnosticPort);
            outputAction.setOutputNodeConnector(new Uri(portNumberAsString));
        } else {
            LOG.error("Provided action is not OF Output action, no associated port found!");
        }

        Integer maxLength = outputActionFromOF.getMaxLength();
        if (maxLength != null) {
            outputAction.setMaxLength(maxLength);
        } else {
            LOG.error("Provided action is not OF Output action, no associated length found!");
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder outputActionCaseBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder();
        outputActionCaseBuilder.setOutputAction(outputAction.build());
        return outputActionCaseBuilder.build();
    }

    /**
     * Method converts OF GroupAction object to SAL GroupAction object
     *
     * @param action action
     * @return GroupAction group action
     */
    public static GroupActionCase ofToSALGroupAction(final Action action) {
        GroupCase actionCase = (GroupCase) action.getActionChoice();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupAction groupActionFromOF =
                actionCase.getGroupAction();

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder groupAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder();
        groupAction.setGroupId(groupActionFromOF.getGroupId());

        return new GroupActionCaseBuilder().setGroupAction(groupAction.build()).build();
    }

    /**
     * Method converts OF SetMplsTTL action object to SAL SetMplsTTL action
     * object.
     *
     * @param action action
     * @return set-mpls ttl action
     */
    public static SetMplsTtlActionCase ofToSALSetMplsTtl(final Action action) {
        SetMplsTtlCase actionCase = (SetMplsTtlCase) action.getActionChoice();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action
                .choice.set.mpls.ttl._case.SetMplsTtlAction setMplsTtlActionFromOF = actionCase.getSetMplsTtlAction();

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder mplsTtlAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.mpls.ttl.action._case.SetMplsTtlActionBuilder();
        mplsTtlAction.setMplsTtl(setMplsTtlActionFromOF.getMplsTtl());
        return new SetMplsTtlActionCaseBuilder().setSetMplsTtlAction(mplsTtlAction.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action input actioj
     * @return PushVlanAction
     */
    public static PushVlanActionCase ofToSALPushVlanAction(final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase) action.getActionChoice();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanAction pushVlanActionFromOF =
                actionCase.getPushVlanAction();

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder pushVlanAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder();

        pushVlanAction.setEthernetType(pushVlanActionFromOF.getEthertype().getValue());
        return new PushVlanActionCaseBuilder().setPushVlanAction(pushVlanAction.build()).build();
    }

    /**
     * Method converts OF PushMpls action to SAL PushMpls action.
     *
     * @param action action
     * @return PushMplsAction
     */
    public static PushMplsActionCase ofToSALPushMplsAction(final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase) action.getActionChoice();
        PushMplsAction pushMplsActionFromOF = actionCase.getPushMplsAction();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder pushMplsAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.mpls.action._case.PushMplsActionBuilder();
        pushMplsAction.setEthernetType(pushMplsActionFromOF.getEthertype().getValue());
        return new PushMplsActionCaseBuilder().setPushMplsAction(pushMplsAction.build()).build();
    }

    /**
     * Method converts OF PopMpls action to SAL PopMpls action.
     *
     * @param action action
     * @return PopMplsActionCase
     */
    public static PopMplsActionCase ofToSALPopMplsAction(final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase) action.getActionChoice();
        PopMplsAction popMplsActionFromOF = actionCase.getPopMplsAction();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder popMplsAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.pop.mpls.action._case.PopMplsActionBuilder();
        popMplsAction.setEthernetType(popMplsActionFromOF.getEthertype().getValue());
        return new PopMplsActionCaseBuilder().setPopMplsAction(popMplsAction.build()).build();
    }

    /**
     * Method converts OF SetQueue action to SAL SetQueue action.
     *
     * @param action action
     * @return SetQueueAction
     */
    public static SetQueueActionCase ofToSALSetQueue(final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase) action.getActionChoice();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.queue._case.SetQueueAction queueActionFromOF =
                actionCase.getSetQueueAction();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder setQueueAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder();
        setQueueAction.setQueueId(queueActionFromOF.getQueueId());
        return new SetQueueActionCaseBuilder().setSetQueueAction(setQueueAction.build()).build();
    }

    /**
     * Method converts OF SetNwTtl action to SAL SetNwTtl action.
     *
     * @param action action
     * @return SetNwTtlAction
     */
    public static SetNwTtlActionCase ofToSALSetNwTtl(final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase) action.getActionChoice();
        SetNwTtlAction setNwTtlActionFromOf = actionCase.getSetNwTtlAction();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder setNwTtl =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.ttl.action._case.SetNwTtlActionBuilder();
        setNwTtl.setNwTtl(setNwTtlActionFromOf.getNwTtl());
        return new SetNwTtlActionCaseBuilder().setSetNwTtlAction(setNwTtl.build()).build();
    }

    /**
     * Method converts OF Pushvlan action to SAL PushVlan action.
     *
     * @param action action
     * @return PushVlanAction
     */
    public static PushPbbActionCase ofToSALPushPbbAction(final Action action) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase actionCase =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase) action.getActionChoice();
        PushPbbAction pushPbbActionFromOf = actionCase.getPushPbbAction();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder pushPbbAction =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.pbb.action._case.PushPbbActionBuilder();
        pushPbbAction.setEthernetType(pushPbbActionFromOf.getEthertype().getValue());
        return new PushPbbActionCaseBuilder().setPushPbbAction(pushPbbAction.build()).build();
    }

    //TODO make a model in YANG for protocols
    /*private enum IPProtocols {
        ICMP(1),
        TCP(6),
        UDP(17),
        ICMPV6(58);

        private int protocol;

        private static Map<Integer, IPProtocols> valueMap;
        static {
            valueMap = new HashMap<>();
            for(IPProtocols protocols : IPProtocols.values()) {
                valueMap.put(protocols.protocol, protocols);
            }
        }

        private IPProtocols(int value) {
            this.protocol = value;
        }

        private byte getValue() {
            return (byte) this.protocol;
        }

        private Short getShortValue() {
            return (short) protocol;
        }

        private IPProtocols fromProtocolNum(Short protocolNum) {
            return valueMap.get(protocolNum);
        }
    }    */

}
