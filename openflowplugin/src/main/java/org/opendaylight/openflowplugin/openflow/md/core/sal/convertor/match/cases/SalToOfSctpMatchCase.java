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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.dst._case.SctpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.src._case.SctpSrcBuilder;

public class SalToOfSctpMatchCase extends ConvertorCase<SctpMatch, List<MatchEntry>, VersionConvertorData> {
    public SalToOfSctpMatchCase() {
        super(SctpMatch.class, true);
    }

    @Override
    public Optional<List<MatchEntry>> process(@Nonnull SctpMatch source, VersionConvertorData data, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (source.getSctpSourcePort() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(SctpSrc.class);

            SctpSrcCaseBuilder sctpSrcCaseBuilder = new SctpSrcCaseBuilder();
            SctpSrcBuilder sctpSrcBuilder = new SctpSrcBuilder();
            sctpSrcBuilder.setPort(source.getSctpSourcePort());
            sctpSrcCaseBuilder.setSctpSrc(sctpSrcBuilder.build());
            matchEntryBuilder.setMatchEntryValue(sctpSrcCaseBuilder.build());
            matchEntryBuilder.setHasMask(false);
            result.add(matchEntryBuilder.build());
        }

        if (source.getSctpDestinationPort() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(SctpDst.class);

            SctpDstCaseBuilder sctpDstCaseBuilder = new SctpDstCaseBuilder();
            SctpDstBuilder sctpDstBuilder = new SctpDstBuilder();
            sctpDstBuilder.setPort(source.getSctpDestinationPort());
            sctpDstCaseBuilder.setSctpDst(sctpDstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(sctpDstCaseBuilder.build());
            matchEntryBuilder.setHasMask(false);
            result.add(matchEntryBuilder.build());
        }

        return Optional.of(result);
    }
}
