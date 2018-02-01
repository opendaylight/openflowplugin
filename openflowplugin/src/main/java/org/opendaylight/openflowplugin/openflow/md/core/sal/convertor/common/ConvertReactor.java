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
 * Base class for a conversion reactor.
 *
 * @param <F> source type for conversion
 */
public abstract class ConvertReactor<F> {
    private final Map<ConvertorKey, ResultInjector<?, ?>> injectionMapping;
    private final Map<Short, ConvertReactorConvertor<F, ?>> conversionMapping;

    protected ConvertReactor() {
        final Map<Short, ConvertReactorConvertor<F, ?>> conversions = new HashMap<>();
        final Map<ConvertorKey, ResultInjector<?, ?>> injections = new HashMap<>();
        initMappings(conversions, injections);

        // Create optimized view of mappings
        this.conversionMapping = ImmutableMap.copyOf(conversions);
        this.injectionMapping = ImmutableMap.copyOf(injections);
    }

    /**
     * Fill conversion and injection mappings.
     *
     * @param conversions convert from
     * @param injections injection
     */
    protected abstract void initMappings(Map<Short, ConvertReactorConvertor<F, ?>> conversions,
            Map<ConvertorKey, ResultInjector<?, ?>> injections);

    /**
     * Converts a source to a target.
     *
     * @param <R> result
     * @param <T> target
     * @param source convert from
     * @param version openflow version
     * @param target convert to
     * @param convertorExecutor the convertor executor
     */
    @SuppressWarnings("unchecked")
    public <R, T> void convert(final F source, final short version, final T target,
            final ConvertorExecutor convertorExecutor) {

        //lookup converter
        ConvertReactorConvertor<F, R> convertor = (ConvertReactorConvertor<F, R>) conversionMapping.get(version);
        if (convertor == null) {
            throw new IllegalArgumentException("convertor for given version [" + version + "] not found");
        }
        R convertedItem = convertor.convert(source, convertorExecutor);

        //lookup injection
        ConvertorKey key = buildInjectionKey(version, convertedItem, target);
        ResultInjector<R, T> injection = (ResultInjector<R, T>) injectionMapping.get(key);
        if (injection == null) {
            throw new IllegalArgumentException("injector for given version and target [" + key + "] not found");
        }
        injection.inject(convertedItem, target);
    }

    /**
     * Builds an injection key.
     *
     * @param version openflow version
     * @param convertedItem to be injected
     * @param target object
     * @return injection key
     */
    protected ConvertorKey buildInjectionKey(final short version, final Object convertedItem, final Object target) {
        return new ConvertorKey(version, target.getClass());
    }

}
