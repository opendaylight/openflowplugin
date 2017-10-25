/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;

/**
 * This interface is responsible for managing lifecycle of itself and all it's associated contexts.
 * Every manager that implements this interface must handle internal map of contexts by implementing methods from
 * {@link org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler}. And at last, it must
 * handle it's own full termination by implementing {@link AutoCloseable#close()}
 */
public interface OFPManager extends
        DeviceRemovedHandler,
        AutoCloseable {

    @Override
    void close();
}
