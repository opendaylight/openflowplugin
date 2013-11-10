package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

public interface IMDController {

    /**
     * Allows application to start receiving OF messages received from switches.
     *
     * @param messageType
     *            the type of OF message that applications want to receive
     * @param version corresponding OF version
     * @param translator
     *            : Object that implements the IMDMessageListener
     */
    public void addMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, DataObject> translator);

    /**
     * Allows application to stop receiving OF message received from switches.
     *
     * @param messageType
     *            The type of OF message that applications want to stop
     *            receiving
     * @param version TODO
     * @param translator
     *            The object that implements the IMDMessageListener
     */
    public void removeMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, DataObject> translator);

}
