/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;

public class SalToOfIpv4MatchCase extends ConvertorCase<Ipv4Match, List<MatchEntry>, VersionConvertorData> {
    public SalToOfIpv4MatchCase() {
        super(Ipv4Match.class, true);
    }

    @Override
    public Optional<List<MatchEntry>> process(@Nonnull Ipv4Match source, VersionConvertorData data, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (source.getIpv4Source() != null) {
            Ipv4Prefix ipv4Prefix = source.getIpv4Source();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv4Src.class);

            Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
            Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();

            Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            ipv4SrcBuilder.setIpv4Address(ipv4Address);
            boolean hasMask = false;
            byte[] mask = MatchConvertorUtil.extractIpv4Mask(addressParts);

            if (null != mask) {
                ipv4SrcBuilder.setMask(mask);
                hasMask = true;
            }

            matchEntryBuilder.setHasMask(hasMask);
            ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv4Destination() != null) {
            Ipv4Prefix ipv4Prefix = source.getIpv4Destination();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);

            Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
            Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();

            Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            ipv4DstBuilder.setIpv4Address(ipv4Address);
            boolean hasMask = false;
            byte[] mask = MatchConvertorUtil.extractIpv4Mask(addressParts);

            if (null != mask) {
                ipv4DstBuilder.setMask(mask);
                hasMask = true;
            }

            matchEntryBuilder.setHasMask(hasMask);
            ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        return Optional.of(result);
    }
}
