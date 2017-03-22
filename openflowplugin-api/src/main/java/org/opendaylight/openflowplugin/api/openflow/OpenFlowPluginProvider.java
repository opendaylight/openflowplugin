/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.controller.md.sal.binding.api.BindingService;

/**
 * Plugin services provider
 */
public interface OpenFlowPluginProvider extends AutoCloseable, BindingService {

    /**
     * Method initializes all DeviceManager, RpcManager and related contexts.
     */
    void initialize();

}
