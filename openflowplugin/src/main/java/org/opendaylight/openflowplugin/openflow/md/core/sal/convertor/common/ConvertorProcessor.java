/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes source and return result based on convertor cases added to this processor.
 *
 * @param <F> the source type
 * @param <T>   the result type
 * @param <D> the type of convertor data
 */
public class ConvertorProcessor<F extends DataContainer, T, D extends ConvertorData> {
    private static final short OFP_VERSION_ALL = 0x00;
    private static final Logger LOG = LoggerFactory.getLogger(ConvertorProcessor.class);

    private final Map<Short, Map<Class<?>, ConvertorCase<?, T, D>>> conversions = new ConcurrentHashMap<>();
    private ConvertorCase<?, T, D> defaultCase;

    /**
     * Add convertor processor case.
     *
     * @param processorCase the processor case
     * @return the convertor processor
     */
    public ConvertorProcessor<F, T, D> addCase(final ConvertorCase<?, T, D> processorCase) {
        if (processorCase.getSupportedVersions().isEmpty()) {
            getCasesForVersion(OFP_VERSION_ALL).putIfAbsent(processorCase.getType(), processorCase);
        } else {
            for (short supportedVersion : processorCase.getSupportedVersions()) {
                getCasesForVersion(supportedVersion).putIfAbsent(processorCase.getType(), processorCase);
            }
        }

        return this;
    }

    /**
     * Process source and return result based on convertor cases, or empty if no match is found.
     *
     * @param source the source
     * @param convertorExecutor convertor executor
     * @return the optional
     */
    public Optional<T> process(final F source, final ConvertorExecutor convertorExecutor) {
        return process(source, null, convertorExecutor);
    }

    /**
     * Process source and return result based on convertor cases, or empty if no match is found.
     *
     * @param source the source
     * @param data   the data
     * @param convertorExecutor convertor executor
     * @return the optional
     */
    public Optional<T> process(final F source, final D data, final ConvertorExecutor convertorExecutor) {
        Optional<T> result = Optional.empty();
        final short version = data != null ? data.getVersion() : OFP_VERSION_ALL;

        if (Objects.isNull(source)) {
            LOG.trace("Failed to convert null for version {}", version);
            return result;
        }

        final Class<?> clazz = source.getImplementedInterface();
        final Optional<ConvertorCase<?, T, D>> caseOptional = Optional
                .ofNullable(getCasesForVersion(version).get(clazz));

        final ConvertorCase<?, T, D> processorCase = caseOptional.orElse(defaultCase);

        if (Objects.nonNull(processorCase)) {
            result = processorCase.processRaw(source, data, convertorExecutor);

            if (processorCase.isErrorOnEmpty() && !result.isPresent()) {
                LOG.warn("Failed to process {} for version {}", clazz, version);
            }
        } else {
            LOG.trace("Failed to process {} for version {}", clazz, version);
        }

        return result;
    }

    /**
     * Sets default case, what will be used when we do not find any matching convertor case for source.
     *
     * @param defaultCase the default case
     * @return the default case
     */
    public ConvertorProcessor<F, T, D> setDefaultCase(final ConvertorCase<?, T, D> defaultCase) {
        this.defaultCase = defaultCase;
        return this;
    }

    private Map<Class<?>, ConvertorCase<?, T, D>> getCasesForVersion(final short version) {
        final Map<Class<?>, ConvertorCase<?, T, D>> casesForVersion =
                conversions.getOrDefault(version, new ConcurrentHashMap<>());

        conversions.putIfAbsent(version, casesForVersion);

        return casesForVersion;
    }
}
