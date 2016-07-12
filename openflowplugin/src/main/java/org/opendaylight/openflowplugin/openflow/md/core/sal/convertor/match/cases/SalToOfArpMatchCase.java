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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.op._case.ArpOpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.sha._case.ArpShaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.spa._case.ArpSpaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tha._case.ArpThaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tpa._case.ArpTpaBuilder;

public class SalToOfArpMatchCase extends ConvertorCase<ArpMatch, List<MatchEntry>, VersionConvertorData> {
    public SalToOfArpMatchCase() {
        super(ArpMatch.class, true);
    }

    @Override
    public Optional<List<MatchEntry>> process(@Nonnull ArpMatch source, VersionConvertorData data, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (source.getArpOp() != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setHasMask(false);
            matchEntryBuilder.setOxmMatchField(ArpOp.class);
            ArpOpCaseBuilder arpOpCaseBuilder = new ArpOpCaseBuilder();
            ArpOpBuilder arpOpBuilder = new ArpOpBuilder();
            arpOpBuilder.setOpCode(source.getArpOp());
            arpOpCaseBuilder.setArpOp(arpOpBuilder.build());
            matchEntryBuilder.setMatchEntryValue(arpOpCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getArpSourceTransportAddress() != null) {
            Ipv4Prefix ipv4Prefix = source.getArpSourceTransportAddress();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(ArpSpa.class);

            ArpSpaCaseBuilder arpSpaCaseBuilder = new ArpSpaCaseBuilder();
            ArpSpaBuilder arpSpaBuilder = new ArpSpaBuilder();

            Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            arpSpaBuilder.setIpv4Address(ipv4Address);
            boolean hasMask = false;
            byte[] mask = MatchConvertorUtil.extractIpv4Mask(addressParts);
            if (null != mask) {
                arpSpaBuilder.setMask(mask);
                hasMask = true;
            }
            matchEntryBuilder.setHasMask(hasMask);
            arpSpaCaseBuilder.setArpSpa(arpSpaBuilder.build());
            matchEntryBuilder.setMatchEntryValue(arpSpaCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        if (source.getArpTargetTransportAddress() != null) {
            Ipv4Prefix ipv4Prefix = source.getArpTargetTransportAddress();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(ArpTpa.class);

            ArpTpaCaseBuilder arpTpaCaseBuilder = new ArpTpaCaseBuilder();
            ArpTpaBuilder arpTpaBuilder = new ArpTpaBuilder();

            Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
            Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
            arpTpaBuilder.setIpv4Address(ipv4Address);
            boolean hasMask = false;
            byte[] mask = MatchConvertorUtil.extractIpv4Mask(addressParts);
            if (null != mask) {
                arpTpaBuilder.setMask(mask);
                hasMask = true;
            }
            matchEntryBuilder.setHasMask(hasMask);
            arpTpaCaseBuilder.setArpTpa(arpTpaBuilder.build());
            matchEntryBuilder.setMatchEntryValue(arpTpaCaseBuilder.build());
            result.add(matchEntryBuilder.build());
        }

        ArpSourceHardwareAddress arpSourceHardwareAddress = source.getArpSourceHardwareAddress();
        if (arpSourceHardwareAddress != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(ArpSha.class);

            ArpShaCaseBuilder arpShaCaseBuilder = new ArpShaCaseBuilder();
            ArpShaBuilder arpShaBuilder = new ArpShaBuilder();
            arpShaBuilder.setMacAddress(arpSourceHardwareAddress.getAddress());
            boolean hasMask = false;
            if (null != arpSourceHardwareAddress.getMask()) {
                arpShaBuilder.setMask(ByteBufUtils.macAddressToBytes(arpSourceHardwareAddress.getMask().getValue()));
                hasMask = true;
            }
            arpShaCaseBuilder.setArpSha(arpShaBuilder.build());
            matchEntryBuilder.setMatchEntryValue(arpShaCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            result.add(matchEntryBuilder.build());
        }

        ArpTargetHardwareAddress arpTargetHardwareAddress = source.getArpTargetHardwareAddress();
        if (arpTargetHardwareAddress != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(ArpTha.class);

            ArpThaCaseBuilder arpThaCaseBuilder = new ArpThaCaseBuilder();
            ArpThaBuilder arpThaBuilder = new ArpThaBuilder();
            arpThaBuilder.setMacAddress(arpTargetHardwareAddress.getAddress());
            boolean hasMask = false;
            if (null != arpTargetHardwareAddress.getMask()) {
                arpThaBuilder.setMask(ByteBufUtils.macAddressToBytes(arpTargetHardwareAddress.getMask().getValue()));
                hasMask = true;
            }
            arpThaCaseBuilder.setArpTha(arpThaBuilder.build());
            matchEntryBuilder.setMatchEntryValue(arpThaCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            result.add(matchEntryBuilder.build());
        }

        return Optional.of(result);
    }
}
