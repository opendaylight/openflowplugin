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
