/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <FROM> source type for conversion
 * 
 */
public abstract class ConvertReactor<FROM> {
    
    private Map<Short, Convertor<FROM, ?>> conversionMapping;
    private Map<InjectionKey, ResultInjector<?, ?>> injectionMapping;
    
    protected ConvertReactor() {
        conversionMapping = new HashMap<>();
        injectionMapping = new HashMap<>();
        initMappings(conversionMapping, injectionMapping);
    }
    
    /**
     * fill conversion and injection mappings
     * @param conversions 
     * @param injections 
     */
    protected abstract void initMappings(Map<Short, Convertor<FROM, ?>> conversions, 
            Map<InjectionKey, ResultInjector<?, ?>> injections);
    
    /**
     * @param source
     * @param version
     * @param target 
     * @param datapathid 
     */
    @SuppressWarnings("unchecked")
    public <RESULT, TARGET> void convert(FROM source, short version, TARGET target, BigInteger datapathid) {
        
        //lookup converter
        Convertor<FROM, RESULT> convertor = (Convertor<FROM, RESULT>) conversionMapping.get(version);
        if (convertor == null) {
            throw new IllegalArgumentException("convertor for given version ["+version+"] not found");
        }
        RESULT convertedItem = convertor.convert(source,datapathid);
        
        //lookup injection
        InjectionKey key = buildInjectionKey(version, convertedItem, target);
        ResultInjector<RESULT, TARGET> injection = (ResultInjector<RESULT, TARGET>) injectionMapping.get(key);
        if (injection == null) {
            throw new IllegalArgumentException("injector for given version and target ["+key+"] not found");
        }
        injection.inject(convertedItem, target);
    }

    /**
     * @param version
     * @param convertedItem to be injected 
     * @param target object
     * @return
     */
    protected InjectionKey buildInjectionKey(short version, Object convertedItem, Object target) {
        return new InjectionKey(version, target.getClass().getName());
    }

}
