/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data;

/**
 * Convertor data used in {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.PacketOutConvertor}
 * containing Openflow version and XID
 */
public class PacketOutConvertorData extends VersionDatapathIdConvertorData {
    private Long xid;

    /**
     * Instantiates a new Packet out convertor data.
     *
     * @param version the version
     */
    public PacketOutConvertorData(short version) {
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