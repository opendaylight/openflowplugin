/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import org.opendaylight.controller.md.sal.common.api.clustering.Entity;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Double candidate aproach brings protected role change for all node cluster instances.
 *
 * Created by kramesha on 9/19/15.
 */
public interface RoleChangeListener extends AutoCloseable {

    /**
     * Method has to be called from MainCandidate Leaderhip notification {@link OfpRole#BECOMEMASTER}
     * It locks any MainCandidate changes and it registrates TxCandidate to Cluster.
     */
    void onDeviceTryToTakeClusterLeadership();

    /**
     * Method has to be called from TxCandidate Leadership notification {@link OfpRole#BECOMEMASTER}
     * and propagate {@link OfpRole#BECOMEMASTER} to device. When device accepts new role, it has to
     * notifies whole DeviceContext suite to take Leadership responsibility
     */
    void onDeviceTakeClusterLeadership();

    /**
     * Method has to be called from MainCandidate Leadership notification {@link OfpRole#BECOMESLAVE}
     * It locks any MainCandidate and TxCandidate changes and it starts propagate LostClusterLeadership
     * to Device and whole DeviceContext suite.
     */
    void onDeviceLostClusterLeadership();

    /**
     * We need to know when the candidate is registrated or in close process
     * @return true/false
     */
    boolean isMainCandidateRegistered();

    /**
     * We need to know when the candidate is registrated or in close process
     * @return true/false
     */
    boolean isTxCandidateRegistered();

    Entity getEntity();

    Entity getTxEntity();

    DeviceState getDeviceState();

    @Override
    void close();
}
