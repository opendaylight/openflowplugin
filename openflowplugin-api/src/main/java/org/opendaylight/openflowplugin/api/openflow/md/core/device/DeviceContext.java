/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.device;

import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.openflowplugin.api.openflow.md.core.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers.DeviceMessageHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers.DeviceContextReadyHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.rpc.RpcContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;

/**
 * The central entity of OFP is the Device Context, which encapsulate the logical state of a switch
 * as seen by the controller. Each OpenFlow session is tracked by a Connection Context.
 * These attach to a particular Device Context in such a way, that there is at most one primary
 * session associated with a Device Context. Whenever the controller needs to interact with a
 * particular switch, it will do so in the context of the calling thread, obtaining a lock on
 * the corresponding Device Context – thus the Device Context becomes the fine-grained point
 * of synchronization. The only two entities allowed to send requests towards the switch are
 * Statistics Manager and RPC Manager. Each of them allocates a Request Context for interacting
 * with a particular Device Context. The Request Contexts are the basic units of fairness,
 * which is enforced by keeping a cap on the number of outstanding requests a particular Request
 * Context can have at any point in time. Should this quota be exceeded, any further attempt to make
 * a request to the switch will fail immediately, with proper error indication.
 * <p/>
 * Created by Martin Bobak <mbobak@cisco.com> on 25.2.2015.
 */
public interface DeviceContext extends DeviceMessageHandler {


    /**
     * Method add auxiliary connection contexts to this context representing single device connection.
     *
     * @param connectionContext
     */
    void addAuxiliaryConenctionContext(ConnectionContext connectionContext);

    /**
     * Method removes auxiliary connection context from this context representing single device connection.
     *
     * @param connectionContext
     */
    void removeAuxiliaryConenctionContext(ConnectionContext connectionContext);


    /**
     * Method provides state of device represented by this device context.
     *
     * @return {@link org.opendaylight.openflowplugin.api.openflow.md.core.device.DeviceState}
     */
    DeviceState getDeviceState();

    /**
     * Method sets transaction chain used for all data store operations on device
     * represented by this context. This transaction chain is write only.
     *
     * @param transactionChain
     */
    void setTransactionChain(TransactionChain transactionChain);

    /**
     * Method exposes transaction created for device
     * represented by this context. This should be used as write only.
     */
    TransactionChain getTransactionChain();


    //TODO : If we know there's only StatisticsManager and RpcManager handling this event and not in same time, wouldn't it be better to register those separately ?

    /**
     * Method registers handler responsible for handling operations related to connected device after
     * request context is created.
     *
     * @param deviceContextReadyHandler
     */
    public void registerRequestContextReadyHandler(Class<? extends DeviceContextReadyHandler> clazz, DeviceContextReadyHandler deviceContextReadyHandler);

    /**
     * Method provides capabilities of connected device.
     *
     * @return
     */
    TableFeatures getCapabilities();

    /**
     * Method sets RPC context created for device represented by this context.
     *
     * @param rpcContext
     */
    void setRpcContext(RpcContext rpcContext);

    /**
     * Method that invokes synchronized state of device.
     */
    void stateSynchronized();

}
