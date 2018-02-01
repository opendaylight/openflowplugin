/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public final class MatchInjector {
    private MatchInjector() {
    }

    @SuppressWarnings("unchecked")
    public static <F, T> void inject(Optional<F> source, T target, short version) {
        F sourceResult;
        if (source.isPresent()) {
            sourceResult = source.get();
        } else if (version == EncodeConstants.OF10_VERSION_ID) {
            sourceResult = (F) MatchV10Convertor.defaultResult();
        } else {
            sourceResult = (F) MatchConvertor.defaultResult();
        }

        final Map<ConvertorKey, ResultInjector<?, ?>> injectorMap = new HashMap<>();
        addInjectors(injectorMap);

        final ResultInjector<F, T> injection = (ResultInjector<F, T>) injectorMap
                .get(new ConvertorKey(version, target.getClass()));

        injection.inject(sourceResult, target);
    }

    private static void addInjectors(final Map<ConvertorKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.3|List<MatchEntries> --> FlowModInputBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_3, FlowModInputBuilder.class),
            (ResultInjector<List<MatchEntry>, FlowModInputBuilder>)(value, target)
                -> target.setMatch(wrapMatchV13(value).build()));

        // OF-1.3|List<MatchEntries> --> OxmFieldsActionBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_3, SetFieldActionBuilder.class),
            (ResultInjector<List<MatchEntry>, SetFieldActionBuilder>)(value, target) -> target.setMatchEntry(value));

        // OF-1.0|MatchV10Builder --> FlowModInputBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_0, FlowModInputBuilder.class),
            (ResultInjector<MatchV10, FlowModInputBuilder>)(value, target) -> target.setMatchV10(value));

        // OF-1.3|List<MatchEntries> --> MultipartRequestFlowBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_3, MultipartRequestFlowBuilder.class),
            (ResultInjector<List<MatchEntry>, MultipartRequestFlowBuilder>)(value, target)
                -> target.setMatch(wrapMatchV13(value).build()));

        // OF-1.0|List<MatchEntries> --> MultipartRequestFlowBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_0, MultipartRequestFlowBuilder.class),
            (ResultInjector<MatchV10, MultipartRequestFlowBuilder>)(value, target) -> target.setMatchV10(value));

        // OF-1.3|List<MatchEntries> --> MultipartRequestAggregateBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_3, MultipartRequestAggregateBuilder.class),
            (ResultInjector<List<MatchEntry>, MultipartRequestAggregateBuilder>)(value, target)
                -> target.setMatch(wrapMatchV13(value).build()));

        // OF-1.0|List<MatchEntries> --> MultipartRequestAggregateBuilder
        injectionMapping.put(new ConvertorKey(OFConstants.OFP_VERSION_1_0, MultipartRequestAggregateBuilder.class),
            (ResultInjector<MatchV10, MultipartRequestAggregateBuilder>)(value, target) -> target.setMatchV10(value));
    }

    private static MatchBuilder wrapMatchV13(final List<MatchEntry> value) {
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
