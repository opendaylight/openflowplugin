/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Generic API for all managers
 */
public interface OFPManager {

    <T extends OFPContext> T gainContext(final DeviceInfo deviceInfo);
    
}
