/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ForwardingDataBroker;

/**
 * Delegating {@link PingPongDataBroker} implementation.
 * This is useful for simple strongly typed dependency injection.
 *
 * @author Michael Vorburger.ch
 */
public class ForwardingPingPongDataBroker extends ForwardingDataBroker implements PingPongDataBroker {

    private final DataBroker delegate;

    public ForwardingPingPongDataBroker(DataBroker delegate) {
        this.delegate = delegate;
    }

    @Override
    protected DataBroker delegate() {
        return delegate;
    }
}
