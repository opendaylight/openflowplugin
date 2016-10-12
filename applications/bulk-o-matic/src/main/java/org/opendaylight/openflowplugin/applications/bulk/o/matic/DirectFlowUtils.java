/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import java.util.Arrays;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.type._case.EthTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

class DirectFlowUtils {
    static FlowModInputBuilder buildFlow(final Short tableId, final Match match) {
        return new FlowModInputBuilder()
                .setTableId(new TableId(tableId.longValue()))
                .setMatch(match)
                // We are adding flows here, so set this flow mod to do it
                .setCommand(FlowModCommand.OFPFCADD)
                // Openflowplugin defaults
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .setCookie(OFConstants.DEFAULT_COOKIE)
                .setCookieMask(OFConstants.DEFAULT_COOKIE_MASK)
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setOutPort(new PortNumber(OFConstants.ANY))
                .setOutGroup(OFConstants.ANY)
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setPriority(OFConstants.DEFAULT_FLOW_PRIORITY)
                .setFlags(new FlowModFlags(false, false, false, false, false));
    }

    static Match buildMatch(final Integer sourceIp) {
        final String ipString = BulkOMaticUtils.ipIntToStr(sourceIp);

        return new MatchBuilder()
                .setType(OxmMatchType.class)
                .setMatchEntry(Arrays.asList(
                        // First, set Ethernet type to 0x800
                        new MatchEntryBuilder()
                                .setOxmClass(OpenflowBasicClass.class)
                                .setHasMask(false)
                                .setOxmMatchField(EthType.class)
                                .setMatchEntryValue(new EthTypeCaseBuilder()
                                        .setEthType(new EthTypeBuilder()
                                                .setEthType(new EtherType(0x800))
                                                .build())
                                        .build())
                                .build(),
                        // Then, set IPv4 source address
                        new MatchEntryBuilder()
                                .setOxmClass(OpenflowBasicClass.class)
                                .setOxmMatchField(Ipv4Src.class)
                                .setHasMask(false)
                                .setMatchEntryValue(new Ipv4SrcCaseBuilder()
                                        .setIpv4Src(new Ipv4SrcBuilder()
                                                .setIpv4Address(new Ipv4Address(ipString.substring(0, ipString.length() - 3)))
                                                .build())
                                        .build())
                                .build()))
                .build();
    }
}
