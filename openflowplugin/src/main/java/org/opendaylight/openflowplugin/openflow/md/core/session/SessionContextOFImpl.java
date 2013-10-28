/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

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

    /**
     * default ctor
     */
    public SessionContextOFImpl() {
        auxiliaryConductors = new ConcurrentHashMap<>();
        mdService = new MessageDispatchServiceImpl(this);
        xid = new AtomicLong();
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
}
