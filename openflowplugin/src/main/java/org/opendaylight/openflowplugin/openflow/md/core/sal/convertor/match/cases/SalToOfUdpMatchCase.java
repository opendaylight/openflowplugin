/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.src._case.UdpSrcBuilder;

public class SalToOfUdpMatchCase extends ConvertorCase<UdpMatch, List<MatchEntry>, VersionConvertorData> {
    public SalToOfUdpMatchCase() {
        super(UdpMatch.class, true);
    }

    @Override
    public Optional<List<MatchEntry>> process(@Nonnull UdpMatch source, VersionConvertorData data, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (source.getUdpSourcePort() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(UdpSrc.class);

            UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
            UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
            udpSrcBuilder.setPort(source.getUdpSourcePort());
            udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
            matchEntryBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
            matchEntryBuilder.setHasMask(false);
            result.add(matchEntryBuilder.build());
        }

        if (source.getUdpDestinationPort() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(UdpDst.class);

            UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
            UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
            udpDstBuilder.setPort(source.getUdpDestinationPort());
            udpDstCaseBuilder.setUdpDst(udpDstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(udpDstCaseBuilder.build());
            matchEntryBuilder.setHasMask(false);
            result.add(matchEntryBuilder.build());
        }

        return Optional.of(result);
    }
}
