/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import java.io.Serializable;

public class Dpn implements Comparable<Dpn>,Serializable {
    private static final long serialVersionUID = 1L;

    private long dpnId;
    private String dpnName;

    public Dpn(long dpnId, String dpnName) {
        this.dpnId = dpnId;
        this.dpnName = dpnName;
    }

    public long getDpnId() {
        return dpnId;
    }

    public String getDpnName() {
        return dpnName;
    }

    public void setDpnId(long dpnId) {
        this.dpnId = dpnId;
    }

    public void setDpnName(String dpnName) {
        this.dpnName = dpnName;
    }

    @Override
    public String toString() {
        return "Node [dpnId=" + dpnId + ", dpnName=" + dpnName + "]";
    }

    @Override
    public int compareTo(Dpn dpn) {
        int res = 0;
        if (dpnName == null && dpn.getDpnName() == null) {
            res = 0;
        } else if (dpnName == null) {
            res = 1;
        } else if (dpn.getDpnName() == null) {
            res = -1;
        } else {
            res = dpnName.compareTo(dpn.getDpnName());
        }
        if (res == 0) {
            return Long.compare(dpnId, dpn.getDpnId());
        }
        return res;
    }

}
