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

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowplugin.api.openflow.md.core.sal.BuildSwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.flow.node.SwitchFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SwitchFeaturesUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SwitchFeaturesUtil.class);
    private static SwitchFeaturesUtil INSTANCE = new SwitchFeaturesUtil();

    private final Map<Short, BuildSwitchFeatures> swFeaturesBuilders;

    private SwitchFeaturesUtil() {
        swFeaturesBuilders = new HashMap<>();
        swFeaturesBuilders.put(OFP_VERSION_1_0, BuildSwitchCapabilitiesOF10.getInstance());
        swFeaturesBuilders.put(OFP_VERSION_1_3, BuildSwitchCapabilitiesOF13.getInstance());
    }

    /**
     * Get singleton instance.
     *
     * @return instance
     */
    public static SwitchFeaturesUtil getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the features of the switch.
     *
     * @param features
     *        {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput}
     * @return switch features
     */
    public SwitchFeatures buildSwitchFeatures(final GetFeaturesOutput features) {
        if (swFeaturesBuilders.containsKey(features.getVersion().toJava())) {
            LOG.debug("map contains version {}", features.getVersion());
            try {
                return swFeaturesBuilders.get(features.getVersion().toJava()).build(features);
            } catch (NullPointerException e) {
                LOG.warn("error while building switch features: {}", e.getMessage());
                LOG.debug("error while building switch features.. ", e);
            }
        } else {
            LOG.warn("unknown version: {}", features.getVersion());
        }
        return null;
    }
}
