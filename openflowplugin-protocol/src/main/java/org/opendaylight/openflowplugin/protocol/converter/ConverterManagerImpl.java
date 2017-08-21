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
import java.util.Collections;
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
 * Manages various convertors and allows to use them all in one generic way
 */
public class ConverterManagerImpl implements ConverterManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConverterManagerImpl.class);

    // Cache, that holds all registered convertors, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private Map<Short, Map<Class<? extends DataContainer>, Converter<?, ?, ? extends ConverterData>>> convertors;

    /**
     * Create new instance of Converter Manager
     * @param supportedVersions supported versions
     */
    public ConverterManagerImpl(final Short... supportedVersions) {
        final Stream<Short> stream = Arrays.stream(supportedVersions);

        if (supportedVersions.length == 1) {
            final Optional<Short> versionOptional = stream.findFirst();
            versionOptional.ifPresent(version -> convertors = Collections.singletonMap(version, new ConcurrentHashMap<>()));
        } else {
            convertors = new ConcurrentHashMap<>();
            stream.forEach(version -> convertors.putIfAbsent(version, new ConcurrentHashMap<>()));
        }
    }

    @Override
    public ConverterManagerImpl registerConverter(final short version, final Converter<?, ?, ? extends ConverterData> converter) {
        final Map<Class<? extends DataContainer>, Converter<?, ?, ? extends ConverterData>> convertorsForVersion =
                convertors.get(version);

        if (Objects.nonNull(convertorsForVersion)) {
            for (final Class<? extends DataContainer> type : converter.getTypes()) {
                final Converter<?, ?, ? extends ConverterData> result = convertorsForVersion.get(type);

                if (Objects.isNull(result)) {
                    converter.setConverterExecutor(this);
                    convertorsForVersion.put(type, converter);
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
    public <FROM extends DataContainer, TO, DATA extends ConverterData> Optional<TO> convert(final FROM source, final DATA data) {
        Optional<TO> result = Optional.empty();

        if (Objects.isNull(source)) {
            LOG.trace("Cannot extract type from null source");
            return result;
        }

        final Class<? extends DataContainer> type = source.getImplementedInterface();

        if (Objects.isNull(type)) {
            LOG.warn("Cannot extract type from {}, because getImplementedInterface() returns null", source);
            return result;
        }

         return findConvertor(data.getVersion(), type)
                .map(convertor -> (TO)convertor.convert(source, data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FROM extends DataContainer, TO, DATA extends ConverterData> Optional<TO> convert(final Collection<FROM> source, final DATA data) {
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

        final Class<? extends DataContainer> type = first.get().getImplementedInterface();

        if (Objects.isNull(type)) {
            LOG.warn("Cannot extract type from {}, because getImplementedInterface() returns null", source);
            return result;
        }

        return findConvertor(data.getVersion(), type)
                .map(convertor -> (TO)convertor.convert(source, data));
    }

    /**
     * Last resort. If we do not already have converter registered,
     * we will perform some costly operations and try to find if we
     * can convert input using any of already registered convertors
     * @param type input type
     * @return found converter
     */
    @VisibleForTesting
    Optional<Converter> findConvertor(final short version, final Class<? extends DataContainer> type) {
        final Map<Class<? extends DataContainer>, Converter<?, ?, ? extends ConverterData>> convertorsForVersion =
                convertors.get(version);

        Optional<Converter> convertor = Optional.empty();

        if (Objects.nonNull(convertorsForVersion)) {
            convertor = Optional.ofNullable(convertorsForVersion.get(type));

            if (!convertor.isPresent()) {
                for (final Class<? extends DataContainer> convertorType : convertorsForVersion.keySet()) {
                    if (type.isAssignableFrom(convertorType)) {
                        final Converter<?, ?, ? extends ConverterData> foundConverter = convertorsForVersion.get(convertorType);
                        convertor = Optional.ofNullable(foundConverter);

                        if (convertor.isPresent()) {
                            convertorsForVersion.put(type, foundConverter);
                            LOG.warn("{} for version {} is now converted by {} using last resort method", type, version, foundConverter);
                            break;
                        }
                    }
                }

                if (!convertor.isPresent()) {
                    LOG.warn("Converter for {} for version {} not found", type, version);
                }
            }
        } else {
            LOG.warn("{} do not supports version {}", this, version);
        }

        return convertor;
    }
}