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
    
    private static ActionSetNwDstReactor instance;
    
    private ActionSetNwDstReactor() {
        //NOOP
    }
    
    /**
     * @return singleton
     */
    public static synchronized ActionSetNwDstReactor getInstance() {
        if (instance == null) {
            instance = new ActionSetNwDstReactor();
        }
        return instance;
    }
    
    @Override
    protected void initMappings(Map<Short, Convertor<SetNwDstActionCase,?>> conversions, 
            Map<InjectionKey, ResultInjector<?,?>> injections) {
        ActionSetNwDstReactorMappingFactory.addSetNwDstConvertors(conversions);
        ActionSetNwDstReactorMappingFactory.addSetNwDstInjectors(injections);
    }
    
    @Override
    protected InjectionKey buildInjectionKey(short version,
            Object convertedItem, Object target) {
        InjectionResultTargetKey key = null;
        if (convertedItem != null) {
             key = new InjectionResultTargetKey(version, target.getClass().getName(), convertedItem.getClass().getName());
        }
        return key;
    }

}
