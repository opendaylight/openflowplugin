/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.session;

import java.math.BigInteger;

public class SwitchSessionKeyOF {

    private BigInteger datapathId;

    /**
     * default ctor.
     */
    public SwitchSessionKeyOF() {
        // NOOP
    }

    /**
     * Setter.
     * @param datapathId the datapathId to set
     */
    public void setDatapathId(BigInteger datapathId) {
        this.datapathId = datapathId;
    }

    /**
     * Getter.
     * @return the datapathId
     */
    public byte[] getId() {
        return datapathId.toByteArray();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((datapathId == null) ? 0 : datapathId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SwitchSessionKeyOF other = (SwitchSessionKeyOF) obj;
        if (datapathId == null) {
            if (other.datapathId != null) {
                return false;
            }
        } else if (!datapathId.equals(other.datapathId)) {
            return false;
        }
        return true;
    }
}
