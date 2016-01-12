/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionResultTargetKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.src._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;

/**
 * add prepared convertors and injectors into given mappings
 *
 * @see ActionSetNwSrcReactor
 */
public class ActionSetNwSrcReactorMappingFactory {

    /**
     * @param conversionMapping conversion mapping
     */
    public static void addSetNwSrcConvertors(final Map<Short, Convertor<SetNwSrcActionCase, ?>> conversionMapping) {
        conversionMapping.put(OFConstants.OFP_VERSION_1_0, new ActionSetNwSrcConvertorV10Impl());
        conversionMapping.put(OFConstants.OFP_VERSION_1_3, new ActionSetNwSrcConvertorImpl());
    }

    /**
     * @param injectionMapping injection mapping
     */
    public static void addSetNwSrcInjectors(final Map<InjectionKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.0| Ipv4Address -> ActionBuilder; SetNwSrc
        injectionMapping.put(new InjectionResultTargetKey(OFConstants.OFP_VERSION_1_0,
                        ActionBuilder.class, Ipv4Address.class),
                new ResultInjector<Ipv4Address, ActionBuilder>() {
                    @Override
                    public void inject(final Ipv4Address result, final ActionBuilder target) {
                        SetNwSrcCaseBuilder nwSrcCaseBuilder = new SetNwSrcCaseBuilder();
                        SetNwSrcActionBuilder nwSrcBuilder = new SetNwSrcActionBuilder();
                        nwSrcBuilder.setIpAddress(new Ipv4Address(result));
                        nwSrcCaseBuilder.setSetNwSrcAction(nwSrcBuilder.build());
                        target.setActionChoice(nwSrcCaseBuilder.build());
                    }
                });

        // OF-1.3| Ipv4Address -> ActionBuilder; SetNwSrc
        injectionMapping.put(new InjectionResultTargetKey(OFConstants.OFP_VERSION_1_3,
                        ActionBuilder.class, Ipv4Address.class),
                new ResultInjector<Ipv4Address, ActionBuilder>() {
                    @Override
                    public void inject(final Ipv4Address result, final ActionBuilder target) {
                        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
                        SetFieldActionBuilder setFieldBuilder = new SetFieldActionBuilder();
                        List<MatchEntry> entries = new ArrayList<>();
                        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                        matchEntryBuilder.setOxmMatchField(Ipv4Src.class);

                        Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
                        Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();
                        ipv4SrcBuilder.setIpv4Address(result);
                        Integer prefix = IpConversionUtil.extractPrefix(result);
                        if (prefix != null) {
                            ipv4SrcBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
                        }
                        ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());

                        matchEntryBuilder.setHasMask(false);
                        matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
                        entries.add(matchEntryBuilder.build());
                        setFieldBuilder.setMatchEntry(entries);
                        setFieldCaseBuilder.setSetFieldAction(setFieldBuilder.build());
                        target.setActionChoice(setFieldCaseBuilder.build());
                    }
                });

        // OF-1.3| Ipv6Address -> ActionBuilder; SetNwSrc
        injectionMapping.put(new InjectionResultTargetKey(OFConstants.OFP_VERSION_1_3,
                        ActionBuilder.class, Ipv6Address.class),
                new ResultInjector<Ipv6Address, ActionBuilder>() {
                    @Override
                    public void inject(final Ipv6Address result, final ActionBuilder target) {
                        List<MatchEntry> matchEntriesList = new ArrayList<>();
                        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                        matchEntryBuilder.setOxmMatchField(Ipv6Src.class);

                        Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
                        Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
                        ipv6SrcBuilder.setIpv6Address(result);
                        Integer prefix = IpConversionUtil.extractPrefix(result);
                        if (prefix != null) {
                            ipv6SrcBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
                        }
                        ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());

                        matchEntryBuilder.setHasMask(false);
                        matchEntryBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
                        matchEntriesList.add(matchEntryBuilder.build());

                        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
                        SetFieldActionBuilder setFieldActionBuilder = new SetFieldActionBuilder();
                        setFieldActionBuilder.setMatchEntry(matchEntriesList);
                        setFieldCaseBuilder.setSetFieldAction(setFieldActionBuilder.build());
                        target.setActionChoice(setFieldCaseBuilder.build());

                    }
                });
    }

    private static byte[] extractIpv4Mask(boolean hasMask, final Iterator<String> addressParts) {
        final int prefix;
        if (addressParts.hasNext()) {
            int potentionalPrefix = Integer.parseInt(addressParts.next());
            prefix = potentionalPrefix < 32 ? potentionalPrefix : 0;
        } else {
            prefix = 0;
        }

        if (prefix != 0) {
            int mask = 0xffffffff << (32 - prefix);
            byte[] maskBytes = new byte[]{(byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8),
                    (byte) mask};
            hasMask = true;
            return maskBytes;
        }
        return null;
    }
}
