/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;

/**
 * <p>
 * openflowplugin-api
 * org.opendaylight.openflowplugin.api.openflow.device
 * Collects multipart msgs from device by provided XID and returns them
 * to the caller as request/collection response one-to-one contract.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         </p>
 *         Created: Mar 23, 2015
 */
public interface MultiMsgCollector {
    /**
     * Method adds a reply multipart message to the collection and if the message has marker
     * "I'M A LAST" method set whole Collection to Future object and remove from cache.
     *
     * @param reply
     */
    void addMultipartMsg(@Nonnull MultipartReply reply);

    void addMultipartMsg(@Nonnull MultipartReply reply, @Nonnull EventIdentifier eventIdentifier);

    /**
     * Null response could be a valid end multipart collecting event for barrier response scenario.
     * We are not able to resolve an issue (it is or it isn't barrier scenario) so we have to finish
     * collecting multipart messages successfully.
     */
    void endCollecting();

    void endCollecting(EventIdentifier eventIdentifier);

}
