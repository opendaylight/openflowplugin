/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.DataPathListener;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.controller.RoleAdvisor;
import org.opendaylight.of.lib.msg.MessageType;

import java.util.Set;

/**
 * Provides a base implementation for sub-components of the core OpenFlow
 * controller. This includes default behavior for registering listeners.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public abstract class AbstractSubComponent {

    /** Our handle on the core controller's listener service. */
    protected ListenerService listenerService;

    /** Our handle on the role advisor service. */
    protected RoleAdvisor roleAdvisor;


    /**
     * Initializes the sub-component with a reference to the listener service.
     * This method also registers message and datapath listeners if the
     * subclass has overridden the appropriate methods.
     *
     * @param ls the listener service
     * @param ra the role advisor service
     * @return self, for chaining
     */
    protected AbstractSubComponent init(ListenerService ls, RoleAdvisor ra) {
        listenerService = ls;
        roleAdvisor = ra;

        DataPathListener dpl = getMyDataPathListener();
        if (dpl != null)
            ls.addDataPathListener(dpl);

        MessageListener ml = getMyMessageListener();
        if (ml != null)
            ls.addMessageListener(ml, getMessageTypes());

        return this;
    }

    /**
     * Subclasses should override this method to update any configuration
     * parameters that are defined for them on the given controller config.
     * These parameters are specifically those that did not require the
     * controller to bounce.
     * <p>
     * This default implementation does nothing.
     *
     * @param cfg the new config
     */
    protected void updateNonBounceConfig(ControllerConfig cfg) { }

    /**
     * Gracefully shuts down the sub-component by removing any
     * registered listeners.
     */
    protected void shutdown() {
        // unregister listeners
        DataPathListener dpl = getMyDataPathListener();
        if (dpl != null)
            listenerService.removeDataPathListener(dpl);
        MessageListener ml = getMyMessageListener();
        if (ml != null)
            listenerService.removeMessageListener(ml);
    }

    /** Returns the datapath listener implementation to be registered with
     * the core controller listener service.
     * <p>
     * This default implementation returns null; no datapath listener will
     * be registered. Subclasses should override this method to provide
     * their datapath listener, if desired.
     *
     * @return a datapath listener instance, if desired
     */
    protected DataPathListener getMyDataPathListener() {
        return null;
    }

    /** Returns the message listener implementation to be registered with
     * the core controller listener service.
     * <p>
     * This default implementation returns null; no message listener will
     * be registered. Subclasses should override this method to provide
     * their message listener, if desired.
     *
     * @return a message listener instance, if desired
     */
    protected MessageListener getMyMessageListener() {
        return null;
    }

    /** Returns the message types of interest for our message listener.
     * See {@link ListenerService#addMessageListener}
     * for a description of what this set should be.
     * <p>
     * This default implementation returns null, which is interpreted as
     * wishing to hear all messages regardless of type.
     *
     * @return the message types of interest
     */
    protected Set<MessageType> getMessageTypes() {
        return null;
    }

}
