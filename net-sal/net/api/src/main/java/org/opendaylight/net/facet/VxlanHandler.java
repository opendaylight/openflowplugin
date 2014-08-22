/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.HandlerFacet;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.Vni;
import org.opendaylight.net.model.StatusFilter;
import org.opendaylight.net.model.TunnelIndex;

import java.util.Set;

/**
 * Abstraction for provisioning and managing VXLAN tunnels on switches.
 * It provides ability to do the following:
 * <ul>
 *   <li>create, destroy & query tunnels
 *   <li>manage VLAN & VNI bindings
 *   <li>manage tunnel and VNI bindings
 *   <li>enable/disable MAC learning
 *   <li>enable/disable flood behaviour
 *   <li>manage static MAC learning for a tunnel/VNI
 * </ul>
 */

public interface VxlanHandler extends HandlerFacet {

    /**
     * Creates a new VxLan tunnel and returns the resulting interface.
     *
     * @param tunnelIndex specifies the suggested tunnel interface index (as a positive integer) to be used for the tunnel;
     *        device may disregard the suggestion and issue one itself; interface index should not be already in use by other tunnels
     * @param srcIp IPv4 address of the local VTEP
     * @param dstIp IPv4 address of the remote VTEP
     * @return the tunnel interface index of the interface that represents the
     *      newly created tunnel
     * @throws IllegalArgumentException if tunnel interface index is already in use 
     */
    TunnelIndex createTunnel(TunnelIndex tunnelIndex,
                             IpAddress srcIp, IpAddress dstIp);
 
    /**
     * Destroys the tunnel identified by the specified tunnel interface index.
     *
     * @param tunnelIndex tunnel interface index
     * @throws IllegalArgumentException if tunnel interface index not correspond to a tunnel 
     * @throws OperationFailureException if the operation failed for some reason
     */
    void destroyTunnel(TunnelIndex tunnelIndex);

    /**
     * Get a set of tunnel interface indexes that represent tunnels created on the device.
     *
     * @param statusFilter status filter (ANY, UP, DOWN)
     * @return set of tunnel interface indexes
     */
    Set<TunnelIndex> getTunnels(StatusFilter statusFilter);

    /**
     * Retrieves the VXLAN tunnel descriptor for the tunnel with the specified interface index.
     *
     * @param tunnelIndex interface index of the tunnel
     * @return tunnel descriptor
     */
    Vxlan getTunnel(TunnelIndex tunnelIndex);


    /**
     * Binds the given set of VLAN/port pairs to the specified VNI.
     * The operation may fail-fast, meaning that no attempt will be made to bind all VLAN port pairs. 
     *
     * @param vni VNI to bind to
     * @param vlanPorts set of VLAN ports
     * @throws OperationFailureException if the operation failed for some reason
     *    // TODO: consider specialized exception to allow retrieval of the fail-point
     */
    void bindVlansToVNI(Vni vni, Set<VlanPortPair> vlanPorts);

    /**
     * Unbinds the given set of VLAN/port pairs from the specified VNI.
     * The operation may fail-fast, meaning that no attempt will be made to unbind all VLAN port pairs. 
     *
     * @param vni VNI to unbind from
     * @param vlanPorts set of VLAN ports
     * @throws OperationFailureException if the operation failed for some reason
     *    // TODO: consider specialized exception to allow retrieval of the fail-point
     */
    void unbindVlansFromVNI(Vni vni, Set<VlanPortPair> vlanPorts);

    /**
     * Retrieves the set of VLAN/port pairs bound to the specified VNI.
     *
     * @param vni VNI a tunnel is bound to
     * @return set of VLAN/port pairs
     */
    Set<VlanPortPair> getVlanBindings(Vni vni);

   
    /**
     * Binds the given set of tunnels to the specified VNI.
     * The operation may fail-fast, meaning that no attempt will be made to bind all tunnels. 
     *
     * @param vni VNI to bind to
     * @param tunnelIndexes set of tunnel indexes
     * @throws OperationFailureException if the operation failed for some reason
     *    // TODO: consider specialized exception to allow retrieval of the fail-point
     */
    void bindTunnelsToVNI(Vni vni, Set<TunnelIndex> tunnelIndexes);
 
    /**
     * Unbinds the given set of tunnels from the specified VNI.
     * The operation may fail-fast, meaning that no attempt will be made to unbind all tunnels. 
     *
     * @param vni VNI to unbind from
     * @param tunnelIndexes set of tunnel indexes
     * @throws OperationFailureException if the operation failed for some reason
     *    // TODO: consider specialized exception to allow retrieval of the fail-point
     */
    void unbindTunnelsFromVNI(Vni vni, Set<TunnelIndex> tunnelIndexes);
 
    /**
     * Retrieves the set of tunnel indexes bound to the specified VNI.
     *
     * @param vni VNI a tunnel is bound to
     * @return set of tunnel indexes
     */
    Set<TunnelIndex> getTunnelBindings(Vni vni);


    /**
     * Enables or disables learning of remote MAC addresses on the VXLAN tunnels.
     *
     * @param on true to enable; false to disable
     */
    void setMacLearning(boolean on);

    /**
     * Enables or disables flooding of packets with unknown destination MAC addresses on a given VNI.
     *
     * @param vni virtual network instance
     * @param on true to enable; false to disable
     *    // TODO: Is this strictly required?  Or should it go into a different facet?
     */
    void setFlooding(Vni vni, boolean on);

    /**
     * Adds the given set of remote MAC addresses to the specified tunnel/VNI; facilitates static learning.
     *
     * @param tunnelIndex tunnel index
     * @param vni VNI 
     * @param macAddresses set of MAC addresses to be added
     */
    void addMacAddresses(TunnelIndex tunnelIndex, Vni vni, Set<MacAddress> macAddresses);

    /**
     * Removes the given set of remote MAC addresses from the specified tunnel/VNI; facilitates static learning.
     *
     * @param tunnelIndex tunnel index 
     * @param vni VNI 
     * @param macAddresses set of MAC addresses to be removed
     */
    void removeMacAddresses(TunnelIndex tunnelIndex, Vni vni, Set<MacAddress> macAddresses);

    /**
     * Clears all remote MAC addresses from the specified tunnel/VNI; facilitates static learning.
     *
     * @param tunnelIndex tunnel index 
     * @param vni VNI 
     */
   void clearMacAddresses(TunnelIndex tunnelIndex, Vni vni);

    /**
     * Gets the set of remote MAC addresses configured on the specified tunnel/VNI; facilitates static learning.
     *
     * @param tunnelIndex tunnel index
     * @param vni VNI 
     * @return set of MAC addresses
     */
    Set<MacAddress> getMacAddresses(TunnelIndex tunnelIndex, Vni vni);

}
