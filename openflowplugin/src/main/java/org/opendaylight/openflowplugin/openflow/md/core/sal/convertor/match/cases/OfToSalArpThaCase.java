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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tha._case.ArpTha;

public class OfToSalArpThaCase extends ConvertorCase<ArpThaCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalArpThaCase() {
        super(ArpThaCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull ArpThaCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final ArpMatchBuilder arpMatchBuilder = data.getArpMatchBuilder();

        ArpTha arpTha = source.getArpTha();
        MacAddress macAddress = arpTha.getMacAddress();

        if (macAddress != null) {
            ArpTargetHardwareAddressBuilder arpTargetHardwareAddressBuilder = new ArpTargetHardwareAddressBuilder();
            arpTargetHardwareAddressBuilder.setAddress(macAddress);
            byte[] mask = arpTha.getMask();

            if (mask != null) {
                arpTargetHardwareAddressBuilder.setMask(new MacAddress(ByteBufUtils
                        .macAddressToString(mask)));
            }

            arpMatchBuilder.setArpTargetHardwareAddress(arpTargetHardwareAddressBuilder.build());
            matchBuilder.setLayer3Match(arpMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
