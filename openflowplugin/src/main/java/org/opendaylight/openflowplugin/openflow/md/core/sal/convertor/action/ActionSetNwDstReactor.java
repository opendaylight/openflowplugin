/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.util.Map;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.InjectionResultTargetKey;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ResultInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;

/**
 *
 */
public class ActionSetNwDstReactor extends ConvertReactor<SetNwDstActionCase> {

    private static ActionSetNwDstReactor INSTANCE = new ActionSetNwDstReactor();

    private ActionSetNwDstReactor() {
        //NOOP
    }

    /**
     * @return singleton
     */
    public static ActionSetNwDstReactor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void initMappings(final Map<Short, Convertor<SetNwDstActionCase,?>> conversions,
            final Map<InjectionKey, ResultInjector<?,?>> injections) {
        ActionSetNwDstReactorMappingFactory.addSetNwDstConvertors(conversions);
        ActionSetNwDstReactorMappingFactory.addSetNwDstInjectors(injections);
    }

    @Override
    protected InjectionKey buildInjectionKey(final short version,
            final Object convertedItem, final Object target) {
        InjectionResultTargetKey key = null;
        if (convertedItem != null) {
             key = new InjectionResultTargetKey(version, target.getClass(), convertedItem.getClass());
        }
        return key;
    }

}
