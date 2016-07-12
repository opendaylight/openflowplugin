/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorKey;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages various convertors and allows to use them all in one generic way
 */
public class ConvertorManager implements ConvertorExecutor, ConvertorRegistrator {
    private static final Logger LOG = LoggerFactory.getLogger(ConvertorManager.class);

    // Actual convertor keys
    private List<ConvertorKey> convertorKeys = new ArrayList<>();

    // Cache, that holds all registered convertors, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private Map<ConvertorKey, Convertor> convertors = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void registerConvertor(final short version, final Convertor convertor) {
        for (final Object typeRaw : convertor.getTypes()) {
            final Class<? extends DataContainer> type = (Class<? extends DataContainer>)typeRaw;
            final ConvertorKey key = new ConvertorKey(version, type);
            final Convertor result = convertors.get(key);

            if (Objects.isNull(result)) {
                convertor.setConvertorManager(this);
                convertorKeys.add(key);
                convertors.put(key, convertor);
                LOG.debug("{} is now converted by {}", key, convertor);
            } else {
                LOG.warn("Convertor for {} is already registered", key);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FROM extends DataContainer, TO, DATA extends ConvertorData> Optional<TO> convert(final FROM source, @Nullable final DATA data) {
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

        final Optional<Convertor> convertor = findConvertor(data.getVersion(), type);

        if (convertor.isPresent()) {
            result = Optional.of((TO) convertor.get().convert(source, data));
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <FROM extends DataContainer, TO, DATA extends ConvertorData> Optional<TO> convert(final Collection<FROM> source, @Nullable final DATA data) {
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

        final Optional<Convertor> convertor = findConvertor(data.getVersion(), type);

        if (convertor.isPresent()) {
            result = Optional.of((TO) convertor.get().convert(source, data));
        }

        return result;
    }

    /**
     * Last resort. If we do not already have convertor registered,
     * we will perform some costly operations and try to find if we
     * can convert input using any of already registered convertors
     * @param type input type
     * @return found convertor
     */
    private Optional<Convertor> findConvertor(final short version, final Class<? extends DataContainer> type) {
        final ConvertorKey key = new ConvertorKey(version, type);
        Optional<Convertor> convertor = Optional.ofNullable(convertors.get(key));

        if (!convertor.isPresent()) {
            for (final ConvertorKey convertorKey : convertorKeys) {
                if (convertorKey.isAssignableFrom(key)) {
                    final Convertor foundConvertor = convertors.get(key);
                    convertor = Optional.ofNullable(foundConvertor);
                    convertors.put(convertorKey, foundConvertor);
                    LOG.warn("{} is now converted by {} using last resort method", convertorKey, foundConvertor);
                    break;
                }
            }

            if (!convertor.isPresent()) {
                LOG.warn("Convertor for {} not found", key);
            }
        }

        return convertor;
    }
}