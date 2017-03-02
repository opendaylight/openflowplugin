/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core;

import com.google.common.base.Preconditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.Notification;

public class NotificationQueueWrapper implements OfHeader {

    private final Notification notification;
    private final Short version;
    private Long xid = -1L;


    /**
     * Notofication queue wrapper.
     * @param notification notofication
     * @param version version
     */
    public NotificationQueueWrapper(final Notification notification, final Short version) {
        Preconditions.checkArgument(notification != null, "wrapped notification must not be null");
        Preconditions.checkArgument(version != null, "message version of wrapped notification must not be null");
        this.notification = notification;
        this.version = version;
    }

    @Override
    public Class<? extends DataContainer> getImplementedInterface() {
        return NotificationQueueWrapper.class;
    }

    @Override
    public Short getVersion() {
        return version;
    }

    @Override
    public Long getXid() {
        return xid;
    }

    /**
     * return the notification.
     */
    public Notification getNotification() {
        return notification;
    }

    /**
     * Setter.
     * @param xid the xid to set
     */
    public void setXid(Long xid) {
        this.xid = xid;
    }
}
