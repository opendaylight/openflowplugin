/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

/**
 * Reserves unique XID for Device Messages.
 */
public interface XidSequencer {

    /**
     * Method reserve unique XID for Device Message.
     * Attention: OFJava expect the message,
     * otherwise OutboundQueue could stop working.
     * @return Reserved XID
     */
    Long reserveXidForDeviceMessage();
}
