/*
 * Copyright (c) 2018 Red Hat, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.spi.connection;

import com.google.common.collect.ForwardingList;
import java.util.List;

/**
 * {@link List} of {@link SwitchConnectionProvider}.
 * This is useful for strongly typed dependency injection,
 * and makes it simpler to have a common single source of truth
 * between Blueprint and other DI frameworks in a standalone environment.
 *
 * @author Michael Vorburger.ch
 */
public class SwitchConnectionProviderList extends ForwardingList<SwitchConnectionProvider> {

    private final List<SwitchConnectionProvider> switchConnectionProviders;

    public SwitchConnectionProviderList(List<SwitchConnectionProvider> switchConnectionProviders) {
        this.switchConnectionProviders = switchConnectionProviders;
    }

    @Override
    protected List<SwitchConnectionProvider> delegate() {
        return switchConnectionProviders;
    }
}
