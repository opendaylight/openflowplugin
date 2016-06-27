/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes source and return result based on convertor cases added to this processor.
 *
 * @param <FROM> the source type
 * @param <TO>   the result type
 * @param <DATA> the type of convertor data
 */
public class ConvertorProcessor<FROM, TO, DATA extends ConvertorData> {
    private static final short OFP_VERSION_ALL = 0x00;
    private static final Logger LOG = LoggerFactory.getLogger(ConvertorProcessor.class);

    private final Map<InjectionKey, ConvertorCase<?, TO, DATA>> conversions = new HashMap<>();
    private ConvertorCase<?, TO, DATA> defaultCase;

    /**
     * Add convertor processor case.
     *
     * @param processorCase the processor case
     * @return the convertor processor
     */
    public ConvertorProcessor<FROM, TO, DATA> addCase(final ConvertorCase<?, TO, DATA> processorCase) {
        if (processorCase.getSupportedVersions().isEmpty()) {
            final InjectionKey key = new InjectionKey(OFP_VERSION_ALL, processorCase.getType());
            conversions.putIfAbsent(key, processorCase);
        } else {
            for (short supportedVersion : processorCase.getSupportedVersions()) {
                final InjectionKey key = new InjectionKey(supportedVersion, processorCase.getType());
                conversions.putIfAbsent(key, processorCase);
            }
        }

        return this;
    }

    /**
     * Process source and return result based on convertor cases, or empty if no match is found.
     *
     * @param source the source
     * @return the optional
     */
    public Optional<TO> process(final FROM source) {
        return process(source, null);
    }

    /**
     * Process source and return result based on convertor cases, or empty if no match is found.
     *
     * @param source the source
     * @param data   the data
     * @return the optional
     */
    public Optional<TO> process(final FROM source, final DATA data) {
        Optional<TO> result = Optional.empty();
        final short version = data != null ? data.getVersion() : OFP_VERSION_ALL;

        if (source == null) {
            LOG.trace("Failed to convert null for version {}", version);
            return result;
        }

        Class<?> clazz = source.getClass();
        final Class<?>[] interfaces = clazz.getInterfaces();

        if (interfaces.length > 0) {
            clazz = interfaces[0];
        }

        final InjectionKey key = new InjectionKey(version, clazz);
        ConvertorCase<?, TO, DATA> rule = defaultCase;

        if (conversions.containsKey(key)) {
            rule = conversions.get(key);
        }

        if (rule != null) {
            result = rule.processRaw(source, data);

            if (rule.isErrorOnEmpty() && !result.isPresent()) {
                LOG.error("Failed to convert {} for version {}", clazz, version);
            }
        } else {
            LOG.trace("Failed to convert {} for version {}", clazz, version);
        }

        return result;
    }

    /**
     * Sets default case, what will be used when we do not find any matching convertor case for source.
     *
     * @param defaultCase the default case
     * @return the default case
     */
    public ConvertorProcessor<FROM, TO, DATA> setDefaultCase(final ConvertorCase<?, TO, DATA> defaultCase) {
        this.defaultCase = defaultCase;
        return this;
    }
}
