/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages various convertors and allows to use them all in one generic way.
 */
public class ConvertorManager implements ConvertorExecutor, ConvertorRegistrator {
    private static final Logger LOG = LoggerFactory.getLogger(ConvertorManager.class);

    // Cache, that holds all registered convertors, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private Map<Uint8, Map<Class<?>, Convertor<?, ?, ? extends ConvertorData>>> convertors;

    /**
     * Create new instance of Convertor Manager.
     *
     * @param supportedVersions supported versions
     */
    public ConvertorManager(final Uint8... supportedVersions) {
        final Stream<Uint8> stream = Arrays.stream(supportedVersions);

        if (supportedVersions.length == 1) {
            final Optional<Uint8> versionOptional = stream.findFirst();
            versionOptional.ifPresent(version -> convertors = Map.of(version, new ConcurrentHashMap<>()));
        } else {
            convertors = new ConcurrentHashMap<>();
            stream.forEach(version -> convertors.putIfAbsent(version, new ConcurrentHashMap<>()));
        }
    }

    @Override
    public ConvertorManager registerConvertor(final Uint8 version,
            final Convertor<?, ?, ? extends ConvertorData> convertor) {
        final Map<Class<?>, Convertor<?, ?, ? extends ConvertorData>> convertorsForVersion =
                convertors.get(requireNonNull(version));

        if (convertorsForVersion != null) {
            for (final Class<?> type : convertor.getTypes()) {
                final Convertor<?, ?, ? extends ConvertorData> result = convertorsForVersion.get(type);

                if (result == null) {
                    convertor.setConvertorExecutor(this);
                    convertorsForVersion.put(type, convertor);
                    LOG.debug("{} for version {} is now converted by {}", type, version, convertor);
                } else {
                    LOG.warn("{} for version {} have already registered convertor", type, version);
                }
            }
        } else {
            LOG.warn("{} do not supports version {}", this, version);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F, T, D extends ConvertorData> Optional<T> convert(final F source, final D data) {
        Optional<T> result = Optional.empty();

        if (source == null) {
            LOG.trace("Cannot extract type from null source");
            return result;
        }

        final Class<?> type = source instanceof DataContainer ? ((DataContainer) source).implementedInterface()
                : source.getClass();

        if (type == null) {
            LOG.warn("Cannot extract type from {}, because implementedInterface() returns null", source);
            return result;
        }

        return findConvertor(data.getVersion(), type).map(convertor -> (T)convertor.convert(source, data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, F, T, D extends ConvertorData> Optional<T> convert(final Map<K, F> source, final D data) {
        Optional<T> result = Optional.empty();

        if (source == null) {
            LOG.trace("Cannot extract type from null source");
            return result;
        }

        final Optional<F> firstOptional = source.values().stream().findFirst();
        if (firstOptional.isEmpty()) {
            LOG.trace("Cannot extract type from empty collection");
            return result;
        }

        final F first = firstOptional.orElseThrow();

        final Class<?> type = first instanceof DataContainer dataContainer ? dataContainer.implementedInterface()
                : first.getClass();

        if (type == null) {
            LOG.warn("Cannot extract type from {}, because implementedInterface() returns null", source);
            return result;
        }

        return findConvertor(data.getVersion(), type).map(convertor -> (T)convertor.convert(source.values(), data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F, T, D extends ConvertorData> Optional<T> convert(final Collection<F> source, final D data) {
        Optional<T> result = Optional.empty();

        if (source == null) {
            LOG.trace("Cannot extract type from null source");
            return result;
        }

        final Optional<F> firstOptional = source.stream().findFirst();
        if (firstOptional.isEmpty()) {
            LOG.trace("Cannot extract type from empty collection");
            return result;
        }

        final F first = firstOptional.orElseThrow();
        final Class<?> type = first instanceof DataContainer dataContainer ? dataContainer.implementedInterface()
                : first.getClass();

        if (type == null) {
            LOG.warn("Cannot extract type from {}, because implementedInterface() returns null", source);
            return result;
        }

        return findConvertor(data.getVersion(), type).map(convertor -> (T)convertor.convert(source, data));
    }

    /**
     * Last resort. If we do not already have convertor registered,
     * we will perform some costly operations and try to find if we
     * can convert input using any of already registered convertors
     * @param type input type
     * @return found convertor
     */
    @VisibleForTesting
    Optional<Convertor> findConvertor(final Uint8 version, final Class<?> type) {
        final Map<Class<?>, Convertor<?, ?, ? extends ConvertorData>> convertorsForVersion =
                convertors.get(requireNonNull(version));

        Optional<Convertor> convertor = Optional.empty();

        if (convertorsForVersion != null) {
            convertor = Optional.ofNullable(convertorsForVersion.get(type));

            if (!convertor.isPresent()) {
                for (Entry<Class<?>, Convertor<?, ?, ? extends ConvertorData>> entry :
                            convertorsForVersion.entrySet()) {
                    final Class<?> convertorType = entry.getKey();
                    if (type.isAssignableFrom(convertorType)) {
                        final Convertor<?, ?, ? extends ConvertorData> foundConvertor = entry.getValue();
                        convertor = Optional.ofNullable(foundConvertor);

                        if (convertor.isPresent()) {
                            convertorsForVersion.put(type, foundConvertor);
                            LOG.warn("{} for version {} is now converted by {} using last resort method",
                                    type, version, foundConvertor);
                            break;
                        }
                    }
                }

                if (!convertor.isPresent()) {
                    LOG.warn("Convertor for {} for version {} not found", type, version);
                }
            }
        } else {
            LOG.warn("{} do not supports version {}", this, version);
        }

        return convertor;
    }
}
