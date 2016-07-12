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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.src._case.TcpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.src._case.UdpSrcBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfSetTpSrcActionCase extends ConvertorCase<SetTpSrcActionCase, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfSetTpSrcActionCase.class);

    public SalToOfSetTpSrcActionCase() {
        super(SetTpSrcActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetTpSrcActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        IPProtocols protocol = null;

        if (data.getIpProtocol() != null) {
            protocol = IPProtocols.fromProtocolNum(data.getIpProtocol());
        }

        SetTpSrcAction settpsrcaction = source.getSetTpSrcAction();
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();

        MatchEntryBuilder matchBuilder = new MatchEntryBuilder();
        matchBuilder.setOxmClass(OpenflowBasicClass.class);
        matchBuilder.setHasMask(false);

        int port = settpsrcaction.getPort().getValue();
        int type = 0xff & port;

        if (protocol != null) {
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
                    tcpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(port));
                    tcpSrcCaseBuilder.setTcpSrc(tcpSrcBuilder.build());
                    matchBuilder.setMatchEntryValue(tcpSrcCaseBuilder.build());
                    break;
                case UDP:
                    matchBuilder.setOxmMatchField(UdpSrc.class);
                    UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
                    UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
                    udpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(port));
                    udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
                    matchBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
                    break;
                default:
                    LOG.warn("Unknown protocol with combination of SetSourcePort: {}", protocol);
                    break;
            }
        } else {
            LOG.warn("Null protocol with combination of SetSourcePort");
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