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
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.exthdr._case.Ipv6ExthdrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.flabel._case.Ipv6FlabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.sll._case.Ipv6NdSllBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.target._case.Ipv6NdTargetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.tll._case.Ipv6NdTllBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;

public class SalToOfIpv6MatchCase extends ConvertorCase<Ipv6Match, List<MatchEntry>, VersionConvertorData> {
    public SalToOfIpv6MatchCase() {
        super(Ipv6Match.class, true);
    }

    @Override
    public Optional<List<MatchEntry>> process(@Nonnull Ipv6Match source, VersionConvertorData data, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (source.getIpv6Source() != null) {
            Ipv6Prefix ipv6Prefix = source.getIpv6Source();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Src.class);

            Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
            Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
            final Integer prefix = IpConversionUtil.extractIpv6Prefix(ipv6Prefix);
            boolean hasMask = false;
            if (null != prefix) {
                ipv6SrcBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
                hasMask = true;
            }
            ipv6SrcBuilder.setIpv6Address(IpConversionUtil.extractIpv6Address(ipv6Prefix));
            ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            matchEntryBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv6Destination() != null) {
            Ipv6Prefix ipv6Prefix = source.getIpv6Destination();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Dst.class);

            Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
            Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
            final Integer prefix = IpConversionUtil.extractIpv6Prefix(ipv6Prefix);
            boolean hasMask = false;
            if (null != prefix) {
                ipv6DstBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
                hasMask = true;
            }
            ipv6DstBuilder.setIpv6Address(IpConversionUtil.extractIpv6Address(ipv6Prefix));
            ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            matchEntryBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv6Label() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            boolean hasmask = false;
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Flabel.class);
            Ipv6FlabelCaseBuilder ipv6FlabelCaseBuilder = new Ipv6FlabelCaseBuilder();
            Ipv6FlabelBuilder ipv6FlabelBuilder = new Ipv6FlabelBuilder();
            ipv6FlabelBuilder.setIpv6Flabel(source.getIpv6Label().getIpv6Flabel());

            if (source.getIpv6Label().getFlabelMask() != null) {
                hasmask = true;
                ipv6FlabelBuilder.setMask(ByteUtil.unsignedIntToBytes(source.getIpv6Label().getFlabelMask().getValue()));
            }

            ipv6FlabelCaseBuilder.setIpv6Flabel(ipv6FlabelBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6FlabelCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasmask);
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv6NdTarget() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setHasMask(false);
            matchEntryBuilder.setOxmMatchField(Ipv6NdTarget.class);

            Ipv6NdTargetCaseBuilder ipv6NdTargetCaseBuilder = new Ipv6NdTargetCaseBuilder();
            Ipv6NdTargetBuilder ipv6NdTargetBuilder = new Ipv6NdTargetBuilder();
            ipv6NdTargetBuilder.setIpv6Address(source.getIpv6NdTarget());
            ipv6NdTargetCaseBuilder.setIpv6NdTarget(ipv6NdTargetBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6NdTargetCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv6NdSll() != null) {
            MacAddress ipv6NdSll = source.getIpv6NdSll();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6NdSll.class);

            Ipv6NdSllCaseBuilder ipv6NdSllCaseBuilder = new Ipv6NdSllCaseBuilder();
            Ipv6NdSllBuilder ipv6NdSllBuilder = new Ipv6NdSllBuilder();
            ipv6NdSllBuilder.setMacAddress(ipv6NdSll);
            ipv6NdSllCaseBuilder.setIpv6NdSll(ipv6NdSllBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6NdSllCaseBuilder.build());
            matchEntryBuilder.setHasMask(false);
            result.add(matchEntryBuilder.build());
        }

        if (source.getIpv6NdTll() != null) {
            MacAddress ipv6NdSll = source.getIpv6NdTll();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6NdTll.class);

            Ipv6NdTllCaseBuilder ipv6NdTllCaseBuilder = new Ipv6NdTllCaseBuilder();
            Ipv6NdTllBuilder ipv6NdTllBuilder = new Ipv6NdTllBuilder();
            ipv6NdTllBuilder.setMacAddress(ipv6NdSll);
            ipv6NdTllCaseBuilder.setIpv6NdTll(ipv6NdTllBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6NdTllCaseBuilder.build());
            matchEntryBuilder.setHasMask(false);
            result.add(matchEntryBuilder.build());

        }

        if (source.getIpv6ExtHeader() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            boolean hasmask = false;
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Exthdr.class);
            Ipv6ExthdrCaseBuilder ipv6ExthdrCaseBuilder = new Ipv6ExthdrCaseBuilder();
            Ipv6ExthdrBuilder ipv6ExthdrBuilder = new Ipv6ExthdrBuilder();

            Integer bitmap = source.getIpv6ExtHeader().getIpv6Exthdr();
            final Boolean NONEXT = ((bitmap) & (1)) != 0;
            final Boolean ESP = ((bitmap) & (1 << 1)) != 0;
            final Boolean AUTH = ((bitmap) & (1 << 2)) != 0;
            final Boolean DEST = ((bitmap) & (1 << 3)) != 0;
            final Boolean FRAG = ((bitmap) & (1 << 4)) != 0;
            final Boolean ROUTER = ((bitmap) & (1 << 5)) != 0;
            final Boolean HOP = ((bitmap) & (1 << 6)) != 0;
            final Boolean UNREP = ((bitmap) & (1 << 7)) != 0;
            final Boolean UNSEQ = ((bitmap) & (1 << 8)) != 0;

            ipv6ExthdrBuilder.setPseudoField(new Ipv6ExthdrFlags(AUTH, DEST, ESP, FRAG, HOP, NONEXT, ROUTER, UNREP, UNSEQ));

            if (source.getIpv6ExtHeader().getIpv6ExthdrMask() != null) {
                hasmask = true;
                ipv6ExthdrBuilder.setMask(ByteUtil.unsignedShortToBytes(source.getIpv6ExtHeader().getIpv6ExthdrMask()));
            }

            ipv6ExthdrCaseBuilder.setIpv6Exthdr(ipv6ExthdrBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ipv6ExthdrCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasmask);
            result.add(matchEntryBuilder.build());
        }

        return Optional.of(result);
    }
}
