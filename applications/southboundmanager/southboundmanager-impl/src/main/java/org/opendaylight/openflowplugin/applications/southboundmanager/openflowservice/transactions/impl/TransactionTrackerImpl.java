/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.ErrorCallable;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTracker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionTrackerImpl implements TransactionTracker {

    private static final Logger s_logger = LoggerFactory.getLogger(TransactionTrackerImpl.class);

    private final NodeId nodeId;
    private AtomicLong bundleIdGenerator;
    private boolean hasOwner;
    private boolean isOwner;

    private Cache<TransactionId, ErrorCallable> m_mapTransactions;


    private TransactionTrackerImpl(NodeId nodeId, AtomicLong bundleIdGenerator) {
        this.nodeId = nodeId;
        this.bundleIdGenerator = bundleIdGenerator;
    }

    static TransactionTrackerImpl createInstance(NodeId nodeId, AtomicLong bundleIdGenerator){
        s_logger.debug("Creating new TransactionTracker for nodeId: {}", nodeId);
        TransactionTrackerImpl transactionTrackerImplOut;

        transactionTrackerImplOut = new TransactionTrackerImpl(nodeId, bundleIdGenerator);
        transactionTrackerImplOut.init();

        return transactionTrackerImplOut;
    }


    private void init(){
        // TODO: Make 5000 ms as a configurable param.
        s_logger.info("{} - init", this);
        m_mapTransactions = CacheBuilder.newBuilder()
                .expireAfterWrite(5000, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void addTransactionEntry(TransactionId txId, ErrorCallable callable) {
        s_logger.debug("Adding transaction Entry: {}", txId);
        m_mapTransactions.put(txId, callable);
    }

    @Override
    public void removeTransactionEntry(TransactionId txId) {
        s_logger.debug("Removing transaction Entry: {}", txId);
        m_mapTransactions.invalidate(txId);
    }

    @Override
    public ErrorCallable getTransactionEntry(TransactionId txId) {
        return m_mapTransactions.getIfPresent(txId);
    }

    @Override
    public String generateBundleId() {
        return Long.toString(bundleIdGenerator.getAndIncrement());
    }

    @Override
    public boolean getHasOwner() {
        return this.hasOwner;
    }

    @Override
    public boolean getIsOwner() {
        return this.isOwner;
    }

    @Override
    public void setHasOwner(boolean hasOwner) {
        this.hasOwner = hasOwner;
    }

    @Override
    public void setIsOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }

    @Override
    public void invalidateAll() {
        this.m_mapTransactions.invalidateAll();
    }
}
