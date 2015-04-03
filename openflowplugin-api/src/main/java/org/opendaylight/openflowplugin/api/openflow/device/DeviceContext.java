/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MessageHandler;
import org.opendaylight.openflowplugin.api.openflow.translator.TranslatorLibrarian;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import java.math.BigInteger;

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
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface DeviceContext extends DeviceReplyProcessor, MessageHandler, TranslatorLibrarian {


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
     * @return {@link DeviceState}
     */
    DeviceState getDeviceState();

    /**
     * Method exposes possibility for write a child of {@link DataObject} to transaction for DataStore.
     */
    <T extends DataObject> void writeToTransaction(final LogicalDatastoreType store, final InstanceIdentifier<T> path, final T data);

    /**
     * Method exposes transaction created for device
     * represented by this context. This should be used as read only.
     * This read only transaction has a fresh dataStore snapshot and
     * here is a possibility to get different data set from  DataStore
     * as have a write transaction in this context.
     */
    ReadTransaction getReadTransaction();

    /**
     * Method provides capabilities of connected device.
     *
     * @return
     */
    TableFeatures getCapabilities();

    /**
     * Method provides current devices connection context.
     *
     * @return
     */
    ConnectionContext getPrimaryConnectionContext();

    /**
     * Method provides current devices auxiliary connection contexts.
     *
     * @return
     */
    ConnectionContext getAuxiliaryConnectiobContexts(BigInteger cookie);

    Xid getNextXid();

    /**
     * Method writes request context into request context map. This method
     * is ment to be used by {@link org.opendaylight.openflowplugin.impl.services.OFJResult2RequestCtxFuture#processResultFromOfJava}
     *
     * @param xid
     * @param requestFutureContext
     */
    public void hookRequestCtx(Xid xid, RequestContext requestFutureContext);

}

