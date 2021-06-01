/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class ExtensionConvertorData extends ConvertorData {
    private Uint32 xid;

    private Uint64 datapathId;


    /**
     * Instantiates a new ExtensionConvertor data.
     *
     * @param version the version
     */
    public ExtensionConvertorData(final Uint8 version) {
        super(version);
    }

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public Uint32 getXid() {
        return xid;
    }

    /**
     * Sets xid.
     *
     * @param xid the xid
     */
    public void setXid(final Uint32 xid) {
        this.xid = xid;
    }

    /**
     * Gets datapath id.
     *
     * @return the datapath id
     */
    public Uint64 getDatapathId() {
        return datapathId;
    }

    /**
     * Sets datapath id.
     *
     * @param datapathId the datapath id
     */
    public void setDatapathId(final Uint64 datapathId) {
        this.datapathId = datapathId;
    }
}
