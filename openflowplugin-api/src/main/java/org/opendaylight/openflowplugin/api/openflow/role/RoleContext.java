/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.role;

import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;

/**
 * Created by kramesha on 9/12/15.
 */
public interface RoleContext extends RoleChangeListener, RequestContextStack {

    void setTxLockOwned(boolean txLockOwned);

    OfpRole getPropagatingRole();

    void setPropagatingRole(OfpRole propagatingRole);

    /**
     * Initialization method is responsible for a registration of
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.Entity}
     * and listen for notification from service. {@link Future} returned object is used primary
     * for new connection initialization phase where we have to wait for actual Role.
     * The {@link Future} has to be canceled if device is in disconnected state or when
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService} returns
     * {@link org.opendaylight.controller.md.sal.common.api.clustering.CandidateAlreadyRegisteredException}
     */
    void initialization() throws CandidateAlreadyRegisteredException;

    /**
     * Transaction Candidate will provide safe way to correctly finish TxChainManager from
     * last Node Master. It means only Master of TxEntity could hold TxChainFactory and
     * active TransactionChain to write Data to Distributed DataStore.
     */
    void setupTxCandidate() throws CandidateAlreadyRegisteredException;

    /**
     * UnregistrationTxCandidate from OwnershipService
     */
    void suspendTxCandidate();

    @Override
    void close();

    DeviceContext getDeviceContext();

    boolean isTxLockOwned();

    void firstTimeRun();
}
