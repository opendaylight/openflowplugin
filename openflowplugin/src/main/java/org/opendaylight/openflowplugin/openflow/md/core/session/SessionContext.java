/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;


import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
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
            SwitchConnectionDistinguisher auxiliaryKey);

    /**
     * @return entries of all auxiliary connections wrapped in conductors in this session
     */
    public Set<Entry<SwitchConnectionDistinguisher, ConnectionConductor>> getAuxiliaryConductors();

    /**
     * register new auxiliary connection wrapped in {@link ConnectionConductor}
     *
     * @param auxiliaryKey
     * @param conductor
     */
    public void addAuxiliaryConductor(SwitchConnectionDistinguisher auxiliaryKey,
            ConnectionConductor conductor);

    /**
     * @param connectionCookie
     * @return removed connectionConductor
     */
    public ConnectionConductor removeAuxiliaryConductor(
            SwitchConnectionDistinguisher connectionCookie);

    /**
     * @return true if this session is valid
     */
    public boolean isValid();

    /**
     * @param valid the valid to set
     */
    public void setValid(boolean valid);

    /**
     * @return the sessionKey
     */
    public SwitchConnectionDistinguisher getSessionKey();

    // TODO:: add listeners here, manager will set them and conductor use them

    /**
     *  get message service to send the message to switch
     *
     * @return the message service
     */
    public IMessageService getMessageService();

   /**
    * @return the unique xid for this session
    */
    public Long getNextXid();



}
