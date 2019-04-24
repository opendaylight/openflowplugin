/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.bos._case.MplsBos;

public class OfToSalMplsBosCase extends ConvertorCase<MplsBosCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalMplsBosCase() {
        super(MplsBosCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@NonNull MplsBosCase source, MatchResponseConvertorData data,
            ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final ProtocolMatchFieldsBuilder protocolMatchFieldsBuilder = data.getProtocolMatchFieldsBuilder();

        MplsBos mplsBos = source.getMplsBos();

        if (mplsBos != null) {
            protocolMatchFieldsBuilder.setMplsBos(mplsBos.isBos() ? (short) 1 : (short) 0);
            matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
