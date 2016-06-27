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
 * Manages various convertors and allows to use them all in one generic way
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
        if (parametrizedConvertors.containsKey(convertor.getType())) {
            LOG.warn("Convertor for type {} is already registered", convertor.getType());
            return;
        }

        parametrizedConvertors.put(convertor.getType(), convertor);
    }

    /**
     * Lookup and use convertor by specified type, then converts source and returns converted result
     *
     * @param <FROM> the source type
     * @param <TO>   the result type
     * @param type   type of convertor,
     *               see {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor#getType()}
     *               and {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor#getType()}
     * @param source the source
     * @return the result (can be empty, if no convertor was found)
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
     * Lookup and use convertor by specified type, then converts source and returns converted result
     *
     * @param <FROM> the source type
     * @param <TO>   the result type
     * @param type   type of convertor,
     *               see {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor#getType()}
     *               and {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ParametrizedConvertor#getType()}
     * @param source the source
     * @param data   convertor data
     * @return the result (can be empty, if no convertor was found)
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
