/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;

/**
 * @param <FROM> source type for conversion
 *
 */
public abstract class ConvertReactor<FROM> {
    private final Map<ConvertorKey, ResultInjector<?, ?>> injectionMapping;
    private final Map<Short, ConvertReactorConvertor<FROM, ?>> conversionMapping;

    protected ConvertReactor() {
        final Map<Short, ConvertReactorConvertor<FROM, ?>> conversions = new HashMap<>();
        final Map<ConvertorKey, ResultInjector<?, ?>> injections = new HashMap<>();
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
    protected abstract void initMappings(Map<Short, ConvertReactorConvertor<FROM, ?>> conversions,
            Map<ConvertorKey, ResultInjector<?, ?>> injections);

    /**
     * @param <RESULT> result
     * @param <TARGET> target
     * @param source convert from
     * @param version openflow version
     * @param target convert to
     * @param convertorExecutor
     */
    @SuppressWarnings("unchecked")
    public <RESULT, TARGET> void convert(final FROM source, final short version, final TARGET target, final ConvertorExecutor convertorExecutor) {

        //lookup converter
        ConvertReactorConvertor<FROM, RESULT> convertor = (ConvertReactorConvertor<FROM, RESULT>) conversionMapping.get(version);
        if (convertor == null) {
            throw new IllegalArgumentException("convertor for given version ["+version+"] not found");
        }
        RESULT convertedItem = convertor.convert(source, convertorExecutor);

        //lookup injection
        ConvertorKey key = buildInjectionKey(version, convertedItem, target);
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
    protected ConvertorKey buildInjectionKey(final short version, final Object convertedItem, final Object target) {
        return new ConvertorKey(version, target.getClass());
    }

}
