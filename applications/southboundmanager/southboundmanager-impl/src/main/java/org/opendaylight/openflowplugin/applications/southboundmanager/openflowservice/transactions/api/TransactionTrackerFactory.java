/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api;

import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * TransactionTrackerFactory holds the reference of the node-id to the
 * TransactionTrackerImpl instance.From the TransactionTrackerImpl instance
 * we can figure out the txn-id to the message result. This is a singleton class.
 */
public interface TransactionTrackerFactory {

    /**
     * Gets the TransactionTrackerImpl instance for the particular
     * node-id, if it doesnot exist it creates one , it returns null
     *
     */
    public TransactionTracker getCacheEntry(NodeId nodeId);

    /**
     * Adds the given nodeId to the TransactionTrackerImpl
     * instance in case it does not exist.
     */
    public void addCacheEntry(NodeId nodeId, boolean isOwner, boolean hasOwner);

    /**
     * Removes the given nodeId from the TransactionTrackerImpl
     */
    public void removeCacheEntry(NodeId nodeId);

    /**
     * Ask this to register a notification listener using the provided service.
     */
    public void registerNotificationListener(NotificationService notificationService);

    /**
     * Ask the tx tracker factory to de-register the yang notification listener
     */
    public void deregisterNotificationListener();
}
