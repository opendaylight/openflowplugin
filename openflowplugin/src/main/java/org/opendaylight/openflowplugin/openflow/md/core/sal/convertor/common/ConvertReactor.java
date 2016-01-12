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

import com.google.common.collect.ImmutableMap;

/**
 * @param <FROM> source type for conversion
 *
 */
public abstract class ConvertReactor<FROM> {
    private final Map<InjectionKey, ResultInjector<?, ?>> injectionMapping;
    private final Map<Short, Convertor<FROM, ?>> conversionMapping;

    protected ConvertReactor() {
        final Map<Short, Convertor<FROM, ?>> conversions = new HashMap<>();
        final Map<InjectionKey, ResultInjector<?, ?>> injections = new HashMap<>();
        initMappings(conversions, injections);

        // Create optimized view of mappings
        this.conversionMapping = ImmutableMap.copyOf(conversions);
        this.injectionMapping = ImmutableMap.copyOf(injections);
    }

    /**
     * fill conversion and injection mappings
     * @param conversions convert from
     * @param injections injection
     */
    protected abstract void initMappings(Map<Short, Convertor<FROM, ?>> conversions,
            Map<InjectionKey, ResultInjector<?, ?>> injections);

    /**
     * @param source convert from
     * @param version openflow version
     * @param target convert to
     * @param datapathid datapath id
     * @param <RESULT> result
     * @param <TARGET> target
     */
    @SuppressWarnings("unchecked")
    public <RESULT, TARGET> void convert(final FROM source, final short version, final TARGET target, final BigInteger datapathid) {

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
     * @param version openflow version
     * @param convertedItem to be injected
     * @param target object
     * @return injection key
     */
    protected InjectionKey buildInjectionKey(final short version, final Object convertedItem, final Object target) {
        return new InjectionKey(version, target.getClass());
    }

}
