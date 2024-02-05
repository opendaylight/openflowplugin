/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.spi.ForwardingDataBroker;

/**
 * Delegating {@link PingPongDataBroker} implementation.
 * This is useful for simple strongly typed dependency injection.
 *
 * @author Michael Vorburger.ch
 */
// FIXME: this should not be necessary
public class ForwardingPingPongDataBroker extends ForwardingDataBroker implements PingPongDataBroker {
    private final @NonNull DataBroker delegate;

    public ForwardingPingPongDataBroker(final DataBroker delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected DataBroker delegate() {
        return delegate;
    }

    @Override
    public TransactionChain createTransactionChain() {
        return delegate().createMergingTransactionChain();
    }
}
