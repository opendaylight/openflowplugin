/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.device.DeviceEvent;
import org.opendaylight.util.net.IfType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.net.device.DeviceEvent.Type.INTERFACE_STATE_CHANGED;
import static org.opendaylight.net.device.DeviceEvent.Type.INTERFACE_UPDATED;
import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Default implementation of {@link Interface}.  The values are discovered from
 * the device and none are configurable.
 *
 * @author Thomas Vachuska
 */
public class DefaultInterface implements Interface {

    private static final String E_ID_NOT_SAME = "Interface ID must be the same";

    private final InterfaceId id;
    private final ElementId hostedBy;
    private final ElementId realizedByElement;
    private final InterfaceId realizedByInterface;

    private Set<State> state;
    private String name;
    private MacAddress mac;
    private Set<IpAddress> ipAddresses;
    private IfType type;

//    private InterfaceInfo info;

    // TODO: Here is an example of relying on *Info for long-term state. BAD!

    /**
     * Constructor to combine id and info.
     *
     * @param id   interface id
     * @param info interface info from device
     */
    public DefaultInterface(InterfaceId id, InterfaceInfo info) {
        notNull(id, info);
        this.id = id;
        this.hostedBy = info.hostedBy();
        this.realizedByElement = info.realizedByElement();
        this.realizedByInterface = info.realizedByInterface();
        setInfo(info);
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

    @Override
    public InterfaceId id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ElementId hostedBy() {
        return hostedBy;
    }

    @Override
    public ElementId realizedByElement() {
        return realizedByElement;
    }

    @Override
    public InterfaceId realizedByInterface() {
        return realizedByInterface;
    }

    @Override
    public MacAddress mac() {
        return mac;
    }

    @Override
    public IfType type() {
        return type;
    }

    @Override
    public Set<IpAddress> ipAddresses() {
        return ipAddresses;
    }

    /**
     * Returns the backing info descriptor.
     *
     * @param info new device interface info descriptor
     * @return device event type corresponding to the change; null if no change
     */
    public DeviceEvent.Type setInfo(InterfaceInfo info) {
        notNull(info);
        if (!info.id().equals(id.port()) || !info.hostedBy().equals(hostedBy()))
            throw new IllegalArgumentException(E_ID_NOT_SAME);

        // Inspect the new vs the old and determine whether there is any change
        boolean stateChanged = !isEqual(state, info.state());
        boolean changed = compareInfo(info);

        name = info.name();
        state = info.state() != null ? new HashSet<>(info.state()) : null;
        type = info.type();
        mac = info.mac();
        ipAddresses = info.ipAddresses() != null ? new HashSet<>(info.ipAddresses()) : null;

        return stateChanged ? INTERFACE_STATE_CHANGED :
                changed ? INTERFACE_UPDATED : null;
    }

    private boolean compareInfo(InterfaceInfo info) {
        // FIXME: complete the comparison
        return !(isEqual(type, info.type()) &&
                isEqual(mac, info.mac()) &&
                isEqual(ipAddresses, info.ipAddresses()) &&
                isEqual(name, info.name())
        );
    }

    private static <T> boolean isEqual(T oldField, T newField) {
        return (oldField == null && newField == null) ||
                oldField != null && oldField.equals(newField);
    }

    @Override
    public String toString() {
        return "DefaultInterface{" +
                "id=" + id +
                ", hostedBy=" + hostedBy +
                ", state=" + state +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", mac=" + mac +
                ", ipAddresses=" + ipAddresses +
                ", realizedByElement=" + realizedByElement +
                ", realizedByInterface=" + realizedByInterface +
                '}';
    }

}
