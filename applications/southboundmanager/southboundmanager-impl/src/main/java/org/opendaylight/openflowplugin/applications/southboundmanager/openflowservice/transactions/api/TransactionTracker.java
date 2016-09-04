/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api;

import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.ErrorCallable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;

/**
 * For each of the entries in TransactionTrackerFactory, there is a TransactionTracker
 * instance that contains the txn-id to the message result mapping for each of
 * the node-ids.
 */
public interface TransactionTracker {
    /**
     * adds an entry into the transaction map for the particular txn-id with the
     * message result on the event of the rpc being successful.
     */
    public void addTransactionEntry(TransactionId txId, ErrorCallable errorCallable);

    /**
     * Removes the txnId on error from the node, to be used with discretion.
     * If the transaction is part of a bundle, it will remove the bundle.
     */
    public void removeTransactionEntry(TransactionId txId);

    /**
     * Get the ErrorCallable for the given txnId.
     */
    public ErrorCallable getTransactionEntry(TransactionId txId);

    /**
     * Generates monotonically increasing Bundle Ids.
     */
    public String generateBundleId();

    /**
     * Return the hasOwner attribute stored with the tracker for the entity it is tracking.
     */
    public boolean getHasOwner();

    /**
     * Return the isOwner attribute stored with the tracker for the entity it is tracking.
     */
    public boolean getIsOwner();

    /**
     * Set the hasOwner attribute in the tracker for the entity it is tracking.
     */
    public void setHasOwner(boolean hasOwner);

    /**
     * Set the isOwner attribute in the tracker for the entity it is tracking.
     */
    public void setIsOwner(boolean isOwner);

    /**
     * Invalidates all the entries in the tracker.
     */
    public void invalidateAll();
}
