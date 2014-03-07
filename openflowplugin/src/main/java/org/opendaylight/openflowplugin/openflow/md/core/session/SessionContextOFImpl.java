/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;

/**
 * @author mirehak
 */
public class SessionContextOFImpl implements SessionContext {

    private GetFeaturesOutput features;
    private ConnectionConductor primaryConductor;
    private ConcurrentHashMap<SwitchConnectionDistinguisher, ConnectionConductor> auxiliaryConductors;
    private boolean valid;
    private SwitchConnectionDistinguisher sessionKey;
    private IMessageDispatchService mdService;
    private final AtomicLong xid;
    private final Map<Long, PortGrouping> physicalPorts;
    private final Map<Long, Boolean> portBandwidth;
    public static Cache<TransactionKey, Object> bulkTransactionCache = CacheBuilder.newBuilder().expireAfterWrite(10000, TimeUnit.MILLISECONDS).concurrencyLevel(1).build();


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
    public Cache<TransactionKey, Object> getbulkTransactionCache() {
        return bulkTransactionCache;
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
    public void setSessionKey(SwitchConnectionDistinguisher sessionKey) {
        this.sessionKey = sessionKey;
    }

    @Override
    public SwitchConnectionDistinguisher getSessionKey() {
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
}
