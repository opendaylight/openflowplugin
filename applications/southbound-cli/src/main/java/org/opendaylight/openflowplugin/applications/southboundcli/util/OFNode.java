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

public class OFNode implements Comparable<OFNode>,Serializable {
    private static final long serialVersionUID = 1L;

    private Long nodeId;
    private String nodeName;
    private List<String> ports;

    public OFNode(final Long nodeId, final String nodeName) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }

    public OFNode(final long nodeId, final String nodeName, final List<String> ports) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.ports = ports;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<String> getPorts() {
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    @Override
    public String toString() {
        return "OFNode [nodeId=" + nodeId + ", nodeName=" + nodeName + ", ports=" + ports + "]";
    }

    @Override
    public int compareTo(OFNode node) {
        return Long.compare(nodeId, node.getNodeId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nodeId.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        OFNode ofNode = (OFNode) object;
        return nodeId != null ? nodeId.equals(ofNode.nodeId) : ofNode.nodeId == null;
    }
}
