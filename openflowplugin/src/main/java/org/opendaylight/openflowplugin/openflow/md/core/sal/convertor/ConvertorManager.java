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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowInstructionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages various convertors and allows to use them all in one generic way
 */
public class ConvertorManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConvertorManager.class);
    private static ConvertorManager INSTANCE;

    static {
        INSTANCE = new ConvertorManager();
        // All convertors are registered here
        INSTANCE.registerConvertor(new TableFeaturesConvertor());
        INSTANCE.registerConvertor(new TableFeaturesResponseConvertor());
        INSTANCE.registerConvertor(new MeterConvertor());
        INSTANCE.registerConvertor(new MeterStatsResponseConvertor());
        INSTANCE.registerConvertor(new MeterConfigStatsResponseConvertor());
        INSTANCE.registerConvertor(new PortConvertor());
        // TODO: Add MatchConvertor
        INSTANCE.registerConvertor(new MatchResponseConvertor());
        INSTANCE.registerConvertor(new MatchV10ResponseConvertor());
        INSTANCE.registerConvertor(new ActionConvertor());
        INSTANCE.registerConvertor(new ActionResponseConvertor());
        INSTANCE.registerConvertor(new GroupConvertor());
        INSTANCE.registerConvertor(new GroupDescStatsResponseConvertor());
        INSTANCE.registerConvertor(new GroupStatsResponseConvertor());
        INSTANCE.registerConvertor(new PacketOutConvertor());
        INSTANCE.registerConvertor(new FlowConvertor());
        INSTANCE.registerConvertor(new FlowInstructionResponseConvertor());
        INSTANCE.registerConvertor(new FlowStatsResponseConvertor());
    }

    // Actual convertor keys
    private List<Class<?>> convertorKeys = new ArrayList<>();
    private List<Class<?>> parametrizedConvertorKeys = new ArrayList<>();

    // Cache, that holds all registered convertors, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private Map<Class<?>, Convertor> convertors = new ConcurrentHashMap<>();
    private Map<Class<?>, ParametrizedConvertor> parametrizedConvertors = new ConcurrentHashMap<>();

    private ConvertorManager() {
        // Hiding implicit constructor
    }

    /**
     * Gets instance of Convertor Manager.
     *
     * @return the instance
     */
    public static ConvertorManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register convertor.
     *
     * @param convertor the convertor
     * @return if registration was successful
     */
    public Convertor registerConvertor(final Convertor convertor) {
        final Class<?> type = convertor.getType();
        final Convertor result = convertors.get(type);

        if (Objects.isNull(result)) {
            convertorKeys.add(type);
            convertors.put(type, convertor);
            LOG.debug("{} is now converted by {}", type, convertor);
        } else {
            LOG.warn("Convertor for type {} is already registered", type);
        }

        return result;
    }

    /**
     * Register convertor.
     *
     * @param convertor the convertor
     * @return if registration was successful
     */
    public ParametrizedConvertor registerConvertor(final ParametrizedConvertor convertor) {
        final Class<?> type = convertor.getType();
        final ParametrizedConvertor result = parametrizedConvertors.get(type);

        if (Objects.isNull(result)) {
            parametrizedConvertorKeys.add(type);
            parametrizedConvertors.put(type, convertor);
            LOG.debug("{} is now converted by {}", type, convertor);
        } else {
            LOG.warn("Convertor for type {} is already registered", type);
        }

        return result;
    }

    /**
     * Lookup and use convertor by specified type, then converts source and returns converted result
     *
     * @param <FROM> the source type
     * @param <TO>   the result type
     * @param source the source
     * @return the result (can be empty, if no convertor was found)
     */
    @SuppressWarnings("unchecked")
    public <FROM, TO> Optional<TO> convert(final FROM source) {
        final Optional<Class<?>> optionalType = extractType(source);

        if (!optionalType.isPresent()) {
            LOG.trace("Cannot convert {}", source);
            return Optional.empty();
        }

        final Class<?> type = optionalType.get();
        Convertor convertor = convertors.get(type);

        if (Objects.isNull(convertor)) {
            for (final Class<?> key : convertorKeys) {
                if (key.isAssignableFrom(type)) {
                    convertor = convertors.get(key);
                    convertors.put(type, convertor);
                    LOG.debug("{} is now converted by {}", type, convertor);
                    break;
                }
            }

            if (Objects.isNull(convertor)) {
                LOG.warn("Convertor for {} not found", type);
                return Optional.empty();
            }
        }

        return Optional.of((TO) convertor.convert(source));
    }

    /**
     * Lookup and use convertor by specified type, then converts source and returns converted result
     *
     * @param <FROM> the source type
     * @param <TO>   the result type
     * @param <DATA> the data type
     * @param source the source
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
     */
    @SuppressWarnings("unchecked")
    public <FROM, TO, DATA extends ConvertorData> Optional<TO> convert(final FROM source, final DATA data) {
        final Optional<Class<?>> optionalType = extractType(source);

        if (!optionalType.isPresent()) {
            LOG.trace("Cannot convert {}", source);
            return Optional.empty();
        }

        final Class<?> type = optionalType.get();
        ParametrizedConvertor convertor = parametrizedConvertors.get(type);

        if (Objects.isNull(convertor)) {
            for (final Class<?> key : parametrizedConvertorKeys) {
                if (key.isAssignableFrom(type)) {
                    convertor = parametrizedConvertors.get(key);
                    parametrizedConvertors.put(type, convertor);
                    LOG.debug("{} is now converted by {}", type, convertor);
                    break;
                }
            }

            if (Objects.isNull(convertor)) {
                LOG.warn("Convertor for {} not found", type);
                return Optional.empty();
            }
        }

        return Optional.of((TO) convertor.convert(source, data));
    }

    private <FROM> Optional<Class<?>> extractType(FROM source) {
        if (Objects.isNull(source)) {
            LOG.trace("Cannot extract type from source, because it is null");
            return Optional.empty();
        }

        Class<?> type = source.getClass();

        if (source instanceof Collection) {
            final Optional first = ((Collection) source).stream().findFirst();

            if (!first.isPresent()) {
                LOG.trace("Cannot extract type {}, because it is empty collection", type);
                return Optional.empty();
            }

            type = first.get().getClass();
        }

        return Optional.of(type);
    }
}