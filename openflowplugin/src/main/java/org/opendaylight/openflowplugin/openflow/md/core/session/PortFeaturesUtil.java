/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for bandwidth extracting utilities
 *
 * @author jsebin
 *
 */
public class PortFeaturesUtil {

    private static PortFeaturesUtil instance = new PortFeaturesUtil();

    private final Map<Short, IGetBandwith> portVersionBandwidth;
    private static final Logger LOG = LoggerFactory.getLogger(PortFeaturesUtil.class);

    private PortFeaturesUtil() {
        this.portVersionBandwidth = new HashMap<>();

        portVersionBandwidth.put((short) 1, FeaturesV10Bandwidth.getInstance());
        portVersionBandwidth.put((short) 4, FeaturesV13Bandwidth.getInstance());
    }

    /**
     *
     * @return instance
     */
    public static PortFeaturesUtil getInstance() {
        return instance;
    }

    /**
     *
     * @param msg {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatus}
     * @return port bandwidth
     */
    public Boolean getPortBandwidth(PortStatus msg) {

        if(portVersionBandwidth.containsKey(msg.getVersion()) == true) {
            try {
                return portVersionBandwidth.get(msg.getVersion()).getBandwidth(msg);
            } catch (NullPointerException e) {
                LOG.warn("error while getting port features: {}", e.getMessage());
                LOG.debug("error while getting port features.. ", e);
            }
        }
        else {
            LOG.warn("unknown port version: {}", msg.getVersion());
        }

        return null;
    }

}
