/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.actions;

import java.util.Optional;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IPProtocols;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;

public class SetTpDstActionSerializer extends AbstractSetFieldActionSerializer {

    @Override
    protected SetFieldCase buildAction(Action input) {
        final SetTpDstAction setTpDstAction = SetTpDstActionCase.class.cast(input).getSetTpDstAction();
        final PortNumber port = setTpDstAction.getPort();
        final SetFieldBuilder builder = new SetFieldBuilder();

        Optional.ofNullable(IPProtocols.fromProtocolNum(setTpDstAction.getIpProtocol())).ifPresent(proto -> {
            switch (proto) {
                case ICMP: {
                    builder.setIcmpv4Match(new Icmpv4MatchBuilder()
                            .setIcmpv4Code((short) (0xFF & port.getValue()))
                            .build());
                    break;
                }
                case ICMPV6: {
                    builder.setIcmpv6Match(new Icmpv6MatchBuilder()
                            .setIcmpv6Code((short) (0xFF & port.getValue()))
                            .build());
                    break;
                }
                case TCP: {
                    builder.setLayer4Match(new TcpMatchBuilder()
                            .setTcpDestinationPort(port)
                            .build());
                    break;
                }
                case UDP: {
                    builder.setLayer4Match(new UdpMatchBuilder()
                            .setUdpDestinationPort(port)
                            .build());
                    break;
                }
            }
        });

        return new SetFieldCaseBuilder().setSetField(builder.build()).build();
    }

}
