/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.device.handlers;

/**
 * This handler does the same, but is not called in the same time as
 * RequestContextReadyHandler.
 *
 * Created by Martin Bobak <mbobak@cisco.com> on 27.2.2015.
 */

public interface DeviceSynchronizedHandler extends DeviceContextReadyHandler {

}
