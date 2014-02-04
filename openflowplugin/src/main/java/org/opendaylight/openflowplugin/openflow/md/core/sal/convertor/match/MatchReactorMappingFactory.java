/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.util.List;
import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

/**
 * add prepared convertors and injectors into given mappings
 * @see MatchReactor
 */
public class MatchReactorMappingFactory {
    
    /**
     * @param conversionMapping
     */
    public static void addMatchConvertors(Map<Short, Convertor<Match, ?>> conversionMapping) {
        conversionMapping.put(OFConstants.OFP_VERSION_1_3, new MatchConvertorImpl());
        conversionMapping.put(OFConstants.OFP_VERSION_1_0, new MatchConvertorV10Impl());
    }
    
    /**
     * @param injectionMapping 
     */
    public static void addMatchIjectors(Map<InjectionKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.3|List<MatchEntries> --> FlowModInputBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, FlowModInputBuilder.class.getName()), 
                new ResultInjector<List<MatchEntries>, FlowModInputBuilder>() {
            @Override
            public void inject(List<MatchEntries> value,
                    FlowModInputBuilder target) {
                target.setMatch(wrapMatchV13(value).build());
            }
        });
        
        // OF-1.3|List<MatchEntries> --> OxmFieldsActionBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, OxmFieldsActionBuilder.class.getName()), 
                new ResultInjector<List<MatchEntries>, OxmFieldsActionBuilder>() {
            @Override
            public void inject(List<MatchEntries> value,
                    OxmFieldsActionBuilder target) {
                target.setMatchEntries(value);
            }
        });
        
        // OF-1.0|MatchV10Builder --> FlowModInputBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, FlowModInputBuilder.class.getName()), 
                new ResultInjector<MatchV10, FlowModInputBuilder>() {
            @Override
            public void inject(MatchV10 value,
                    FlowModInputBuilder target) {
                target.setMatchV10(value);
            }
        });
        
        // OF-1.3|List<MatchEntries> --> MultipartRequestFlowBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, MultipartRequestFlowBuilder.class.getName()), 
                new ResultInjector<List<MatchEntries>, MultipartRequestFlowBuilder>() {
            @Override
            public void inject(List<MatchEntries> value,
                    MultipartRequestFlowBuilder target) {
                target.setMatch(wrapMatchV13(value).build());
            }
        });
        
        // OF-1.0|List<MatchEntries> --> MultipartRequestFlowBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, MultipartRequestFlowBuilder.class.getName()), 
                new ResultInjector<MatchV10, MultipartRequestFlowBuilder>() {
            @Override
            public void inject(MatchV10 value,
                    MultipartRequestFlowBuilder target) {
                target.setMatchV10(value);
            }
        });
        
        // OF-1.3|List<MatchEntries> --> MultipartRequestAggregateBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, MultipartRequestAggregateBuilder.class.getName()), 
                new ResultInjector<List<MatchEntries>, MultipartRequestAggregateBuilder>() {
            @Override
            public void inject(List<MatchEntries> value,
                    MultipartRequestAggregateBuilder target) {
                target.setMatch(wrapMatchV13(value).build());
            }
        });
        
        // OF-1.0|List<MatchEntries> --> MultipartRequestAggregateBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, MultipartRequestAggregateBuilder.class.getName()), 
                new ResultInjector<MatchV10, MultipartRequestAggregateBuilder>() {
            @Override
            public void inject(MatchV10 value,
                    MultipartRequestAggregateBuilder target) {
                target.setMatchV10(value);
            }
        });
    }
    
    /**
     * @param value pure match
     * @return wrapped match
     */
    public static MatchBuilder wrapMatchV13(List<MatchEntries> value) {
        MatchBuilder matchBuilder = new MatchBuilder(); 
        matchBuilder.setType(FlowConvertor.DEFAULT_MATCH_TYPE);
        if (value == null) {
            matchBuilder.setMatchEntries(FlowConvertor.DEFAULT_MATCH_ENTRIES);
        } else {
            matchBuilder.setMatchEntries(value);
        }
        return matchBuilder;
    }
}
