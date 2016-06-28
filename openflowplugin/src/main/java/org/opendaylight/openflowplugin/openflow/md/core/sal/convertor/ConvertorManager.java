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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor;
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
    }

    // Actual convertor keys
    private List<Class<?>> convertorKeys = new ArrayList<>();
    private List<Class<?>> parametrizedConvertorKeys = new ArrayList<>();

    // Cache, that holds all registered convertors, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private Map<Class<?>, Convertor> convertors = new HashMap<>();
    private Map<Class<?>, ParametrizedConvertor> parametrizedConvertors = new HashMap<>();

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
     */
    public void registerConvertor(Convertor convertor) {
        Class<?> type = convertor.getType();

        if (convertors.containsKey(type)) {
            LOG.warn("Convertor for type {} is already registered", type);
            return;
        }

        convertorKeys.add(type);
        convertors.put(type, convertor);
        LOG.debug("{} is now converted by {}", type, convertor);
    }

    /**
     * Register convertor.
     *
     * @param convertor the convertor
     */
    public void registerConvertor(ParametrizedConvertor convertor) {
        Class<?> type = convertor.getType();

        if (parametrizedConvertors.containsKey(type)) {
            LOG.warn("Convertor for type {} is already registered", type);
            return;
        }

        parametrizedConvertorKeys.add(type);
        parametrizedConvertors.put(convertor.getType(), convertor);
        LOG.debug("{} is now converted by {}", type, convertor);
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
    public <FROM, TO> Optional<TO> convert(FROM source) {
        if (Objects.isNull(source)) {
            LOG.trace("Cannot convert source, because it is null");
            return Optional.empty();
        }

        Class<?> type = source.getClass();

        if (source instanceof Collection) {
            final Iterator it = ((Collection) source).iterator();
            Object next = null;

            if (it.hasNext()) {
                next = it.next();
            }

            if (Objects.isNull(next)) {
                LOG.trace("Cannot convert {}, because it is empty collection", type);
                return Optional.empty();
            }

            type = next.getClass();
        }

        Convertor convertor = null;

        if (!convertors.containsKey(type)) {
            boolean found = false;

            for (Class<?> key : convertorKeys) {
                if (key.isAssignableFrom(type)) {
                    convertor = convertors.get(key);
                    convertors.put(type, convertor);
                    LOG.debug("{} is now converted by {}", type, convertor);
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.error("Convertor for {} not found", type);
                return Optional.empty();
            }
        }

        if (Objects.isNull(convertor)) {
            convertor = convertors.get(type);
        }

        Object result = convertor.convert(source);
        return Optional.of((TO) result);
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
    public <FROM, TO, DATA> Optional<TO> convert(FROM source, DATA data) {
        if (Objects.isNull(source)) {
            LOG.trace("Cannot convert source, because it is null");
            return Optional.empty();
        }

        Class<?> type = source.getClass();

        if (source instanceof Collection) {
            final Iterator it = ((Collection) source).iterator();
            Object next = null;

            if (it.hasNext()) {
                next = it.next();
            }

            if (Objects.isNull(next)) {
                LOG.trace("Cannot convert {}, because it is empty collection", type);
                return Optional.empty();
            }

            type = next.getClass();
        }

        ParametrizedConvertor convertor = null;

        if (!parametrizedConvertors.containsKey(type)) {
            boolean found = false;

            for (Class<?> key : parametrizedConvertorKeys) {
                if (key.isAssignableFrom(type)) {
                    convertor = parametrizedConvertors.get(key);
                    parametrizedConvertors.put(type, convertor);
                    LOG.debug("{} is now converted by {}", type, convertor);
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.error("Convertor for {} not found", type);
                return Optional.empty();
            }
        }

        if (Objects.isNull(convertor)) {
            convertor = parametrizedConvertors.get(type);
        }

        Object result = convertor.convert(source, data);
        return Optional.of((TO) result);
    }
}