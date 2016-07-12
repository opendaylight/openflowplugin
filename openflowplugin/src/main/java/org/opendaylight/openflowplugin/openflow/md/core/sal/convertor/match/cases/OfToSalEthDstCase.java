/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import static org.opendaylight.openflowjava.util.ByteBufUtils.macAddressToString;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDst;

public class OfToSalEthDstCase extends ConvertorCase<EthDstCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalEthDstCase() {
        super(EthDstCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull EthDstCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final EthernetMatchBuilder ethMatchBuilder = data.getEthernetMatchBuilder();

        final EthDst ethDstCase = source.getEthDst();

        if (ethDstCase != null) {
            EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
            ethDestinationBuilder.setAddress(ethDstCase.getMacAddress());
            byte[] destinationMask = ethDstCase.getMask();

            if (destinationMask != null) {
                ethDestinationBuilder.setMask(new MacAddress(macAddressToString(destinationMask)));
            }

            ethMatchBuilder.setEthernetDestination(ethDestinationBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
