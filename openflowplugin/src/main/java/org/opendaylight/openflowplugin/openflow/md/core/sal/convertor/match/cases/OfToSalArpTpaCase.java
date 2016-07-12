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
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tpa._case.ArpTpa;

public class OfToSalArpTpaCase extends ConvertorCase<ArpTpaCase, MatchBuilder, MatchResponseConvertorData> {
    public OfToSalArpTpaCase() {
        super(ArpTpaCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<MatchBuilder> process(@Nonnull ArpTpaCase source, MatchResponseConvertorData data, ConvertorExecutor convertorExecutor) {
        final MatchBuilder matchBuilder = data.getMatchBuilder();
        final ArpMatchBuilder arpMatchBuilder = data.getArpMatchBuilder();

        ArpTpa arpTpa = source.getArpTpa();

        if (arpTpa != null) {
            int mask = 32;

            if (null != arpTpa.getMask()) {
                mask = IpConversionUtil.countBits(arpTpa.getMask());
            }

            Ipv4Prefix ipv4Prefix = IpConversionUtil.createPrefix(arpTpa.getIpv4Address(), mask);
            arpMatchBuilder.setArpTargetTransportAddress(ipv4Prefix);
            matchBuilder.setLayer3Match(arpMatchBuilder.build());
        }

        return Optional.of(matchBuilder);
    }
}
