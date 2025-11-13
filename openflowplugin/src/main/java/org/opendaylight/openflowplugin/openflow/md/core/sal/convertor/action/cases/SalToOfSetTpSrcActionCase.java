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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SalToOfSetTpSrcActionCase extends ConvertorCase<SetTpSrcActionCase, Action, ActionConvertorData> {
    private static final Logger LOG = LoggerFactory.getLogger(SalToOfSetTpSrcActionCase.class);

    public SalToOfSetTpSrcActionCase() {
        super(SetTpSrcActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Action> process(final SetTpSrcActionCase source, final ActionConvertorData data,
            final ConvertorExecutor convertorExecutor) {
        IPProtocols protocol = null;

        if (data.getIpProtocol() != null) {
            protocol = IPProtocols.fromProtocolNum(data.getIpProtocol());
        }

        SetTpSrcAction settpsrcaction = source.getSetTpSrcAction();

        MatchEntryBuilder matchBuilder = new MatchEntryBuilder()
                .setOxmClass(OpenflowBasicClass.VALUE)
                .setHasMask(Boolean.FALSE);

        final Uint16 port = settpsrcaction.getPort().getValue();
        final Uint8 type = Uint8.valueOf(0xff & port.toJava());

        if (protocol != null) {
            switch (protocol) {
                case ICMP:
                    matchBuilder.setOxmMatchField(Icmpv4Type.VALUE);
                    matchBuilder.setMatchEntryValue(new Icmpv4TypeCaseBuilder()
                        .setIcmpv4Type(new Icmpv4TypeBuilder().setIcmpv4Type(type).build())
                        .build());
                    break;
                case ICMPV6:
                    matchBuilder.setOxmMatchField(Icmpv6Type.VALUE);
                    matchBuilder.setMatchEntryValue(new Icmpv6TypeCaseBuilder()
                        .setIcmpv6Type(new Icmpv6TypeBuilder().setIcmpv6Type(type).build())
                        .build());
                    break;
                case TCP:
                    matchBuilder.setOxmMatchField(TcpSrc.VALUE);
                    matchBuilder.setMatchEntryValue(new TcpSrcCaseBuilder()
                        .setTcpSrc(new TcpSrcBuilder()
                            .setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types
                                .rev130715.PortNumber(port))
                            .build())
                        .build());
                    break;
                case UDP:
                    matchBuilder.setOxmMatchField(UdpSrc.VALUE);
                    matchBuilder.setMatchEntryValue(new UdpSrcCaseBuilder()
                        .setUdpSrc(new UdpSrcBuilder()
                            .setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types
                                .rev130715.PortNumber(port))
                            .build())
                        .build());
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

        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
        setFieldBuilder.setMatchEntry(entries);

        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());

        return Optional.of(new ActionBuilder()
                .setActionChoice(setFieldCaseBuilder.build())
                .build());
    }
}
