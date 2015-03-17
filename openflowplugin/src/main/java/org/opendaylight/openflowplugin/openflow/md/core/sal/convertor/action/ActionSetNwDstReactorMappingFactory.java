/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionResultTargetKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.IpAddressAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.IpAddressActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmFieldsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmFieldsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * add prepared convertors and injectors into given mappings
 *
 * @see ActionSetNwSrcReactor
 */
public class ActionSetNwDstReactorMappingFactory {

    /**
     * @param conversionMapping
     */
    public static void addSetNwDstConvertors(final Map<Short, Convertor<SetNwDstActionCase, ?>> conversionMapping) {
        conversionMapping.put(OFConstants.OFP_VERSION_1_0, new ActionSetNwDstConvertorV10Impl());
        conversionMapping.put(OFConstants.OFP_VERSION_1_3, new ActionSetNwDstConvertorImpl());
    }

    /**
     * @param injectionMapping
     */
    public static void addSetNwDstInjectors(final Map<InjectionKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.0| Ipv4Address -> ActionBuilder; SetNwSrc
        injectionMapping.put(new InjectionResultTargetKey(OFConstants.OFP_VERSION_1_0,
                        ActionBuilder.class, Ipv4Address.class),
                new ResultInjector<Ipv4Address, ActionBuilder>() {
                    @Override
                    public void inject(final Ipv4Address result, final ActionBuilder target) {
                        IpAddressActionBuilder ipvaddress = new IpAddressActionBuilder();
                        ipvaddress.setIpAddress(result);
                        target.setType(SetNwDst.class);
                        target.addAugmentation(IpAddressAction.class, ipvaddress.build());
                    }
                });

        // OF-1.3| Ipv4Address -> ActionBuilder; SetNwSrc
        injectionMapping.put(new InjectionResultTargetKey(OFConstants.OFP_VERSION_1_3,
                        ActionBuilder.class, Ipv4Address.class),
                new ResultInjector<Ipv4Address, ActionBuilder>() {
                    @Override
                    public void inject(final Ipv4Address result, final ActionBuilder target) {
                        OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
                        target.setType(SetField.class);
                        List<MatchEntry> matchEntriesList = new ArrayList<>();
                        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                        matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);

                        Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
                        Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();
                        ipv4DstBuilder.setIpv4Address(result);
                        ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());

                        matchEntryBuilder.setHasMask(false);
                        matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
                        matchEntriesList.add(matchEntryBuilder.build());
                        oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
                        target.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
                    }
                });

        // OF-1.3| Ipv6Address -> ActionBuilder; SetNwSrc
        injectionMapping.put(new InjectionResultTargetKey(OFConstants.OFP_VERSION_1_3,
                        ActionBuilder.class, Ipv6Address.class),
                new ResultInjector<Ipv6Address, ActionBuilder>() {
                    @Override
                    public void inject(final Ipv6Address result, final ActionBuilder target) {
                        OxmFieldsActionBuilder oxmFieldsActionBuilder = new OxmFieldsActionBuilder();
                        target.setType(SetField.class);
                        List<MatchEntry> matchEntriesList = new ArrayList<>();
                        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                        matchEntryBuilder.setOxmMatchField(Ipv6Dst.class);

                        Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
                        Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
                        ipv6DstBuilder.setIpv6Address(result);
                        ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());

                        matchEntryBuilder.setHasMask(false);
                        matchEntryBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
                        matchEntriesList.add(matchEntryBuilder.build());
                        oxmFieldsActionBuilder.setMatchEntry(matchEntriesList);
                        target.addAugmentation(OxmFieldsAction.class, oxmFieldsActionBuilder.build());
                    }
                });
    }
}
