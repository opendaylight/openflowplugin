/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.cache.Cache;

import org.opendaylight.openflowplugin.openflow.md.ModelDrivenSwitch;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yangtools.concepts.CompositeObjectRegistration;

/**
 * @author mirehak
 */
public interface SessionContext {

    /**
     * @return primary connection wrapper
     */
    ConnectionConductor getPrimaryConductor();

    /**
     * @return the features of corresponding switch
     */
    GetFeaturesOutput getFeatures();

    /**
     * @param auxiliaryKey
     *            key under which the auxiliary conductor is stored
     * @return list of auxiliary connection wrappers
     */
    ConnectionConductor getAuxiliaryConductor(
            SwitchConnectionDistinguisher auxiliaryKey);

    /**
     * @return entries of all auxiliary connections wrapped in conductors in this session
     */
    Set<Entry<SwitchConnectionDistinguisher, ConnectionConductor>> getAuxiliaryConductors();

    /**
     * register new auxiliary connection wrapped in {@link ConnectionConductor}
     *
     * @param auxiliaryKey
     * @param conductor
     */
    void addAuxiliaryConductor(SwitchConnectionDistinguisher auxiliaryKey,
            ConnectionConductor conductor);

    /**
     * @param connectionCookie
     * @return removed connectionConductor
     */
    ConnectionConductor removeAuxiliaryConductor(
            SwitchConnectionDistinguisher connectionCookie);

    /**
     * @return true if this session is valid
     */
    boolean isValid();

    /**
     * @param valid the valid to set
     */
    void setValid(boolean valid);

    /**
     * @return the sessionKey
     */
    SwitchSessionKeyOF getSessionKey();

    /**
     * Returns a map containing all OFPhysicalPorts of this switch.
     * @return The Map of OFPhysicalPort
     */
    Map<Long, PortGrouping> getPhysicalPorts();
    
    /**
     * Returns a map containing all bandwidths for all OFPorts of this switch.
     * @return The Map of bandwidths for all OFPorts
     */
    Map<Long, Boolean> getPortsBandwidth();

    /**
     * Returns a Set containing all port IDs of this switch.
     * @return The Set of port ID
     */
    Set<Long> getPorts();
    
    /**
     * @return the Object for this session xId
     */
    Cache<TransactionKey, Object> getbulkTransactionCache();

    /**
     * Returns OFPhysicalPort of the specified portNumber of this switch.
     * @param portNumber The port ID
     * @return OFPhysicalPort for the specified PortNumber
     */
    PortGrouping getPhysicalPort(Long portNumber);

    /**
     * Returns the bandwidth of the specified portNumber of this switch.
     * @param portNumber the port ID
     * @return bandwidth
     */
    Boolean getPortBandwidth(Long portNumber);

    /**
     * Returns True if the port is enabled,
     * @param portNumber
     * @return True if the port is enabled
     */
    boolean isPortEnabled(long portNumber);

    /**
     * Returns True if the port is enabled.
     * @param port
     * @return True if the port is enabled
     */
    boolean isPortEnabled(PortGrouping port);

    /**
     * Returns a list containing all enabled ports of this switch.
     * @return List containing all enabled ports of this switch
     */
    List<PortGrouping> getEnabledPorts();

    // TODO:: add listeners here, manager will set them and conductor use them

    /**
     *  get message dispatch service to send the message to switch
     *
     * @return the message service
     */
    IMessageDispatchService getMessageDispatchService();

   /**
    * @return the unique xid for this session
    */
    Long getNextXid();

    /**
     * @param registration provider composite registration
     */
    void setProviderRegistration(CompositeObjectRegistration<ModelDrivenSwitch> registration);

    /**
     * @return provider composite registration
     */
    CompositeObjectRegistration<ModelDrivenSwitch> getProviderRegistration();
    
    /**
     * @return seed value for random operations
     */
    int getSeed();
}
