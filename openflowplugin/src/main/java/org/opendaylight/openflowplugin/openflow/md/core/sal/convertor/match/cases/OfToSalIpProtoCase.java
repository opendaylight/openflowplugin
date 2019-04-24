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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.proto._case.IpProto;
import org.opendaylight.yangtools.yang.common.Uint8;

public class OfToSalIpProtoCase extends ConvertorCase<IpProtoCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalIpProtoCase() {
        super(IpProtoCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@NonNull final IpProtoCase source, final MatchResponseConvertorData data,
            final ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final IpMatchBuilder ipMatchBuilder = data.getIpMatchBuilder();

        IpProto ipProto = source.getIpProto();
        Uint8 protocolNumber = ipProto.getProtocolNumber();

        if (protocolNumber != null) {
            ipMatchBuilder.setIpProtocol(protocolNumber);
            matchBuilder.setIpMatch(ipMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
