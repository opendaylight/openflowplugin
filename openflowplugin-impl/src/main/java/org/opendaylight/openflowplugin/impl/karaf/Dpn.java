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

    private long m_nDpId;
    private String m_sDpnName;

    public Dpn(long nDpId, String sDpnName) {
        super();
        m_nDpId = nDpId;
        m_sDpnName = sDpnName;
    }

    public long getDpId() {
        return m_nDpId;
    }

    public String getDpnName() {
        return m_sDpnName;
    }

    public void setDpId(long m_nDpId) {
        this.m_nDpId = m_nDpId;
    }

    public void setDpnName(String m_sDpnName) {
        this.m_sDpnName = m_sDpnName;
    }

    @Override
    public String toString() {
        return "Node [m_nDpId=" + m_nDpId + ", m_sDpnName=" + m_sDpnName + "]";
    }

    @Override
    public int compareTo(Dpn dpn) {
        int res = 0;
        if (m_sDpnName == null && dpn.getDpnName() == null) {
            res = 0;
        } else if (m_sDpnName == null) {
            res = 1;
        } else if (dpn.getDpnName() == null) {
            res = -1;
        } else {
            res = m_sDpnName.compareTo(dpn.getDpnName());
        }
        if (res == 0) {
            return Long.compare(m_nDpId, dpn.getDpId());
        }
        return res;
    }

}
