/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

/**
 * Base class for handling datapath mementos.
 *
 * @author Simon Hunt
 */
public abstract class DataPathUtils {

    /** Attaches the specified datapath info (memento) to the given
     * datapath ID instance.
     *
     * @param dpid the datapath ID
     * @param dpi the datapath info to attach
     */
    protected void attachMemento(DataPathId dpid, Object dpi) {
        dpid.memento = dpi;
    }

    /** Removes the reference to the datapath info (memento) from the
     * given datapath ID instance.
     *
     * @param dpid the datapath ID
     */
    protected void detachMemento(DataPathId dpid) {
        dpid.memento = null;
    }

    /** Returns the datapath info (memento) associated with this
     * datapath ID.
     *
     * @param dpid the datapath ID
     * @return the associated datapath info (if any)
     */
    protected Object getMemento(DataPathId dpid) {
        return dpid.memento;
    }
}
