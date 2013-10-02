/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.plan;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

/**
 * @author mirehak
 */
public interface SwitchTestRcpResponseEvent extends SwitchTestEvent {

    /**
     * @return switch notification/rpc response
     */
    public OfHeader getPlannedRpcResponse();

    /**
     * @return response transaction id
     */
    public long getXid();
}
