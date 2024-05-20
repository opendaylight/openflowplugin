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
 * A {@link DataBroker} implementation which always creates merging transaction chains.
 *
 * @author Michael Vorburger.ch
 */
final class PingPongDataBroker extends ForwardingDataBroker {
    private final @NonNull DataBroker delegate;

    PingPongDataBroker(final DataBroker delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected DataBroker delegate() {
        return delegate;
    }

    @Override
    public TransactionChain createTransactionChain() {
        return delegate.createMergingTransactionChain();
    }
}
