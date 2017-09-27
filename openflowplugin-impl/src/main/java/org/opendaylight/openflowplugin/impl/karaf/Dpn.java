package org.opendaylight.openflowplugin.impl.karaf;

import java.io.Serializable;
import java.util.List;

public class Dpn implements Comparable<Dpn>,Serializable {
    private static final long serialVersionUID = 1L;

    private long dpId;
    private String dpnName;
    private List<String> ports;

    // TODO dpnState

    public Dpn(long nDpId, String sDpnName, List<String> ports) {
        this.dpId = nDpId;
        this.dpnName = sDpnName;
        this.ports = ports;
    }

    public long getDpId() {
        return dpId;
    }

    public String getDpnName() {
        return dpnName;
    }

    public List<String> getPorts() {

        return ports;
    }

    public void setDpId(long m_nDpId) {
        this.dpId = m_nDpId;
    }

    public void setDpnName(String m_sDpnName) {
        this.dpnName = m_sDpnName;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    @Override
    public String toString() {
        return "Dpn [dpId=" + dpId + ", dpnName=" + dpnName + ", ports=" + ports + "]";
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
            return Long.compare(dpId, dpn.getDpId());
        }
        return res;
    }

}
