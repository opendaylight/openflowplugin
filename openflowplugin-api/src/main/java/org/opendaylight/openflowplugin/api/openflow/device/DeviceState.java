/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 25.2.2015.
 */
public interface DeviceState {

    /**
     * @return id of encapsulated node
     */
    NodeId getNodeId();

    /**
     * @return {@link Node} instance identifier
     */
    KeyedInstanceIdentifier<Node, NodeKey> getNodeInstanceIdentifier();

    /**
     * @return the features of corresponding switch
     */
    GetFeaturesOutput getFeatures();

    /**
     * @return true if this session is valid
     */
    boolean isValid();

    /**
     * @param valid the valid to set
     */
    void setValid(boolean valid);

    /**
     * Returns a map containing all OFPhysicalPorts of this switch.
     *
     * @return The Map of OFPhysicalPort
     */
    Map<Long, PortGrouping> getPhysicalPorts();

    /**
     * Returns a map containing all bandwidths for all OFPorts of this switch.
     *
     * @return The Map of bandwidths for all OFPorts
     */
    Map<Long, Long> getPortsBandwidth();

    /**
     * Returns a Set containing all port IDs of this switch.
     *
     * @return The Set of port ID
     */
    Set<Long> getPorts();

    /**
     * Returns OFPhysicalPort of the specified portNumber of this switch.
     *
     * @param portNumber The port ID
     * @return OFPhysicalPort for the specified PortNumber
     */
    PortGrouping getPhysicalPort(Long portNumber);

    /**
     * Returns the bandwidth of the specified portNumber of this switch.
     *
     * @param portNumber the port ID
     * @return bandwidth
     */
    Long getPortBandwidth(Long portNumber);

    /**
     * Returns True if the port is enabled,
     *
     * @param portNumber
     * @return True if the port is enabled
     */
    boolean isPortEnabled(long portNumber);

    /**
     * Returns True if the port is enabled.
     *
     * @param port
     * @return True if the port is enabled
     */
    boolean isPortEnabled(PortGrouping port);

    /**
     * Returns a list containing all enabled ports of this switch.
     *
     * @return List containing all enabled ports of this switch
     */
    List<PortGrouping> getEnabledPorts();

    /**
     * Return node current OF protocol version
     *
     * @return
     */
    short getVersion();

    /**
     * @return seed value for random operations
     */
    int getSeed();

}
