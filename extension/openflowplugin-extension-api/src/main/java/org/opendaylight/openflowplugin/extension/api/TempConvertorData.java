/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

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
