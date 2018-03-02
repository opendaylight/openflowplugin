/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.recovery;

/**
 * OpenflowServiceRecoveryHandler is the generic interface which provides
 * facility to generate key for service recovery for openflowplugin.
 */

public interface OpenflowServiceRecoveryHandler {

    /**
     * This method is called to generate registry key for openflowplugin
     * service recovery and the service recover handler will be accessed
     * from registry using this key.
     */
    String buildServiceRegistryKey();
}
