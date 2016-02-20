/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import javax.annotation.CheckForNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.api.openflow.device.handlers
 *
 * Interface represent handler for new connected device building cycle. Every
 * implementation have some unnecessary steps which has to be done before
 * add new Device to MD-SAL DataStore.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Apr 3, 2015
 */
public interface DeviceInitializationPhaseHandler {

    /**
     * Method represents an initialization cycle for {@link DeviceContext}
     * preparation for use.
     *
     * @param deviceContext
     */
    void onDeviceContextLevelUp(@CheckForNull DeviceContext deviceContext) throws Exception;
}
