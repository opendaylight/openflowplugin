/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import static java.util.Objects.requireNonNullElse;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.MatchNormalizationUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class FlowRegistryKeyFactory {
    // Temporary use, we will overwrite this. We do not have a flow ID and are about to make one up
    // from the contents -- but for that we need a Flow object. But for that we need a FlowId, but
    // ... so we set a dummy value and overwrite it afterwards.
    public static final FlowKey DUMMY_FLOW_KEY = new FlowKey(new FlowId("__DUMMY_ID_FOR_ALIEN__"));

    public static final @NonNull FlowRegistryKeyFactory VERSION_1_0 =
        new FlowRegistryKeyFactory(OFConstants.OFP_VERSION_1_0);
    public static final @NonNull FlowRegistryKeyFactory VERSION_1_3 =
        new FlowRegistryKeyFactory(OFConstants.OFP_VERSION_1_3);
    private static final LoadingCache<Uint8, @NonNull FlowRegistryKeyFactory> CACHE = CacheBuilder.newBuilder()
        .weakValues().build(new CacheLoader<>() {
            @Override
            public FlowRegistryKeyFactory load(final Uint8 key) {
                return new FlowRegistryKeyFactory(key);
            }
        });

    private final MatchNormalizationUtil matchNormalizer;

    private FlowRegistryKeyFactory(final Uint8 version) {
        matchNormalizer = MatchNormalizationUtil.ofVersion(version);
    }

    public static @NonNull FlowRegistryKeyFactory ofVersion(final Uint8 version) {
        if (OFConstants.OFP_VERSION_1_3.equals(version)) {
            return VERSION_1_3;
        } else if (OFConstants.OFP_VERSION_1_0.equals(version)) {
            return VERSION_1_0;
        } else {
            return CACHE.getUnchecked(version);
        }
    }

    public @NonNull FlowRegistryKey create(final @NonNull Flow flow) {
        // FIXME: mandatory flow input values (or default values) should be specified via YANG model
        final var priority = requireNonNullElse(flow.getPriority(), OFConstants.DEFAULT_FLOW_PRIORITY);
        final var cookie = requireNonNullElse(flow.getCookie(), OFConstants.DEFAULT_FLOW_COOKIE).getValue();
        return new FlowRegistryKeyImpl(flow.requireTableId().toJava(), priority.toJava(), cookie,
            matchNormalizer.normalizeMatch(requireNonNullElse(flow.getMatch(), OFConstants.EMPTY_MATCH)));
    }
}
