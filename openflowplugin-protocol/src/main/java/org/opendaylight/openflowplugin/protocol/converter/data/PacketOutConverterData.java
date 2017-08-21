/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.data;

/**
 * Converter data used in {@link org.opendaylight.openflowplugin.protocol.converter.PacketOutConverter}
 * containing Openflow version and XID
 */
public class PacketOutConverterData extends VersionDatapathIdConverterData {
    private Long xid;

    /**
     * Instantiates a new Packet out converter data.
     *
     * @param version the version
     */
    public PacketOutConverterData(short version) {
        super(version);
    }

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public Long getXid() {
        return xid;
    }

    /**
     * Sets xid.
     *
     * @param xid the xid
     */
    public void setXid(Long xid) {
        this.xid = xid;
    }
}