/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceProvider;

/**
 * Openflow plugin mastership change service provider.
 */
public interface OpenFlowPluginMastershipChangeServiceProvider {

    /**
     * Gets mastership change service provider.
     *
     * @return the mastership change service provider
     */
    MastershipChangeServiceProvider getMastershipChangeServiceProvider();

}
