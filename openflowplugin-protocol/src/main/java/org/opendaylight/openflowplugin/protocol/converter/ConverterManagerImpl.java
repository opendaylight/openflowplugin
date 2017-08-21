/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterData;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.ConverterManager;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages various converters and allows to use them all in one generic way
 */
public class ConverterManagerImpl implements ConverterManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConverterManagerImpl.class);

    // Cache, that holds all registered converters, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private final Map<Short, Map<Class<?>, Converter<?, ?, ? extends ConverterData>>> converters =
            new ConcurrentHashMap<>();

    /**
     * Create new instance of Converter Manager
     * @param supportedVersions supported versions
     */
    public ConverterManagerImpl(final Short... supportedVersions) {
        final Stream<Short> stream = Arrays.stream(supportedVersions);
        stream.forEach(version -> converters.putIfAbsent(version, new ConcurrentHashMap<>()));
    }

    @Override
    public ConverterManagerImpl registerConverter(final short version, final Converter<?, ?, ? extends ConverterData> converter) {
        final Map<Class<?>, Converter<?, ?, ? extends ConverterData>> convertersForVersion =
                converters.get(version);

        if (Objects.nonNull(convertersForVersion)) {
            for (final Class<?> type : converter.getTypes()) {
                final Converter<?, ?, ? extends ConverterData> result = convertersForVersion.get(type);

                if (Objects.isNull(result)) {
                    converter.setConverterExecutor(this);
                    convertersForVersion.put(type, converter);
                    LOG.debug("{} for version {} is now converted by {}", type, version, converter);
                } else {
                    LOG.warn("{} for version {} have already registered converter", type, version);
                }
            }
        } else {
            LOG.warn("{} do not supports version {}", this, version);
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FROM, TO, DATA extends ConverterData> Optional<TO> convert(final FROM source, final DATA data) {
        Optional<TO> result = Optional.empty();

        if (Objects.isNull(source)) {
            LOG.trace("Cannot extract type from null source");
            return result;
        }

        final Class<?> type = source instanceof DataContainer
                ? ((DataContainer)source).getImplementedInterface()
                : source.getClass();

        if (Objects.isNull(type)) {
            LOG.warn("Cannot extract type from {}, because getImplementedInterface() returns null", source);
            return result;
        }

         return findConvertor(data.getVersion(), type)
                .map(converter -> (TO)converter.convert(source, data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FROM, TO, DATA extends ConverterData> Optional<TO> convert(final Collection<FROM> source, final DATA data) {
        Optional<TO> result = Optional.empty();

        if (Objects.isNull(source)) {
            LOG.trace("Cannot extract type from null source");
            return result;
        }

        final Optional<FROM> first = source.stream().findFirst();

        if (!first.isPresent()) {
            LOG.trace("Cannot extract type from empty collection");
            return result;
        }

        final FROM firstSource = first.get();
        final Class<?> type = firstSource instanceof DataContainer
                ? ((DataContainer)firstSource).getImplementedInterface()
                : firstSource.getClass();

        if (Objects.isNull(type)) {
            LOG.warn("Cannot extract type from {}, because type returns null", source);
            return result;
        }

        return findConvertor(data.getVersion(), type)
                .map(converter -> (TO)converter.convert(source, data));
    }

    /**
     * Last resort. If we do not already have converter registered,
     * we will perform some costly operations and try to find if we
     * can convert input using any of already registered converters
     * @param type input type
     * @return found converter
     */
    @VisibleForTesting
    Optional<Converter> findConvertor(final short version, final Class<?> type) {
        final Map<Class<?>, Converter<?, ?, ? extends ConverterData>> convertersForVersion =
                converters.get(version);

        Optional<Converter> converter = Optional.empty();

        if (Objects.nonNull(convertersForVersion)) {
            converter = Optional.ofNullable(convertersForVersion.get(type));

            if (!converter.isPresent()) {
                for (final Class<?> converterType : convertersForVersion.keySet()) {
                    if (type.isAssignableFrom(converterType)) {
                        final Converter<?, ?, ? extends ConverterData> foundConverter = convertersForVersion.get(converterType);
                        converter = Optional.ofNullable(foundConverter);

                        if (converter.isPresent()) {
                            convertersForVersion.put(type, foundConverter);
                            LOG.warn("{} for version {} is now converted by {} using last resort method", type, version, foundConverter);
                            break;
                        }
                    }
                }

                if (!converter.isPresent()) {
                    LOG.warn("Converter for {} for version {} not found", type, version);
                }
            }
        } else {
            LOG.warn("{} do not supports version {}", this, version);
        }

        return converter;
    }
}