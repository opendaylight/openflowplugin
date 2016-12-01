/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;

public class ContextChainImpl implements ContextChain {

    private Set<OFPContext> contexts = new HashSet<>();
    private ContextChainState contextChainState;
    private ConnectionContext primaryConnectionContext;

    public ContextChainImpl() {
        this.contextChainState = ContextChainState.WORKINGSLAVE;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public <T extends OFPContext> void addContext(final T context) {

    }

    @Override
    public ListenableFuture<Void> stopChain() {
        this.contextChainState = ContextChainState.WORKINGSLAVE;
        return Futures.immediateFuture(null);
    }

    @Override
    public ListenableFuture<Void> startChain() {
        this.contextChainState = ContextChainState.WORKINGMASTER;
        return Futures.immediateFuture(null);
    }

    @Override
    public void close() {

    }

    @Override
    public void changePrimaryConnection(final ConnectionContext connectionContext) {
        //TODO: recreate stuff in device context !!!
        this.primaryConnectionContext = connectionContext;
    }

    @Override
    public ContextChainState getContextChainState() {
        return contextChainState;
    }

    @Override
    public ListenableFuture<Void> connectionDropped() {
        ContextChainState oldState = this.contextChainState;
        this.contextChainState = ContextChainState.SLEEPING;
        if (oldState.equals(ContextChainState.WORKINGMASTER)) {
            return this.stopChain();
        }
        return Futures.immediateFuture(null);
    }
}
