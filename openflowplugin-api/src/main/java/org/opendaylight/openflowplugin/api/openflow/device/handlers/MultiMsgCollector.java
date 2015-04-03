/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import javax.annotation.Nonnull;

/**
 * openflowplugin-api
 * org.opendaylight.openflowplugin.api.openflow.device
 * <p/>
 * Collects multipart msgs from device by provided XID and returns them
 * to the caller as request/collection response one-to-one contract.
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 * @author <a href="mailto:tkubas@cisco.com">Timotej Kubas</a>
 *         <p/>
 *         Created: Mar 23, 2015
 */
public interface MultiMsgCollector {

    /**
     * Property used to know a max life time of Multipart collection in internal Cache
     */
    final int DEFAULT_TIME_OUT = 10;

    /**
     * Method registers a transaction id xid to the Multipart messages collector
     * and returns Settable future with all MultipartReply. Method has to be called before
     * send a request to the device, otherwise there is a small possibility to miss a first msg.
     *
     * @param xid
     * @return
     */
    void registerMultipartXid(long xid);

    /**
     * Method adds a reply multipart message to the collection and if the message has marker
     * "I'M A LAST" method set whole Collection to Future object and remove from cache.
     *
     * @param reply
     */
    void addMultipartMsg(@Nonnull MultipartReply reply);
}
