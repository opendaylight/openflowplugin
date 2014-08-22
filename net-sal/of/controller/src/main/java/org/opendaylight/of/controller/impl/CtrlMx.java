/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.*;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.ResourceUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import static org.opendaylight.of.controller.OpenflowEventType.LISTENER_ADDED;
import static org.opendaylight.of.controller.OpenflowEventType.LISTENER_REMOVED;
import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Implements the {@link ControllerMx} interface, providing
 * the management API functionality.
 *
 * @author Simon Hunt
 */
class CtrlMx implements ControllerMx {
    // =====================================================================
    // === Management API

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            CtrlMx.class, "ctrlMx");

    private static final String E_REG_ALREADY = RES.getString("e_reg_already");

    /** Management API synchronization lock. */
    private static final Object MX_LOCK = new Object();

    private final ListenerManager manager;
    private final OpenflowController controller;

    /** Our registration listener. */
    private RegistrationListener regListener;
    // TODO : Need to wrap this puppy in a RegHandler.

    /** Constructs our management API implementing class.
     *
     * @param manager the manager
     *
     */
    public CtrlMx(ListenerManager manager) {
        this.manager = manager;
        this.controller = manager.getController();
    }


    @Override
    public Set<OpenflowListener<?>> getAllListeners() {
        Set<OpenflowListener<?>> all = new HashSet<OpenflowListener<?>>();
        all.addAll(manager.getDpListeners());
        all.addAll(manager.getMsgListeners());
        return Collections.unmodifiableSet(all);
    }

    @Override
    public void setRegistrationListener(RegistrationListener listener) {
        notNull(listener);
        synchronized (MX_LOCK) {
            if (regListener != null)
                throw new IllegalStateException(E_REG_ALREADY +
                        regListener.getClass());
            regListener = listener;
        }
    }

    @Override
    public void clearRegistrationListener(RegistrationListener listener) {
        notNull(listener);
        synchronized (MX_LOCK) {
            if (regListener == listener)
                regListener = null;
        }
    }

    @Override
    public TxRxControl getTxRxControl() {
        return controller.getTxRxControl();
    }

    @Override
    public Set<DataPathDetails> getAllDataPathDetails() {
        return controller.getAllDataPathDetails();
    }

    @Override
    public DataPathDetails getDataPathDetails(DataPathId dpid) {
        return controller.getDataPathDetails(dpid);
    }

    @Override
    public void startIOProcessing() {
        controller.startIOProcessing();
    }

    @Override
    public void stopIOProcessing() {
        controller.stopIOProcessing();
    }

    @Override
    public int getOpenflowListenPort() {
        return controller.getOpenflowPort();
    }

    /** Notifies the registration listener of an addition or removal of
     * an openflow listener.
     *
     * @param o the openflow listener
     * @param added true if added; false if removed
     */
    void notifyRegListener(OpenflowListener<?> o, boolean added) {
        synchronized (MX_LOCK) {
            if (regListener != null) {
                OpenflowEventType type = added ? LISTENER_ADDED
                        : LISTENER_REMOVED;
                ListenerEvent ev = new ListenerEvt(type, o);
                // TODO: Consider adding a reg event queue (RegQ) and plumb that
                //  rather than invoking the listener's callback in this thread
                regListener.event(ev);
            }
        } // sync
    }

    // =====================================================================
}