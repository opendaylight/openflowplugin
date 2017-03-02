/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core;

/**
 * provider of wrapped notification enqueue.
 */
public interface NotificationEnqueuer {

    /**
     * enqueue given notification into standard message processing queue.
     * @param notification notification
     */
    void enqueueNotification(NotificationQueueWrapper notification);

}
