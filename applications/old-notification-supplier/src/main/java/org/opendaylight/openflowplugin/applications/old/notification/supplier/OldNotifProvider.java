/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier;

/**
 * Project module provider interface representation
 */
public interface OldNotifProvider extends AutoCloseable {

    /**
     * Method is responsible for initialization and registration all Old Notification Suppliers
     * followed by settings from ConfigSubsystem
     */
    void start();

}

