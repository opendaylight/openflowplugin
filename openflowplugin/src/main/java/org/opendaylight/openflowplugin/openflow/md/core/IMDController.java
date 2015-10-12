/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;

import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

public interface IMDController {

    /**
     * Allows application to start translating OF messages received from switches.
     *
     * @param messageType
     *            the type of OF message that applications want to receive
     * @param version corresponding OF version
     * @param translator
     *            : Object that implements the {@link org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator}
     */
    public void addMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, List<DataObject>> translator);

    /**
     * Allows application to stop receiving OF message received from switches.
     *
     * @param messageType
     *            The type of OF message that applications want to stop
     *            receiving
     * @param version TODO
     * @param translator
     *            The object that implements the {@link IMDMessageTranslator}
     */
    public void removeMessageTranslator(Class<? extends DataObject> messageType, int version, IMDMessageTranslator<OfHeader, List<DataObject>> translator);

    /**
     * Allows application to start pop-listening MD-SAL messages received from switches.
     *
     * @param messageType
     *            the type of OF message that applications want to receive
     * @param popListener
     *            : Object that implements the {@link PopListener}
     */
    void removeMessagePopListener(Class<? extends DataObject> messageType, PopListener<DataObject> popListener);

    /**
     * Allows application to stop pop-listening MD-SAL messages received from switches.
     *
     * @param messageType
     *            the type of OF message that applications want to receive
     * @param popListener
     *            : Object that implements the {@link PopListener}
     */
    void addMessagePopListener(Class<? extends DataObject> messageType, PopListener<DataObject> popListener);

}
