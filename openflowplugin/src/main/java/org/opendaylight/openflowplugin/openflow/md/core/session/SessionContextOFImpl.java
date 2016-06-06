/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.openflowplugin.api.openflow.md.ModelDrivenSwitchRegistration;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.NotificationEnqueuer;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.IMessageDispatchService;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SwitchSessionKeyOF;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ControllerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

/**
 * @author mirehak
 */
public class SessionContextOFImpl implements SessionContext {

    private GetFeaturesOutput features;
    private ConnectionConductor primaryConductor;
    private NotificationEnqueuer notificationEnqueuer;
    private ConcurrentHashMap<SwitchConnectionDistinguisher, ConnectionConductor> auxiliaryConductors;
    private boolean valid;
    private SwitchSessionKeyOF sessionKey;
    private IMessageDispatchService mdService;
    private final AtomicLong xid;
    private final Map<Long, PortGrouping> physicalPorts;
    private final Map<Long, Boolean> portBandwidth;
    private ModelDrivenSwitchRegistration providerRegistration;
    private int seed;
    private ControllerRole roleOnDevice = ControllerRole.OFPCRROLEEQUAL;


    /**
     * default ctor
     */
    public SessionContextOFImpl() {
        auxiliaryConductors = new ConcurrentHashMap<>();
        mdService = new MessageDispatchServiceImpl(this);
        xid = new AtomicLong();
        this.physicalPorts = new HashMap<Long, PortGrouping>();
        this.portBandwidth = new HashMap<Long, Boolean>();
    }

    @Override
    public ConnectionConductor getPrimaryConductor() {
        return primaryConductor;
    }

    @Override
    public ConnectionConductor getAuxiliaryConductor(
            SwitchConnectionDistinguisher auxiliaryKey) {
        return auxiliaryConductors.get(auxiliaryKey);
    }

    @Override
    public void addAuxiliaryConductor(
            SwitchConnectionDistinguisher auxiliaryKey,
            ConnectionConductor conductor) {
        auxiliaryConductors.put(auxiliaryKey, conductor);
    }

    @Override
    public Set<Entry<SwitchConnectionDistinguisher, ConnectionConductor>> getAuxiliaryConductors() {
        return Collections.unmodifiableSet(auxiliaryConductors.entrySet());
    }

    @Override
    public GetFeaturesOutput getFeatures() {
        return features;
    }

    /**
     * @param features
     *            the features to set
     */
    public void setFeatures(GetFeaturesOutput features) {
        this.features = features;
    }

    /**
     * @param primaryConductor
     *            the primaryConductor to set
     */
    public void setPrimaryConductor(ConnectionConductor primaryConductor) {
        this.primaryConductor = primaryConductor;
    }

    @Override
    public ConnectionConductor removeAuxiliaryConductor(
            SwitchConnectionDistinguisher connectionCookie) {
        return auxiliaryConductors.remove(connectionCookie);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @param sessionKey the sessionKey to set
     */
    public void setSessionKey(SwitchSessionKeyOF sessionKey) {
        this.sessionKey = sessionKey;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public SwitchSessionKeyOF getSessionKey() {
        return sessionKey;
    }

    @Override
    public IMessageDispatchService getMessageDispatchService() {
        return mdService;
    }

    @Override
    public Long getNextXid() {
        return xid.incrementAndGet();
    }

    @Override
    public Map<Long, PortGrouping> getPhysicalPorts() {
        return this.physicalPorts;
    }

    @Override
    public Map<Long, Boolean> getPortsBandwidth() {
        return this.portBandwidth;
    }

    @Override
    public Set<Long> getPorts() {
        return this.physicalPorts.keySet();
    }

    @Override
    public PortGrouping getPhysicalPort(Long portNumber) {
        return this.physicalPorts.get(portNumber);
    }

    @Override
    public Boolean getPortBandwidth(Long portNumber) {
        return this.portBandwidth.get(portNumber);
    }

    @Override
    public boolean isPortEnabled(long portNumber) {
        return isPortEnabled(physicalPorts.get(portNumber));
    }

    @Override
    public boolean isPortEnabled(PortGrouping port) {
        if (port == null) {
            return false;
        }
        if (port.getConfig().isPortDown()) {
            return false;
        }
        if (port.getState().isLinkDown()) {
            return false;
        }
        if (port.getState().isBlocked()) {
            return false;
        }
        return true;
    }

    @Override
    public List<PortGrouping> getEnabledPorts() {
        List<PortGrouping> result = new ArrayList<PortGrouping>();
        synchronized (this.physicalPorts) {
            for (PortGrouping port : physicalPorts.values()) {
                if (isPortEnabled(port)) {
                    result.add(port);
                }
            }
        }
        return result;
    }

    @Override
    public void setProviderRegistration(ModelDrivenSwitchRegistration providerRegistration) {
        this.providerRegistration = providerRegistration;
    }

    @Override
    public ModelDrivenSwitchRegistration getProviderRegistration() {
        return providerRegistration;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    /**
     * @param notificationEnqueuer the notificationEnqueuer to set
     */
    public void setNotificationEnqueuer(
            NotificationEnqueuer notificationEnqueuer) {
        this.notificationEnqueuer = notificationEnqueuer;
    }

    @Override
    public NotificationEnqueuer getNotificationEnqueuer() {
        return notificationEnqueuer;
    }

    /**
     * @return the roleOnDevice
     */
    @Override
    public ControllerRole getRoleOnDevice() {
        return roleOnDevice;
    }

    /**
     * @param roleOnDevice the roleOnDevice to set
     */
    @Override
    public void setRoleOnDevice(ControllerRole roleOnDevice) {
        Preconditions.checkNotNull("Proposed controller role can not be empty.", roleOnDevice);
        this.roleOnDevice = roleOnDevice;
    }
}
