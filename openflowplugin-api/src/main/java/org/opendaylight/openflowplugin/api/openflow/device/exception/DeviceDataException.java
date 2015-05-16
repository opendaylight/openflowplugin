/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.exception;

import org.opendaylight.openflowjava.protocol.api.connection.DeviceRequestFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Error;

/**
 * @deprecated FIXME: TO BE REMOVED: migrate to {@link DeviceRequestFailedException}.
 */
@Deprecated
public class DeviceDataException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Error error;

    public DeviceDataException(final String message) {
        super(message);
    }

    public DeviceDataException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DeviceDataException(final String message, final Error error) {
        super(message);
        this.error= error;
    }

    public Error getError() {
        return error;
    }
}
