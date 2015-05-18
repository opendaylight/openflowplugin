/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.plan;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * @author mirehak
 */
public class SwitchTestNotificationEventImpl implements
        SwitchTestNotificationEvent {

    private Notification notification;

    /**
     * @param notification
     *            the notification to set
     */
    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    @Override
    public Notification getPlannedNotification() {
        return notification;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SwitchTestNotificationEventImpl [notification=");
        if (notification instanceof OfHeader) {
            OfHeader header = (OfHeader) notification;
            sb.append("version:").append(header.getVersion()).append(';')
                .append("xid:").append(header.getXid()).append(';')
                .append("type:").append(header.getClass().getSimpleName());
        } else {
            sb.append(notification.toString());
        }
        sb.append(']');
        return sb.toString();
    }
}
