/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IPProtocols;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.dst._case.TcpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfSetTpDstActionCase extends ConvertorCase<SetTpDstActionCase, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfSetTpDstActionCase.class);

    public SalToOfSetTpDstActionCase() {
        super(SetTpDstActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(final SetTpDstActionCase source, final ActionConvertorData data,
            final ConvertorExecutor convertorExecutor) {
        final MatchEntryBuilder matchBuilder = new MatchEntryBuilder()
                .setOxmClass(OpenflowBasicClass.class)
                .setHasMask(Boolean.FALSE);

        final Uint8 ipProtocol = data.getIpProtocol();
        final IPProtocols protocol;
        if (ipProtocol != null) {
            protocol = IPProtocols.fromProtocolNum(ipProtocol);
        } else {
            protocol = null;
        }

        if (protocol != null) {
            final Uint16 port = source.getSetTpDstAction().getPort().getValue();

            switch (protocol) {
                case ICMP:
                    matchBuilder.setOxmMatchField(Icmpv4Code.class);
                    matchBuilder.setMatchEntryValue(new Icmpv4CodeCaseBuilder()
                        .setIcmpv4Code(new Icmpv4CodeBuilder()
                            .setIcmpv4Code(Uint8.valueOf(0xFF & port.toJava()))
                            .build())
                        .build());
                    break;
                case ICMPV6:
                    matchBuilder.setOxmMatchField(Icmpv6Code.class);
                    matchBuilder.setMatchEntryValue(new Icmpv6CodeCaseBuilder()
                        .setIcmpv6Code(new Icmpv6CodeBuilder()
                            .setIcmpv6Code(Uint8.valueOf(0xFF & port.toJava()))
                            .build())
                        .build());
                    break;
                case TCP:
                    matchBuilder.setOxmMatchField(TcpDst.class);
                    TcpDstCaseBuilder tcpDstCaseBuilder = new TcpDstCaseBuilder();
                    TcpDstBuilder tcpDstBuilder = new TcpDstBuilder();
                    tcpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types
                            .rev130715.PortNumber(port));
                    tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
                    matchBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
                    break;
                case UDP:
                    matchBuilder.setOxmMatchField(UdpDst.class);
                    UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
                    UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
                    udpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types
                            .rev130715.PortNumber(port));
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

        return Optional.of(new ActionBuilder()
            .setActionChoice(new SetFieldCaseBuilder()
                .setSetFieldAction(new SetFieldActionBuilder().setMatchEntry(List.of(matchBuilder.build())).build())
                .build())
            .build());
    }
}
