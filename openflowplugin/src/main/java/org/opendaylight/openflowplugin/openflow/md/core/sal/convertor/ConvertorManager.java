/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Convertor manager.
 */
public class ConvertorManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConvertorManager.class);
    private static ConvertorManager INSTANCE;

    static {
        INSTANCE = new ConvertorManager();
    }

    private Map<Class<?>, Convertor> convertors = new HashMap<>();
    private Map<Class<?>, ParametrizedConvertor> parametrizedConvertors = new HashMap<>();

    private ConvertorManager() {
        // Hiding implicit constructor
    }

    /**
     * Gets instance.
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
        if (convertors.containsKey(convertor.getType())) {
            LOG.warn("Convertor for type {} is already registered", convertor.getType());
            return;
        }

        convertors.put(convertor.getType(), convertor);
    }

    /**
     * Register convertor.
     *
     * @param convertor the convertor
     */
    public void registerConvertor(ParametrizedConvertor convertor) {
        if (parametrizedConvertors.containsKey(convertor.getKey())) {
            LOG.warn("Convertor for type {} is already registered", convertor.getKey());
            return;
        }

        parametrizedConvertors.put(convertor.getKey(), convertor);
    }

    /**
     * Convert optional.
     *
     * @param <FROM> the type parameter
     * @param <TO>   the type parameter
     * @param type   the type
     * @param source the source
     * @return the optional
     */
    @SuppressWarnings("unchecked")
    public <FROM, TO> Optional<TO> convert(Class<?> type, FROM source) {
        if (!convertors.containsKey(type)) {
            LOG.error("Convertor for type {} not found", type);
            return Optional.empty();
        }

        Convertor convertor = convertors.get(type);

        Object result = convertor.convert(source);
        return Optional.of((TO) result);
    }

    /**
     * Convert optional.
     *
     * @param <FROM> the type parameter
     * @param <TO>   the type parameter
     * @param <DATA> the type parameter
     * @param type   the type
     * @param source the source
     * @param data   the data
     * @return the optional
     */
    @SuppressWarnings("unchecked")
    public <FROM, TO, DATA> Optional<TO> convert(Class<?> type, FROM source, DATA data) {
        if (!parametrizedConvertors.containsKey(type)) {
            LOG.error("Convertor for type {} not found", type);
            return Optional.empty();
        }

        ParametrizedConvertor convertor = parametrizedConvertors.get(type);

        Object result = convertor.convert(source, data);
        return Optional.of((TO) result);
    }
}
