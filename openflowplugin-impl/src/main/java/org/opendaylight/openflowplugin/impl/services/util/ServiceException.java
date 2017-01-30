/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.util;

/**
 * Exception thrown by {@link org.opendaylight.openflowplugin.impl.services.AbstractService#buildRequest(org.opendaylight.openflowplugin.api.openflow.device.Xid, Object)}
 */
public class ServiceException extends Exception {
    public ServiceException(Throwable cause) {
        super(cause);
    }
}
