/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flowflag;

import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

/**
 * add prepared convertors and injectors into given mappings
 * @see FlowFlagReactor
 */
public class FlowFlagReactorMappingFactory {
    
    /**
     * @param conversionMapping
     */
    public static void addFlowFlagsConvertors(Map<Short, Convertor<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags, ?>> conversionMapping) {
        conversionMapping.put(OFConstants.OFP_VERSION_1_3, new FlowFlagsConvertorImpl());
        conversionMapping.put(OFConstants.OFP_VERSION_1_0, new FlowFlagsConvertorV10Impl());
    }
    
    /**
     * @param injectionMapping 
     */
    public static void addFlowFlagsIjectors(Map<InjectionKey, ResultInjector<?, ?>> injectionMapping) {
        // OF-1.3|FlowModFlags --> FlowModInputBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_3, FlowModInputBuilder.class.getName()), 
                new ResultInjector<FlowModFlags, FlowModInputBuilder>() {
            @Override
            public void inject(FlowModFlags value,
                    FlowModInputBuilder target) {
                target.setFlags(value);
            }
        });
        
        // OF-1.3|FlowModFlagsV10 --> FlowModInputBuilder
        injectionMapping.put(new InjectionKey(OFConstants.OFP_VERSION_1_0, FlowModInputBuilder.class.getName()), 
                new ResultInjector<FlowModFlagsV10, FlowModInputBuilder>() {
            @Override
            public void inject(FlowModFlagsV10 value,
                    FlowModInputBuilder target) {
                target.setFlagsV10(value);
            }
        });
        
    }

}
