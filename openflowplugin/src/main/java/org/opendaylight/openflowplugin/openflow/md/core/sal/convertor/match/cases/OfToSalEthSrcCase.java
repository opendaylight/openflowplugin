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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrc;

public class OfToSalEthSrcCase extends ConvertorCase<EthSrcCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalEthSrcCase() {
        super(EthSrcCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@NonNull EthSrcCase source, MatchResponseConvertorData data,
            ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final EthernetMatchBuilder ethMatchBuilder = data.getEthernetMatchBuilder();

        final EthSrc ethSrcCase = source.getEthSrc();

        if (ethSrcCase != null) {
            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
            ethSourceBuilder.setAddress(ethSrcCase.getMacAddress());
            byte[] mask = ethSrcCase.getMask();

            if (mask != null) {
                ethSourceBuilder.setMask(IetfYangUtil.macAddressFor(mask));
            }

            ethMatchBuilder.setEthernetSource(ethSourceBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
