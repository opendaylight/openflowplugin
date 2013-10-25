package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yangtools.yang.binding.DataObject;

public interface IMDMessageListener {

    /**
     * This method is called by the MDController/ConnectionConductor when a
     * message is received from a switch. Application who is interested in
     * receiving OF Messages needs to implement this method.
     *
     * @param sw
     *            The SessionContext which sent the OF message
     * @param msg
     *            The OF message
     */
    public void receive(SwitchConnectionDistinguisher cookie, SessionContext sw, DataObject msg);

}
