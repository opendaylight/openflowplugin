/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IPProtocols;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.dst._case.TcpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfSetTpDstActionCase extends ConvertorCase<SetTpDstActionCase, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfSetTpDstActionCase.class);

    public SalToOfSetTpDstActionCase() {
        super(SetTpDstActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetTpDstActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        IPProtocols protocol = null;

        if (data.getIpProtocol() != null) {
            protocol = IPProtocols.fromProtocolNum(data.getIpProtocol());
        }

        SetTpDstAction settpdstaction = source.getSetTpDstAction();
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setHasMask(false);
        int port = settpdstaction.getPort().getValue();
        int code = 0xff & port;

        if (protocol != null) {
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
                    tcpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(port));
                    tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
                    matchBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
                    break;
                case UDP:
                    matchBuilder.setOxmMatchField(UdpDst.class);
                    UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
                    UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
                    udpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(port));
                    udpDstCaseBuilder.setUdpDst(udpDstBuilder.build());
                    matchBuilder.setMatchEntryValue(udpDstCaseBuilder.build());
                    break;
                default:
                    LOG.warn("Unknown protocol with combination of SetSourcePort: {}", protocol);
                    break;
            }
        } else {
            LOG.warn("Missing protocol with combination of SetSourcePort");
        }

        List<MatchEntry> entries = new ArrayList<>();
        entries.add(matchBuilder.build());
        setFieldBuilder.setMatchEntry(entries);
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setFieldCaseBuilder.build())
                .build());
    }
}