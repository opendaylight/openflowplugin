/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.deviceownershipservice;

public interface DeviceOwnershipService {
    /**
     * Method is used to check if the node is the entity owner of the switch.
     * @return true if the node is the owner of the switch ,
     *         false if either the switch is owned by some other node or if the cluster is in Jeopardy
     */
    boolean isEntityOwned(String nodeId);
}