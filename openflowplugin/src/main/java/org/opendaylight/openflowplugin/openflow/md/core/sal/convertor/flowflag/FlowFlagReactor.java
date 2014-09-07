/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flowflag;

import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertReactor;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;

/**
 *
 */
public class FlowFlagReactor extends ConvertReactor<FlowModFlags> {

    private static FlowFlagReactor INSTANCE = new FlowFlagReactor();

    private FlowFlagReactor() {
        //NOOP
    }

    /**
     * @return singleton
     */
    public static FlowFlagReactor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void initMappings(final Map<Short, Convertor<FlowModFlags, ?>> conversions,
            final Map<InjectionKey, ResultInjector<?,?>> injections) {
        FlowFlagReactorMappingFactory.addFlowFlagsConvertors(conversions);
        FlowFlagReactorMappingFactory.addFlowFlagsIjectors(injections);
    }

}
