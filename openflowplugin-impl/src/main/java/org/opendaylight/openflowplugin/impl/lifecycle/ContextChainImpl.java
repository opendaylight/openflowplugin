/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;

public class ContextChainImpl implements ContextChain {

    private Set<OFPContext> contexts = new HashSet<>();
    private final ContextChainState contextChainState;

    public ContextChainImpl(final ContextChainState contextChainState) {
        this.contextChainState = contextChainState;
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public <T extends OFPContext> void addContext(final T context) {

    }

    @Override
    public Future<Void> stopChain() {
        return null;
    }

    @Override
    public Future<Void> startChain() {
        return null;
    }

    @Override
    public void close() {

    }
}
