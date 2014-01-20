/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.yangtools.yang.binding.Notification;

public class NotificationPopListener<T> implements PopListener<T> {


    @Override
    public void onPop(T processedMessage) {
        if(processedMessage instanceof Notification) {
            //TODO: create via factory, inject service
            NotificationProviderService notificationProviderService = OFSessionUtil.getSessionManager().getNotificationProviderService();
            notificationProviderService.publish((Notification) processedMessage);
        }
    }

}
