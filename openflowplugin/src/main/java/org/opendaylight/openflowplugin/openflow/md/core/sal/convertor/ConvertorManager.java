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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.ActionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowInstructionResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages various convertors and allows to use them all in one generic way
 */
public class ConvertorManager implements ConvertorExecutor, ConvertorRegistrator {
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
    private List<Class<? extends DataContainer>> convertorKeys = new ArrayList<>();

    // Cache, that holds all registered convertors, but they can have multiple keys,
    // based on instanceof checks in the convert method
    private Map<Class<? extends DataContainer>, Convertor> convertors = new ConcurrentHashMap<>();

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

    @Override
    @SuppressWarnings("unchecked")
    public Convertor registerConvertor(final Convertor convertor) {
        final Class<? extends DataContainer> type = convertor.getType();
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

    public <FROM extends DataContainer, TO> Optional<TO> convert(final FROM source) {
        return convert(source, null);
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
            LOG.warn("Cannot extract type from source, because getImplementedInterface() returns null");
            return result;
        }

        Convertor convertor = convertors.get(type);

        if (Objects.isNull(convertor)) {
            for (final Class<? extends DataContainer> key : convertorKeys) {
                if (key.isAssignableFrom(type)) {
                    convertor = convertors.get(key);
                    convertors.put(type, convertor);
                    LOG.debug("{} is now converted by {}", type, convertor);
                    break;
                }
            }

            if (Objects.isNull(convertor)) {
                LOG.warn("Convertor for {} not found", type);
                return result;
            }
        }

        result = Optional.of((TO) convertor.convert(source, data));
        return result;
    }

    public <FROM extends DataContainer, TO> Optional<TO> convert(final Collection<FROM> source) {
        return convert(source, null);
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
            LOG.warn("Cannot extract type from source, because getImplementedInterface() returns null");
            return result;
        }

        Convertor convertor = convertors.get(type);

        if (Objects.isNull(convertor)) {
            for (final Class<? extends DataContainer> key : convertorKeys) {
                if (key.isAssignableFrom(type)) {
                    convertor = convertors.get(key);
                    convertors.put(type, convertor);
                    LOG.debug("{} is now converted by {}", type, convertor);
                    break;
                }
            }

            if (Objects.isNull(convertor)) {
                LOG.warn("Convertor for {} not found", type);
                return result;
            }
        }

        result = Optional.of((TO) convertor.convert(source, data));
        return result;
    }
}