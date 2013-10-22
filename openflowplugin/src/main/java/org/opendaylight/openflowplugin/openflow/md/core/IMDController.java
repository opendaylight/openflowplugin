package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.yangtools.yang.binding.DataObject;

public interface IMDController {

    /**
     * Allows application to start receiving OF messages received from switches.
     *
     * @param type
     *            the type of OF message that applications want to receive
     * @param listener
     *            : Object that implements the IMDMessageListener
     */
    public void addMessageListener(Class<? extends DataObject> messageType, IMDMessageListener listener);

    /**
     * Allows application to stop receiving OF message received from switches.
     *
     * @param type
     *            The type of OF message that applications want to stop
     *            receiving
     * @param listener
     *            The object that implements the IMDMessageListener
     */
    public void removeMessageListener(Class<? extends DataObject> messageType, IMDMessageListener listener);

}
