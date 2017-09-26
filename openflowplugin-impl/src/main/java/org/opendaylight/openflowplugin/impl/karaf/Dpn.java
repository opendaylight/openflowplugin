package org.opendaylight.openflowplugin.impl.karaf;

import java.io.Serializable;
import java.util.List;

public class Dpn implements Comparable<Dpn>,Serializable {
        private static final long serialVersionUID = 1L;

        private long m_nDpId;
        private String m_sDpnName;
        private List<String> m_listPorts;

        // TODO dpnState

        public Dpn(long nDpId, String sDpnName, List<String> listPorts) {
                super();
                m_nDpId = nDpId;
                m_sDpnName = sDpnName;
                m_listPorts = listPorts;
        }

        public long getDpId() {
                return m_nDpId;
        }

        public String getDpnName() {
                return m_sDpnName;
        }

        public List<String> getListPorts() {
                return m_listPorts;
        }

        public void setDpId(long m_nDpId) {
                this.m_nDpId = m_nDpId;
        }

        public void setDpnName(String m_sDpnName) {
                this.m_sDpnName = m_sDpnName;
        }

        public void setListPorts(List<String> m_listPorts) {
                this.m_listPorts = m_listPorts;
        }

        @Override
        public String toString() {
                return "Node [m_nDpId=" + m_nDpId + ", m_sDpnName=" + m_sDpnName + ", m_listPorts=" + m_listPorts + "]";
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
