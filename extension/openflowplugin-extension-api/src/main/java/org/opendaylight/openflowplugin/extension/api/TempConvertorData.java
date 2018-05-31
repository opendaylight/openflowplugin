package org.opendaylight.openflowplugin.extension.api;

import java.math.BigInteger;

public class TempConvertorData extends ConvertorData {
        private Long xid;

        private BigInteger datapathId;


        /**
         * Instantiates a new Packet out convertor data.
         *
         * @param version the version
         */
        public TempConvertorData(short version) {
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

        /**
         * Gets datapath id.
         *
         * @return the datapath id
         */
        public BigInteger getDatapathId() {
                return datapathId;
        }

        /**
         * Sets datapath id.
         *
         * @param datapathId the datapath id
         */
        public void setDatapathId(BigInteger datapathId) {
                this.datapathId = datapathId;
        }
}
