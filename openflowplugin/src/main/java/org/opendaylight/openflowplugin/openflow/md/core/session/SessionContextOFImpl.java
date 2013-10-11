/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDestinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * @author mirehak
 */
public class SessionContextOFImpl implements SessionContext {

    private GetFeaturesOutput features;
    private ConnectionConductor primaryConductor;
    private ConcurrentHashMap<Object, ConnectionConductor> auxiliaryConductors;

    /**
     * default ctor
     */
    public SessionContextOFImpl() {
        auxiliaryConductors = new ConcurrentHashMap<>();
    }

    @Override
    public ConnectionConductor getPrimaryConductor() {
        return primaryConductor;
    }

    @Override
    public ConnectionConductor getAuxiliaryConductor(
            SwitchConnectionDestinguisher auxiliaryKey) {
        return auxiliaryConductors.get(auxiliaryKey);
    }

    @Override
    public void addAuxiliaryConductor(
            SwitchConnectionDestinguisher auxiliaryKey,
            ConnectionConductor conductor) {
        auxiliaryConductors.put(auxiliaryKey, conductor);
    }

    @Override
    public Iterator<ConnectionConductor> getAuxiliaryConductors() {
        return auxiliaryConductors.values().iterator();
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
            SwitchConnectionDestinguisher connectionCookie) {
        return auxiliaryConductors.remove(connectionCookie);
    }
}
