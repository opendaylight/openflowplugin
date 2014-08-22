/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.DataPathId;

import java.util.Set;

/**
 * Provides a management API to the OpenFlow Core Controller.
 * <p>
 * This interface provides for:
 * <ul>
 *     <li>
 *         requesting a list of currently registered {@link OpenflowListener}s
 *     </li>
 *     <li>
 *         setting a {@link RegistrationListener} to be notified when
 *         {@link OpenflowListener}s are added to or removed from the
 *         controller
 *     </li>
 *     <li>
 *         the acquisition of a {@link TxRxControl} from which
 *         the recording of transmitted and received
 *         OpenFlow messages can be enabled.
 *     </li>
 * </ul>
 *
 * @author Simon Hunt
 */
public interface ControllerMx {

    /** Returns the set of all currently registered {@link OpenflowListener}s.
     *
     * @return the set of currently registered OpenFlow listeners
     */
    Set<OpenflowListener<?>> getAllListeners();

    /** Sets the specified registration listener on the controller.
     * If a registration listener is already set, an exception is thrown.
     *
     * @param listener the listener to be set
     * @throws NullPointerException if listener is null
     * @throws IllegalStateException if a listener is already set
     */
    void setRegistrationListener(RegistrationListener listener);

    /** Clears the registration listener from the controller.
     * The listener that was set must be passed to this method, to
     * prevent anyone from simply clearing the listener. If the
     * listener is already cleared, or is not the currently set listener,
     * this method does nothing.
     *
     * @param listener the listener to be cleared
     * @throws NullPointerException if listener is null
     */
    void clearRegistrationListener(RegistrationListener listener);

    /** Returns a reference to the TX/RX message queue control.
     *
     * @return the TX/RX queue control
     */
    TxRxControl getTxRxControl();

    /** Returns detailed information describing each of the OpenFlow
     * datapaths connected to the controller, including information about
     * auxiliary connections.
     *
     * @return detailed information about all connected datapaths
     */
    Set<DataPathDetails> getAllDataPathDetails();

    /** Returns detailed information describing a specific OpenFlow
     * datapath, including information about its auxiliary connections.
     * This may be null, if no such OpenFlow datapath is currently connected.
     *
     * @param dpid the datapath id
     * @return detailed information describing the corresponding datapath
     * @throws NullPointerException if dpid is null
     */
    DataPathDetails getDataPathDetails(DataPathId dpid);

    /**
     * Starts the controller IO Processing loop.
     */
    void startIOProcessing();

    /**
     * Stops the controller IO Processing loop.
     */
    void stopIOProcessing();

    /**
     * Returns the configured Open Flow port
     * @return the open flow port
     */
    int getOpenflowListenPort();

    // FIXME: add reset stats method:
    /** Resets the message handling statistics, setting counts to zero. */
//    void resetStats();

}
