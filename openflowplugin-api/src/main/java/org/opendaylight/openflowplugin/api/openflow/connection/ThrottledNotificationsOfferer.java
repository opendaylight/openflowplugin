/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.connection;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Queue;
import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.5.2015.
 */
public interface ThrottledNotificationsOfferer extends AutoCloseable {

    ListenableFuture<Void> applyThrottlingOnConnection(Queue<? extends Notification> notificationsQueue);

    boolean isThrottlingEffective(Queue<? extends Notification> notificationsQueue);

    @Override
    void close() throws SecurityException;
}
