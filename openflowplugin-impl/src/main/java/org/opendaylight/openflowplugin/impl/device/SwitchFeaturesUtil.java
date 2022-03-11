/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import static org.opendaylight.openflowplugin.api.OFConstants.OFP_VERSION_1_0;
import static org.opendaylight.openflowplugin.api.OFConstants.OFP_VERSION_1_3;

import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.BuildSwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SwitchFeaturesUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SwitchFeaturesUtil.class);

    private static final Map<Uint8, BuildSwitchFeatures> SWITCH_FEATURES_BUILDERS = Map.of(
        OFP_VERSION_1_0, BuildSwitchCapabilitiesOF10.getInstance(),
        OFP_VERSION_1_3, BuildSwitchCapabilitiesOF13.getInstance());

    private SwitchFeaturesUtil() {
        // Hidden on purpose
    }

    /**
     * Returns the features of the switch.
     *
     * @param features
     *        {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput}
     * @return switch features
     */
    public static SwitchFeatures buildSwitchFeatures(final GetFeaturesOutput features) {
        final var version = features.getVersion();
        final var builder = SWITCH_FEATURES_BUILDERS.get(version);
        if (builder != null) {
            LOG.debug("map contains version {}", version);
            return builder.build(features);
        }
        LOG.warn("unknown version: {}", version);
        return null;
    }
}
