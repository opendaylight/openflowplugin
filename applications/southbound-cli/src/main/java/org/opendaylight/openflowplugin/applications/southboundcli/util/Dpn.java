/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli.util;

import java.io.Serializable;
import java.util.List;

public class Dpn implements Comparable<Dpn>,Serializable {
    private static final long serialVersionUID = 1L;

    private long dpnId;
    private String dpnName;
    private List<String> ports;

    public Dpn(long dpnId, String dpnName, List<String> ports) {
        this.dpnId = dpnId;
        this.dpnName = dpnName;
        this.ports = ports;
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

    public List<String> getPorts() {

        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    @Override
    public String toString() {
        return "Dpn [dpnId=" + dpnId + ", dpnName=" + dpnName + ", ports=" + ports + "]";
    }


    @Override
    public int compareTo(Dpn dpn) {
        return Long.compare(dpnId, dpn.getDpnId());
    }

}
