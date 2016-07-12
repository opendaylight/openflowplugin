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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;

public class SalToOfIpv6MatchArbitraryBitMaskCase extends ConvertorCase<Ipv6MatchArbitraryBitMask, List<MatchEntry>, VersionConvertorData> {
    public SalToOfIpv6MatchArbitraryBitMaskCase() {
        super(Ipv6MatchArbitraryBitMask.class, true);
    }

    @Override
    public Optional<List<MatchEntry>> process(@Nonnull Ipv6MatchArbitraryBitMask source, VersionConvertorData data, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (source.getIpv6SourceAddressNoMask() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Src.class);

            Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
            Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
            ipv6SrcBuilder.setIpv6Address(source.getIpv6SourceAddressNoMask());
            Ipv6ArbitraryMask sourceArbitrarySubNetMask = source.getIpv6SourceArbitraryBitmask();
            boolean hasMask = false;
            if (sourceArbitrarySubNetMask != null) {
                byte[] maskByteArray = IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(sourceArbitrarySubNetMask);
                if (maskByteArray != null) {
                    ipv6SrcBuilder.setMask(maskByteArray);
                    hasMask = true;
                }
            }
            matchEntryBuilder.setHasMask(hasMask);
            ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv6DestinationAddressNoMask() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Dst.class);

            Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
            Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();

            ipv6DstBuilder.setIpv6Address(source.getIpv6DestinationAddressNoMask());
            Ipv6ArbitraryMask destinationArbitrarySubNetMask = source.getIpv6DestinationArbitraryBitmask();

            boolean hasMask = false;
            if (destinationArbitrarySubNetMask != null) {
                byte[] maskByteArray = IpConversionUtil.convertIpv6ArbitraryMaskToByteArray(destinationArbitrarySubNetMask);
                if (maskByteArray != null) {
                    ipv6DstBuilder.setMask(maskByteArray);
                    hasMask = true;
                }
            }
            matchEntryBuilder.setHasMask(hasMask);
            ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        return Optional.of(result);
    }
}
