/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder of registration request for reconciliation (fresh operational).
 */
public class ReconciliationRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationRegistry.class);
    private final Map<NodeId, Date> registration = new ConcurrentHashMap<>();

    public Date register(NodeId nodeId) {
        Date timestamp = new Date();
        registration.put(nodeId, timestamp);
        LOG.debug("Registered for reconciliation: {}", nodeId.getValue());
        // TODO  elicit statistics gathering if not running actually
        return timestamp;
    }

    public Date unregisterIfRegistered(NodeId nodeId) {
        Date timestamp = registration.remove(nodeId);
        if (timestamp != null) {
            LOG.debug("Unregistered for reconciliation: {}", nodeId.getValue());
        }
        return timestamp;
    }

    public boolean isRegistered(NodeId nodeId) {
        return registration.get(nodeId) != null;
    }

    public Date getRegistrationTimestamp(NodeId nodeId) {
        return registration.get(nodeId);
    }

}
