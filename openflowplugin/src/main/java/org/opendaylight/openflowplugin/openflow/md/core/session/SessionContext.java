/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.Iterator;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDestinguisher;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * @author mirehak
 */
public interface SessionContext {

    /**
     * @return primary connection wrapper
     */
    public ConnectionConductor getPrimaryConductor();

    /**
     * @return the features of corresponding switch
     */
    public GetFeaturesOutput getFeatures();

    /**
     * @param auxiliaryKey
     *            key under which the auxiliary conductor is stored
     * @return list of auxiliary connection wrappers
     */
    public ConnectionConductor getAuxiliaryConductor(
            SwitchConnectionDestinguisher auxiliaryKey);

    /**
     * @return iterator through all auxiliary connections wrapped in conductors
     */
    public Iterator<ConnectionConductor> getAuxiliaryConductors();

    /**
     * register new auxiliary connection wrapped in {@link ConnectionConductor}
     *
     * @param auxiliaryKey
     * @param conductor
     */
    public void addAuxiliaryConductor(SwitchConnectionDestinguisher auxiliaryKey,
            ConnectionConductor conductor);

    /**
     * @param connectionCookie
     * @return removed connectionConductor
     */
    public ConnectionConductor removeAuxiliaryConductor(
            SwitchConnectionDestinguisher connectionCookie);

    // TODO:: add listeners here, manager will set them and conductor use them

}
