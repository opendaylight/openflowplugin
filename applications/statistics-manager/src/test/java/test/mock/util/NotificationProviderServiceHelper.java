/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package test.mock.util;

import com.google.common.collect.LinkedHashMultimap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import org.opendaylight.controller.sal.binding.api.NotificationListener;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationProviderServiceHelper {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationProviderServiceHelper.class);

    private NotificationProviderService notifBroker = new NotificationProviderServiceDummyImpl();

    public NotificationProviderService getNotifBroker() {
        return notifBroker;
    }

    public void pushDelayedNotification(final Notification notification, int delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                notifBroker.publish(notification);
            }
        }, delay);
    }

    public void pushNotification(final Notification notification) {
        notifBroker.publish(notification);
    }

    private static class NotificationListenerExecTuple {
        Method m;
        org.opendaylight.yangtools.yang.binding.NotificationListener listenerInst;

        void propagateNotification(Notification notification) {
            try {
                m.invoke(listenerInst, notification);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error("Exception occurred: {} ", e.getMessage(), e);
            }
        }

        @Override
        public int hashCode() {
            return listenerInst.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return listenerInst.equals(obj);
        }

    }

    private static class NotificationProviderServiceDummyImpl implements NotificationProviderService {
        private LinkedHashMultimap<Class, NotificationListenerExecTuple> listenerRegistry = LinkedHashMultimap.create();

        @Override

        public void publish(Notification notification) {
            Set<NotificationListenerExecTuple> execPack = listenerRegistry.get(notification.getImplementedInterface());
            for (NotificationListenerExecTuple notificationListenerExecTuple : execPack) {
                notificationListenerExecTuple.propagateNotification(notification);
            }
        }

        @Override
        public void publish(Notification notification, ExecutorService executorService) {
            throw new IllegalAccessError("publish with executorService not supported");
        }

        @Override
        public ListenerRegistration<NotificationInterestListener> registerInterestListener(NotificationInterestListener notificationInterestListener) {
            throw new IllegalAccessError("registering of interest listener not supported");
        }

        @Override
        public <T extends Notification> ListenerRegistration<NotificationListener<T>> registerNotificationListener(Class<T> aClass, NotificationListener<T> notificationListener) {
            throw new IllegalAccessError("registering with class not supported");
        }

        @Override
        public ListenerRegistration<org.opendaylight.yangtools.yang.binding.NotificationListener> registerNotificationListener(org.opendaylight.yangtools.yang.binding.NotificationListener notificationListener) {
            for (Method m : notificationListener.getClass().getMethods()) {
                if (m.getName().startsWith("on") && m.getParameterTypes().length == 1) {
                    Class<?> key = m.getParameterTypes()[0];
                    Set<NotificationListenerExecTuple> listeners = listenerRegistry.get(key);
                    NotificationListenerExecTuple execPack = new NotificationListenerExecTuple();
                    execPack.listenerInst = notificationListener;
                    execPack.m = m;
                    listeners.add(execPack);
                }
            }
            return null;
        }
    }
}
