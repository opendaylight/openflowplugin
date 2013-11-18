package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;

import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.queue.PopListener;
import org.opendaylight.yangtools.yang.binding.Notification;

public class NotificationPopListener<T> implements PopListener<T> {


    @Override
    public void onPop(List<T> processedMessage) {
        OFSessionUtil.getSessionManager().getTranslatorMapping();
        for ( T message : processedMessage ) {
            if(message instanceof Notification) {
                OFSessionUtil.getSessionManager().getNotificationProviderService().publish((Notification) message);
            }
        }

    }

}
