/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.model.Interface.State;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IfType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.Set;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Default implementation of {@link InterfaceInfo}. Information is discovered
 * from the device and not configurable.
 *
 * @author Thomas Vachuska
 */
public class DefaultInterfaceInfo implements InterfaceInfo {

    private BigPortNumber id;
    private Set<State> state;
    private String name;
    private IfType type;
    private MacAddress mac;
    private ElementId hostedBy;
    private ElementId realizedByElement;
    private InterfaceId realizedByInterface;
    private Set<IpAddress> ipAddresses;

    /**
     * Constructs an Interface Info object.
     * @param hostedBy device hosting interface
     * @param id interface index
     */
    public DefaultInterfaceInfo(ElementId hostedBy, BigPortNumber id) {
        notNull(hostedBy);
        this.hostedBy = hostedBy;
        this.id = id;
    }

    /**
     * Modify the interface state
     *
     * @param state interface state
     * @return self, for chaining
     */
    public DefaultInterfaceInfo state(Set<State> state) {
        notNull(state);
        this.state = state;
        return this;
    }

    @Override
    public Set<State> state() {
        return state;
    }
    
    @Override
    public boolean isEnabled() {
        return state != null && !state.contains(Interface.State.DOWN);
    }
    
    @Override
    public boolean isBlocked() {
        return state != null && state.contains(Interface.State.BLOCKED);
    }

    /**
     * Modify the interface name
     *
     * @param name interface name
     * @return self, for chaining
     */
    public DefaultInterfaceInfo name(String name) {
        notNull(name);
        this.name = name;
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ElementId hostedBy() {
        return hostedBy;
    }

    /**
     * Modify the element realized by
     *
     * @param realizedBy Element (physical) that realizes this interface
     * @return self, for chaining
     */
    public DefaultInterfaceInfo realizedByElement(ElementId realizedBy) {
        this.realizedByElement = realizedBy;
        return this;
    }

    @Override
    public ElementId realizedByElement() {
        return realizedByElement;
    }

    /**
     * Modify the interface realized by
     *
     * @param realizedBy Interface (physical) that realizes this interface
     * @return self, for chaining
     */
    public DefaultInterfaceInfo realizedByInterface(InterfaceId realizedBy) {
        this.realizedByInterface = realizedBy;
        return this;
    }

    @Override
    public InterfaceId realizedByInterface() {
        return realizedByInterface;
    }

    /**
     * Modify the interface mac address
     *
     * @param mac Interface mac address
     * @return self, for chaining
     */
    public DefaultInterfaceInfo mac(MacAddress mac) {
        notNull(mac);
        this.mac = mac;
        return this;
    }

    @Override
    public MacAddress mac() {
        return mac;
    }

    /**
     * Modify the interface type
     *
     * @param type Interface type
     * @return self, for chaining
     */
    public DefaultInterfaceInfo type(IfType type) {
        notNull(type);
        this.type = type;
        return this;
    }

    @Override
    public IfType type() {
        return type;
    }

    /**
     * Modify the interface ip addresses
     *
     * @param ipAddresses Interface ip addresses
     * @return self, for chaining
     */
    public DefaultInterfaceInfo ipAddresses(Set<IpAddress> ipAddresses) {
        notNull(ipAddresses);
        this.ipAddresses = ipAddresses;
        return this;
    }

    @Override
    public Set<IpAddress> ipAddresses() {
        return ipAddresses;
    }

    @Override
    public BigPortNumber id() {
        return id;
    }

    @Override
    public String toString() {
        return "DefaultInterfaceInfo{" +
                "id=" + id +
                ", name=" + name +
                ", mac=" + mac +
                '}';
    }

}
