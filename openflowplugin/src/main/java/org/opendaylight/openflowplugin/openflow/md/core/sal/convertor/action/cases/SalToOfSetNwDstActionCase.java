/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.cases;

import com.google.common.base.Splitter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;

public class SalToOfSetNwDstActionCase extends ConvertorCase<SetNwDstActionCase, Action, ActionConvertorData> {
    private static final Splitter PREFIX_SPLITTER = Splitter.on('/');

    public SalToOfSetNwDstActionCase() {
        super(SetNwDstActionCase.class, true, OFConstants.OFP_VERSION_1_3);
    }

    @Nonnull
    @Override
    public Optional<Action> process(@Nonnull final SetNwDstActionCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        final ActionBuilder builder = new ActionBuilder();
        final Address address = source.getSetNwDstAction().getAddress();

        if (address instanceof Ipv4) {
            Iterable<String> addressParts = PREFIX_SPLITTER.split(((Ipv4) address).getIpv4Address().getValue());
            Ipv4Address result = new Ipv4Address(addressParts.iterator().next());
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);

            Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
            Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();
            ipv4DstBuilder.setIpv4Address(result);
            Integer prefix = IpConversionUtil.extractPrefix(result);
            ipv4DstBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
            ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());

            matchEntryBuilder.setHasMask(false);
            matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
            matchEntriesList.add(matchEntryBuilder.build());

            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldActionBuilder = new SetFieldActionBuilder();
            setFieldActionBuilder.setMatchEntry(matchEntriesList);
            setFieldCaseBuilder.setSetFieldAction(setFieldActionBuilder.build());
            builder.setActionChoice(setFieldCaseBuilder.build());
        } else if (address instanceof Ipv6) {
            Iterable<String> addressParts = PREFIX_SPLITTER.split(((Ipv6) address).getIpv6Address().getValue());
            Ipv6Address result = new Ipv6Address(addressParts.iterator().next());
            List<MatchEntry> matchEntriesList = new ArrayList<>();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(Ipv6Dst.class);

            Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
            Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
            ipv6DstBuilder.setIpv6Address(result);
            Integer prefix = IpConversionUtil.extractPrefix(result);
            ipv6DstBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
            ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());

            matchEntryBuilder.setHasMask(false);
            matchEntryBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
            matchEntriesList.add(matchEntryBuilder.build());

            SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
            SetFieldActionBuilder setFieldActionBuilder = new SetFieldActionBuilder();
            setFieldActionBuilder.setMatchEntry(matchEntriesList);
            setFieldCaseBuilder.setSetFieldAction(setFieldActionBuilder.build());
            builder.setActionChoice(setFieldCaseBuilder.build());
        } else {
            throw new IllegalArgumentException("Address is not supported: " + address.getClass().getName());
        }

        return Optional.of(builder.build());
    }
}
