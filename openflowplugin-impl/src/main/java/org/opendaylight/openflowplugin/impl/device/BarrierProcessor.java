/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.OutstandingMessageExtractor;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.device
 *
 *
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *
 * Created: Apr 3, 2015
 */
public class BarrierProcessor {

    /**
     * for all requestContexts from deviceContext cache which are older than barrier (lower barrierXid value) we do: <br>
     *     <ul>
     *         <li>remove from cache</li>
     *         <li>cancel inner future</li>
     *     </ul>
     *
     * @param barrierXid
     * @param messageExtractor
     */
    public static void processOutstandingRequests(final long barrierXid, final OutstandingMessageExtractor messageExtractor) {
        RequestContext nextRequestContext;
        while ((nextRequestContext = messageExtractor.extractNextOutstandingMessage(barrierXid)) != null ) {
            nextRequestContext.getFuture().cancel(false);
        }
    }
}
