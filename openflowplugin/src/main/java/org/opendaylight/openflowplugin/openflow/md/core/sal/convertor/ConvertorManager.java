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

        // Type: org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures.class
        INSTANCE.registerConvertor(new TableFeaturesConvertor());

        // Type: org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures.class
        INSTANCE.registerConvertor(new TableFeaturesResponseConvertor());

        // Type: org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter.class
        INSTANCE.registerConvertor(new MeterConvertor());

        // Type: org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats.class
        INSTANCE.registerConvertor(new MeterStatsResponseConvertor());

        // Type: org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfig.class
        INSTANCE.registerConvertor(new MeterConfigStatsResponseConvertor());
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
