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
public class SwitchTestRcpResponseEventImpl implements
        SwitchTestRcpResponseEvent {

    private OfHeader response;
    private long xid;

    /**
     * @param response
     *            the response to set
     */
    public void setResponse(OfHeader response) {
        this.response = response;
    }

    @Override
    public OfHeader getPlannedRpcResponse() {
        return response;
    }

    @Override
    public long getXid() {
        return xid;
    }

    /**
     * @param xid
     *            the xid to set
     */
    public void setXid(long xid) {
        this.xid = xid;
    }

    @Override
    public String toString() {
        return "SwitchTestRcpResponseEventImpl [response=" + response.getClass().getSimpleName()
                + ", xid=" + xid + "]";
    }
}
