/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.sha._case.ArpSha;

public class OfToSalArpShaCase extends ConvertorCase<ArpShaCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalArpShaCase() {
        super(ArpShaCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull ArpShaCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final ArpMatchBuilder arpMatchBuilder = data.getArpMatchBuilder();

        ArpSha arpSha = source.getArpSha();
        MacAddress macAddress = arpSha.getMacAddress();

        if (macAddress != null) {
            ArpSourceHardwareAddressBuilder arpSourceHardwareAddressBuilder = new ArpSourceHardwareAddressBuilder();
            arpSourceHardwareAddressBuilder.setAddress(macAddress);
            byte[] mask = arpSha.getMask();

            if (mask != null) {
                arpSourceHardwareAddressBuilder.setMask(new MacAddress(ByteBufUtils
                        .macAddressToString(mask)));
            }

            arpMatchBuilder.setArpSourceHardwareAddress(arpSourceHardwareAddressBuilder.build());
            matchBuilder.setLayer3Match(arpMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
