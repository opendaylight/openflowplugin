/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.md.core.session;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitchRegistration;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationQueueWrapper;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

public interface SessionContext {

    /**
     * return primary connection wrapper.
     */
    ConnectionConductor getPrimaryConductor();

    /**
     * return the features of corresponding switch.
     */
    GetFeaturesOutput getFeatures();

    /**
     * Auxiliary connections.
     * @param auxiliaryKey key under which the auxiliary conductor is stored
     * @return list of auxiliary connection wrappers
     */
    ConnectionConductor getAuxiliaryConductor(
            SwitchConnectionDistinguisher auxiliaryKey);

    /**
     * return entries of all auxiliary connections wrapped in conductors in this session.
     */
    Set<Entry<SwitchConnectionDistinguisher, ConnectionConductor>> getAuxiliaryConductors();

    /**
     * register new auxiliary connection wrapped in {@link ConnectionConductor}.
     *
     * @param auxiliaryKey key
     * @param conductor connection conductor
     */
    void addAuxiliaryConductor(SwitchConnectionDistinguisher auxiliaryKey,
                               ConnectionConductor conductor);

    /**
     * Remove conductor.
     * @param connectionCookie cookie
     * @return removed connectionConductor
     */
    ConnectionConductor removeAuxiliaryConductor(
            SwitchConnectionDistinguisher connectionCookie);

    /**
     * return true if this session is valid.
     */
    boolean isValid();

    /**
     * Setter.
     * @param valid the valid to set
     */
    void setValid(boolean valid);

    /**
     * return the sessionKey.
     */
    SwitchSessionKeyOF getSessionKey();

    /**
     * Returns a map containing all OFPhysicalPorts of this switch.
     *
     * @return The Map of OFPhysicalPort
     */
    @Deprecated
    Map<Long, PortGrouping> getPhysicalPorts();

    /**
     * Returns a map containing all bandwidths for all OFPorts of this switch.
     *
     * @return The Map of bandwidths for all OFPorts
     */
    @Deprecated
    Map<Long, Boolean> getPortsBandwidth();

    /**
     * Returns a Set containing all port IDs of this switch.
     *
     * @return The Set of port ID
     */
    @Deprecated
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
    Boolean getPortBandwidth(Long portNumber);

    /**
     * Returns True if the port is enabled.
     *
     * @param portNumber port number
     * @return True if the port is enabled
     */
    boolean isPortEnabled(long portNumber);

    /**
     * Returns True if the port is enabled.
     *
     * @param port port
     * @return True if the port is enabled
     */
    boolean isPortEnabled(PortGrouping port);

    /**
     * Returns a list containing all enabled ports of this switch.
     *
     * @return List containing all enabled ports of this switch
     */
    List<PortGrouping> getEnabledPorts();

    // TODO:: add listeners here, manager will set them and conductor use them

    /**
     * get message dispatch service to send the message to switch.
     *
     * @return the message service
     */
    IMessageDispatchService getMessageDispatchService();

    /**
     * return the unique xid for this session.
     * @return  xid
     */
    Long getNextXid();

    /**
     * Setter.
     * @param registration provider composite registration
     */
    void setProviderRegistration(ModelDrivenSwitchRegistration registration);

    /**
     * return provider composite registration.
     * @return ModelDrivenSwitchRegistration
     */
    ModelDrivenSwitchRegistration getProviderRegistration();

    /**
     * return seed value for random operations.
     * @return int
     */
    int getSeed();

    /**
     * return (wrapped) notification enqueue service - {@link NotificationQueueWrapper}.
     * @return NotificationEnqueuer
     */
    NotificationEnqueuer getNotificationEnqueuer();

    /**
     * Setter.
     * @param roleOnDevice role
     */
    void setRoleOnDevice(ControllerRole roleOnDevice);

    /**
     * return actual role.
     * @return role
     */
    ControllerRole getRoleOnDevice();
}
