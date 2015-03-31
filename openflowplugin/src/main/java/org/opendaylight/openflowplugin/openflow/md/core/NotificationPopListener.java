/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.api.openflow.md.queue.PopListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.MessageSpy;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * general publisher to MD-SAL 
 * 
 * @param <T> type of supported notification
 */
public class NotificationPopListener<T> implements PopListener<T> {
    
    private MessageSpy<? super T> messageSpy;
    private NotificationProviderService notificationProviderService;
    
    /**
     * @param messageSpy the messageSpy to set
     */
    public void setMessageSpy(MessageSpy<? super T> messageSpy) {
        this.messageSpy = messageSpy;
    }

    /**
     * @param notificationProviderService the notificationProviderService to set
     */
    public void setNotificationProviderService(
            NotificationProviderService notificationProviderService) {
        this.notificationProviderService = notificationProviderService;
    }

    @Override
    public void onPop(T processedMessage) {
        boolean published = false;
        if(processedMessage instanceof Notification) {
            if (notificationProviderService != null) {
                notificationProviderService.publish((Notification) processedMessage);
                messageSpy.spyMessage(processedMessage, MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_SUCCESS);
                published = true;
            }
        }
        
        if (! published) {
            messageSpy.spyMessage(processedMessage, MessageSpy.STATISTIC_GROUP.FROM_SWITCH_PUBLISHED_FAILURE);
        }
    }

}
