/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import java.util.List;
import java.util.Map;

/**
 * add prepared convertors and injectors into given mappings
 *
 * @see MatchReactor
 */
public class MatchReactorMappingFactory {

    /**
     * @param conversionMapping conversion mapping
     */
    public static void addMatchConvertors(final Map<Short, Convertor<Match, ?>> conversionMapping) {
        conversionMapping.put(OFConstants.OFP_VERSION_1_3, new MatchConvertorImpl());
        conversionMapping.put(OFConstants.OFP_VERSION_1_0, new MatchConvertorV10Impl());
    }

    /**
     * @param injectionMapping injection mapping
     */
    public static void addMatchIjectors(final Map<InjectionKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.3|List<MatchEntries> --> FlowModInputBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, FlowModInputBuilder.class),
                new ResultInjector<List<MatchEntry>, FlowModInputBuilder>() {
                    @Override
                    public void inject(final List<MatchEntry> value,
                                       final FlowModInputBuilder target) {
                        target.setMatch(wrapMatchV13(value).build());
                    }
                });

        // OF-1.3|List<MatchEntries> --> OxmFieldsActionBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, SetFieldActionBuilder.class),
                new ResultInjector<List<MatchEntry>, SetFieldActionBuilder>() {
                    @Override
                    public void inject(final List<MatchEntry> value,
                                       final SetFieldActionBuilder target) {
                        target.setMatchEntry(value);
                    }
                });

        // OF-1.0|MatchV10Builder --> FlowModInputBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, FlowModInputBuilder.class),
                new ResultInjector<MatchV10, FlowModInputBuilder>() {
                    @Override
                    public void inject(final MatchV10 value,
                                       final FlowModInputBuilder target) {
                        target.setMatchV10(value);
                    }
                });

        // OF-1.3|List<MatchEntries> --> MultipartRequestFlowBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, MultipartRequestFlowBuilder.class),
                new ResultInjector<List<MatchEntry>, MultipartRequestFlowBuilder>() {
                    @Override
                    public void inject(final List<MatchEntry> value,
                                       final MultipartRequestFlowBuilder target) {
                        target.setMatch(wrapMatchV13(value).build());
                    }
                });

        // OF-1.0|List<MatchEntries> --> MultipartRequestFlowBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, MultipartRequestFlowBuilder.class),
                new ResultInjector<MatchV10, MultipartRequestFlowBuilder>() {
                    @Override
                    public void inject(final MatchV10 value,
                                       final MultipartRequestFlowBuilder target) {
                        target.setMatchV10(value);
                    }
                });

        // OF-1.3|List<MatchEntries> --> MultipartRequestAggregateBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, MultipartRequestAggregateBuilder.class),
                new ResultInjector<List<MatchEntry>, MultipartRequestAggregateBuilder>() {
                    @Override
                    public void inject(final List<MatchEntry> value,
                                       final MultipartRequestAggregateBuilder target) {
                        target.setMatch(wrapMatchV13(value).build());
                    }
                });

        // OF-1.0|List<MatchEntries> --> MultipartRequestAggregateBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, MultipartRequestAggregateBuilder.class),
                new ResultInjector<MatchV10, MultipartRequestAggregateBuilder>() {
                    @Override
                    public void inject(final MatchV10 value,
                                       final MultipartRequestAggregateBuilder target) {
                        target.setMatchV10(value);
                    }
                });
    }

    /**
     * @param value pure match
     * @return wrapped match
     */
    public static MatchBuilder wrapMatchV13(final List<MatchEntry> value) {
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setType(FlowConvertor.DEFAULT_MATCH_TYPE);
        if (value == null) {
            matchBuilder.setMatchEntry(FlowConvertor.DEFAULT_MATCH_ENTRIES);
        } else {
            matchBuilder.setMatchEntry(value);
        }
        return matchBuilder;
    }
}
