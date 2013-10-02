/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.plan;

import org.opendaylight.yangtools.yang.binding.Notification;

/**
 * @author mirehak
 */
public interface SwitchTestNotificationEvent extends SwitchTestEvent {

    /**
     * @return next switch notification/rpc response
     */
    Notification getPlannedNotification();
}
